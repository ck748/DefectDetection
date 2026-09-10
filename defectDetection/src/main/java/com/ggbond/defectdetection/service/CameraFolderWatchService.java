package com.ggbond.defectdetection.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.CameraWatchRecord;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class CameraFolderWatchService {

    // 默认服务器端存储与监听目录
    @Value("${file.camera-watch-dir:/root/desc/cmzj-main/mijia-watcher/image}")
    private String serverWatchDir = "/root/desc/cmzj-main/mijia-watcher/image";

    // AI 视觉推理接口地址
    @Value("${ai.detect.url:http://192.168.1.3:9001/Qualified}")
    private String aiDetectUrl = "http://192.168.1.3:9001/Qualified";

    @Autowired(required = false)
    private CameraWatchRecordService cameraWatchRecordService;

    private WatchService watchService;
    private ExecutorService executorService;
    private ScheduledExecutorService scanExecutorService;
    private volatile boolean isRunning = false;

    // 本地去重/已识别缓存（避免重复调用 AI）
    private final Set<String> processedFileNames = Collections.synchronizedSet(new HashSet<>());

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    /**
     * 接收客户端上传的米家拍照图片并存盘，自动调用 AI 识别
     */
    public synchronized Result<CameraWatchRecord> saveUploadedImage(MultipartFile file, String customFileName) {
        if (file == null || file.isEmpty()) {
            return Result.fail("上传的图片文件为空");
        }

        try {
            File targetDir = new File(this.serverWatchDir);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            String originalName = (customFileName != null && !customFileName.trim().isEmpty())
                    ? customFileName.trim()
                    : file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) {
                originalName = "camera_capture.jpg";
            }

            String ext = ".jpg";
            if (originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            String timestamp = LocalDateTime.now().format(FILE_TIME_FORMATTER);
            String storedName = "mijia_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            File destFile = new File(targetDir, storedName);
            file.transferTo(destFile.getAbsoluteFile());

            String formattedSize = String.format("%.2f KB", file.getSize() / 1024.0);

            CameraWatchRecord record = new CameraWatchRecord();
            record.setFileName(originalName);
            record.setStoredName(storedName);
            record.setFilePath(destFile.getAbsolutePath().replace("\\", "/"));
            record.setWebUrl(buildWebUrl(storedName));
            record.setFileSize(formattedSize);
            record.setFileBytes(file.getSize());
            record.setUploadTime(LocalDateTime.now());
            record.setServerWatchDir(this.serverWatchDir);
            record.setStatus("已同步");

            // 同步调用 AI 推理接口
            processAiDetection(destFile);

            processedFileNames.add(originalName);
            processedFileNames.add(storedName);

            log.info("📸 米家相机图片成功存盘并触发AI识别: 原始名={}, 存盘名={}", originalName, storedName);
            return Result.success("图片存盘成功", record);
        } catch (Exception e) {
            log.error("保存上传图片失败:", e);
            return Result.fail("保存上传图片失败: " + e.getMessage());
        }
    }

    /**
     * 调用 AI 视觉服务接口进行推理并将带红框结果图落地到目录
     */
    private void processAiDetection(File imageFile) {
        if (imageFile == null || !imageFile.exists() || imageFile.getName().startsWith("annotated_")) {
            return;
        }

        try {
            log.info("🚀 正在向 AI 视觉推理服务发送请求: url={}, file={}", aiDetectUrl, imageFile.getName());

            HttpResponse response = HttpRequest.post(aiDetectUrl)
                    .form("file", imageFile)
                    .timeout(10000)
                    .execute();

            if (response.isOk()) {
                String body = response.body();
                log.info("✅ AI 推理返回: {}", body.length() > 200 ? body.substring(0, 200) + "..." : body);

                JSONObject resObj = JSONUtil.parseObj(body);
                String annotatedBase64 = resObj.getStr("image_base64");
                if (annotatedBase64 == null || annotatedBase64.isEmpty()) {
                    annotatedBase64 = resObj.getStr("image");
                }

                // 若 AI 返回了带红框的结果图，将其直接保存至同一目录下
                if (annotatedBase64 != null && !annotatedBase64.trim().isEmpty()) {
                    String base64Data = annotatedBase64;
                    if (base64Data.contains(",")) {
                        base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                    }

                    byte[] imgBytes = Base64.decode(base64Data);
                    String annotatedFileName = "annotated_" + imageFile.getName();
                    File annotatedFile = new File(imageFile.getParentFile(), annotatedFileName);

                    try (FileOutputStream fos = new FileOutputStream(annotatedFile)) {
                        fos.write(imgBytes);
                        fos.flush();
                    }
                    processedFileNames.add(annotatedFileName);
                    log.info("🎯 已生成带红框的识别图: {}", annotatedFileName);
                }
            } else {
                log.warn("AI 视觉接口响应非 200: code={}", response.getStatus());
            }
        } catch (Exception e) {
            log.error("调用 AI 接口异常: {}", e.getMessage());
        }
    }

    /**
     * 启动/切换服务器存储监听目录
     */
    public synchronized Result<String> startWatch(String customPath) {
        if (customPath != null && !customPath.trim().isEmpty()) {
            this.serverWatchDir = customPath.trim();
        }

        File folder = new File(this.serverWatchDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        stopLocalNioWatch();

        try {
            this.isRunning = true;

            if (folder.exists() && folder.isDirectory()) {
                scanAndDetectImages();
                try {
                    this.watchService = FileSystems.getDefault().newWatchService();
                    Path path = Paths.get(this.serverWatchDir);
                    path.register(this.watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY);

                    this.executorService = Executors.newSingleThreadExecutor();
                    this.executorService.submit(this::listenFolderLoop);

                    this.scanExecutorService = Executors.newSingleThreadScheduledExecutor();
                    this.scanExecutorService.scheduleWithFixedDelay(this::scanAndDetectImages, 2, 2, TimeUnit.SECONDS);
                } catch (Exception nioEx) {
                    log.warn("NIO本地文件监控未启动: {}", nioEx.getMessage());
                }
            }

            log.info("【摄像头监听服务】已启动，当前目录: {}", this.serverWatchDir);
            return Result.success("监听服务已启动，当前目录：" + this.serverWatchDir);
        } catch (Exception e) {
            log.error("启动目录失败:", e);
            return Result.fail("启动目录失败：" + e.getMessage());
        }
    }

    /**
     * 停止监听
     */
    public synchronized Result<String> stopWatch() {
        this.isRunning = false;
        stopLocalNioWatch();
        log.info("【摄像头监听服务】已停止");
        return Result.success("监听已停止");
    }

    /**
     * 核心改造：不读数据库，直接扫描物理目录中的所有图片并按最新时间倒序渲染到前端
     */
    public Map<String, Object> getStatusAndImages() {
        Map<String, Object> map = new HashMap<>();
        map.put("running", this.isRunning);
        map.put("watchDir", this.serverWatchDir);

        List<Map<String, Object>> list = new ArrayList<>();
        try {
            File folder = new File(this.serverWatchDir);
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles(f -> f != null && f.isFile() && isImageFile(f.getName()));
                if (files != null && files.length > 0) {
                    // 按文件最后修改时间倒序排列（最新拍摄排在最前）
                    Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    int maxCount = Math.min(files.length, 100);

                    for (int i = 0; i < maxCount; i++) {
                        File f = files[i];
                        Map<String, Object> item = new HashMap<>();
                        // 用文件名作为前端唯一标识 id
                        item.put("id", f.getName());
                        item.put("fileName", f.getName());
                        item.put("imgUrl", buildWebUrl(f.getName()));
                        item.put("createTime", sdf.format(new Date(f.lastModified())));
                        item.put("fileSize", String.format("%.2f KB", f.length() / 1024.0));
                        item.put("status", f.getName().startsWith("annotated_") ? "AI已识别" : "已捕获");
                        list.add(item);
                    }
                }
            }
        } catch (Exception e) {
            log.error("扫描物理目录读取图片异常:", e);
        }

        map.put("list", list);
        return map;
    }

    /**
     * 删除单张物理图片
     */
    public Result<String> deleteImage(Object idOrName, boolean deleteSourceFile) {
        if (idOrName == null) {
            return Result.fail("图片标识不能为空");
        }

        String fileName = idOrName.toString().trim();
        try {
            File targetFile = new File(this.serverWatchDir, fileName);
            if (targetFile.exists()) {
                boolean ok = targetFile.delete();
                log.info("物理删除图片文件: {} -> {}", targetFile.getAbsolutePath(), ok);
            }
            // 若存在对应的 annotated_ 标注图，一并删除
            File annotatedFile = new File(this.serverWatchDir, "annotated_" + fileName);
            if (annotatedFile.exists()) {
                annotatedFile.delete();
            }
            processedFileNames.remove(fileName);
            return Result.success("物理图片删除成功");
        } catch (Exception e) {
            log.error("删除物理图片异常:", e);
            return Result.fail("删除图片失败: " + e.getMessage());
        }
    }

    /**
     * 清空当前目录下的所有图片
     */
    public Result<String> clearImages(boolean deletePhysical) {
        try {
            File folder = new File(this.serverWatchDir);
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles(f -> f != null && f.isFile() && isImageFile(f.getName()));
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
            processedFileNames.clear();
            return Result.success("已清空目录下的所有图片文件");
        } catch (Exception e) {
            log.error("清空物理图片异常:", e);
            return Result.fail("清空目录异常: " + e.getMessage());
        }
    }

    private String buildWebUrl(String fileName) {
        return "/api/cameraWatch/image?name=" + fileName;
    }

    /**
     * 将物理图片文件流直接写入 HTTP 响应
     */
    public void writeImageStream(Integer id, String name, HttpServletResponse response) {
        try {
            File targetFile = null;
            if (name != null && !name.trim().isEmpty()) {
                String cleanName = new File(name.trim()).getName();
                targetFile = new File(this.serverWatchDir, cleanName);
                if (!targetFile.exists()) {
                    targetFile = new File("/root/desc/cmzj-main/mijia-watcher/image", cleanName);
                }
            }

            if (targetFile == null || !targetFile.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String mimeType = URLConnection.guessContentTypeFromName(targetFile.getName());
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }
            response.setContentType(mimeType);
            response.setContentLengthLong(targetFile.length());
            response.setHeader("Cache-Control", "no-cache");

            try (FileInputStream fis = new FileInputStream(targetFile);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            log.warn("输出相机抓拍图片流失败: {}", e.getMessage());
        }
    }

    private void stopLocalNioWatch() {
        if (this.watchService != null) {
            try {
                this.watchService.close();
            } catch (IOException ignored) {}
            this.watchService = null;
        }
        if (this.executorService != null) {
            this.executorService.shutdownNow();
            this.executorService = null;
        }
        if (this.scanExecutorService != null) {
            this.scanExecutorService.shutdownNow();
            this.scanExecutorService = null;
        }
    }

    private void scanAndDetectImages() {
        if (!isRunning) return;
        try {
            File folder = new File(this.serverWatchDir);
            if (!folder.exists() || !folder.isDirectory()) return;

            File[] files = folder.listFiles(f -> f != null && f.isFile() && !f.getName().startsWith("annotated_"));
            if (files == null || files.length == 0) return;

            for (File file : files) {
                String name = file.getName();
                if (isImageFile(name) && !processedFileNames.contains(name) && file.length() > 0) {
                    processedFileNames.add(name);
                    // 自动发送给 AI 推理
                    processAiDetection(file);
                }
            }
        } catch (Exception e) {
            log.error("扫描目录图片异常:", e);
        }
    }

    private void listenFolderLoop() {
        Path path = Paths.get(this.serverWatchDir);
        while (isRunning) {
            try {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path fileName = (Path) event.context();
                    String nameStr = fileName.toString();

                    if (isImageFile(nameStr) && !nameStr.startsWith("annotated_")) {
                        Path fullPath = path.resolve(fileName);
                        File targetFile = fullPath.toFile();

                        Thread.sleep(300);
                        if (targetFile.exists() && targetFile.length() > 0 && !processedFileNames.contains(targetFile.getName())) {
                            scanAndDetectImages();
                        }
                    }
                }
                if (!key.reset()) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (isRunning) {
                    log.error("本地目录监听处理异常:", e);
                }
            }
        }
    }

    private boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".bmp");
    }

    @PreDestroy
    public void cleanup() {
        stopWatch();
    }
}
