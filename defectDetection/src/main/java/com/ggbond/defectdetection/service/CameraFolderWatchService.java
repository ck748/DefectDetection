package com.ggbond.defectdetection.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class CameraFolderWatchService {

    // 默认服务器端存储与监听目录（Web静态映射支持 /uploads/**）
    @Value("${file.camera-watch-dir:/root/desc/cmzj-main/mijia-watcher/image}")
    private String serverWatchDir = "/root/desc/cmzj-main/mijia-watcher/image";

    // AI 视觉推理接口地址
    @Value("${ai.detect.url:http://192.168.1.3:9001/Qualified}")
    private String aiDetectUrl = "http://192.168.1.3:9001/Qualified";

    @Autowired
    private CameraWatchRecordService cameraWatchRecordService;

    private WatchService watchService;
    private ExecutorService executorService;
    private ScheduledExecutorService scanExecutorService;
    private volatile boolean isRunning = false;

    // 本地快速去重缓存
    private final Set<String> processedFileNames = Collections.synchronizedSet(new HashSet<>());

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    /**
     * 接收客户端（如 mijia-watcher 守护进程）上传的米家拍照图片并入库，同时触发 AI 识别
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

            String webUrl = buildWebUrl(storedName);
            String formattedSize = String.format("%.2f KB", file.getSize() / 1024.0);

            CameraWatchRecord record = new CameraWatchRecord();
            record.setFileName(originalName);
            record.setStoredName(storedName);
            record.setFilePath(destFile.getAbsolutePath().replace("\\", "/"));
            record.setWebUrl(webUrl);
            record.setFileSize(formattedSize);
            record.setFileBytes(file.getSize());
            record.setUploadTime(LocalDateTime.now());
            record.setServerWatchDir(this.serverWatchDir);
            record.setStatus("已同步待识别");

            // 同步调用 AI 推理接口
            processAiDetection(destFile, record);

            cameraWatchRecordService.save(record);
            processedFileNames.add(originalName);

            log.info("📸 米家相机图片成功接收并完成AI识别入库: 原始名={}, 存盘名={}, 状态={}", originalName, storedName, record.getStatus());
            return Result.success("图片上传并识别入库成功", record);
        } catch (Exception e) {
            log.error("保存上传图片并识别失败:", e);
            return Result.fail("保存上传图片失败: " + e.getMessage());
        }
    }

    /**
     * 核心方法：调用 AI 视觉服务接口进行推理并将标注结果保存
     */
    private void processAiDetection(File imageFile, CameraWatchRecord record) {
        if (imageFile == null || !imageFile.exists()) {
            return;
        }

        try {
            log.info("🚀 正在向 AI 视觉推理服务发送请求: url={}, file={}", aiDetectUrl, imageFile.getName());

            // 发送 Multipart 文件请求
            HttpResponse response = HttpRequest.post(aiDetectUrl)
                    .form("file", imageFile)
                    .timeout(10000)
                    .execute();

            if (response.isOk()) {
                String body = response.body();
                log.info("✅ AI 推理返回结果: {}", body.length() > 200 ? body.substring(0, 200) + "..." : body);

                JSONObject resObj = JSONUtil.parseObj(body);

                // 提取标注后的图片 Base64
                String annotatedBase64 = resObj.getStr("image_base64");
                if (annotatedBase64 == null || annotatedBase64.isEmpty()) {
                    annotatedBase64 = resObj.getStr("image");
                }

                // 提取置信度与状态
                Double confidence = resObj.getDouble("confidence");
                String status = resObj.getStr("status");
                if (status == null || status.isEmpty()) {
                    Boolean isQualified = resObj.getBool("is_qualified");
                    if (isQualified != null) {
                        status = isQualified ? "合格" : "不合格";
                    } else {
                        status = "识别完成";
                    }
                }

                if (confidence != null) {
                    status += String.format(" (%.1f%%)", confidence * 100);
                }

                record.setStatus(status);

                // 若 AI 返回了带标注红框的结果图，将其解码另存为 annotated_xxx.jpg 并设为展示主图
                if (annotatedBase64 != null && !annotatedBase64.trim().isEmpty()) {
                    String base64Data = annotatedBase64;
                    if (base64Data.contains(",")) {
                        base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                    }

                    byte[] imgBytes = Base64.decode(base64Data);
                    String annotatedFileName = "annotated_" + record.getStoredName();
                    File annotatedFile = new File(imageFile.getParentFile(), annotatedFileName);

                    try (FileOutputStream fos = new FileOutputStream(annotatedFile)) {
                        fos.write(imgBytes);
                        fos.flush();
                    }

                    // 更新展示与存储路径为识别结果图
                    record.setStoredName(annotatedFileName);
                    record.setFilePath(annotatedFile.getAbsolutePath().replace("\\", "/"));
                    record.setWebUrl(buildWebUrl(annotatedFileName));
                    log.info("🎯 已生成带红框的 AI 识别结果图并更新: {}", annotatedFileName);
                }
            } else {
                log.warn("AI 视觉接口响应非 200: code={}, body={}", response.getStatus(), response.body());
                record.setStatus("识别服务异常(" + response.getStatus() + ")");
            }
        } catch (Exception e) {
            log.error("调用 AI 接口异常:", e);
            record.setStatus("已同步 (AI未连接)");
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
            boolean ok = folder.mkdirs();
            log.info("创建服务器监听存储目录: {} -> {}", this.serverWatchDir, ok);
        }

        stopLocalNioWatch();

        try {
            this.isRunning = true;

            // 若运行于本地支持磁盘读写的机器，同时启动本地 NIO 监听以便本地调试直接丢图
            if (folder.exists() && folder.isDirectory()) {
                scanExistingImages();
                try {
                    this.watchService = FileSystems.getDefault().newWatchService();
                    Path path = Paths.get(this.serverWatchDir);
                    path.register(this.watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY);

                    this.executorService = Executors.newSingleThreadExecutor();
                    this.executorService.submit(this::listenFolderLoop);

                    this.scanExecutorService = Executors.newSingleThreadScheduledExecutor();
                    this.scanExecutorService.scheduleWithFixedDelay(this::scanExistingImages, 2, 2, TimeUnit.SECONDS);
                } catch (Exception nioEx) {
                    log.warn("NIO本地文件监控未启动(跨机部署由HTTP上传触发): {}", nioEx.getMessage());
                }
            }

            log.info("【摄像头监听服务】已启动，当前服务器存储目录: {}", this.serverWatchDir);
            return Result.success("监听服务已启动，当前服务器目录：" + this.serverWatchDir);
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
     * 获取状态与最新抓拍图片流列表（从数据库中拉取）
     */
    public Map<String, Object> getStatusAndImages() {
        Map<String, Object> map = new HashMap<>();
        map.put("running", this.isRunning);
        map.put("watchDir", this.serverWatchDir);

        try {
            LambdaQueryWrapper<CameraWatchRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(CameraWatchRecord::getUploadTime);
            queryWrapper.last("LIMIT 100");

            List<CameraWatchRecord> records = cameraWatchRecordService.list(queryWrapper);
            List<Map<String, Object>> list = new ArrayList<>();

            for (CameraWatchRecord r : records) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", String.valueOf(r.getId()));
                item.put("fileName", r.getFileName());
                // 动态生成标准图片URL，携带 id 与 name 确保双重精准寻址
                String safeUrl = r.getWebUrl();
                if (safeUrl == null || safeUrl.isEmpty() || safeUrl.contains("/detectInfo/")) {
                    String paramName = r.getStoredName() != null ? r.getStoredName() : r.getFileName();
                    safeUrl = "/api/cameraWatch/image?id=" + r.getId() + "&name=" + (paramName != null ? paramName : "");
                }
                item.put("imgUrl", safeUrl);
                item.put("createTime", r.getUploadTime() != null ? r.getUploadTime().format(TIME_FORMATTER) : "");
                item.put("fileSize", r.getFileSize() != null ? r.getFileSize() : "0 KB");
                item.put("status", r.getStatus() != null ? r.getStatus() : "已同步");
                list.add(item);
            }

            map.put("list", list);
        } catch (Exception e) {
            log.error("查询相机监听记录异常:", e);
            map.put("list", Collections.emptyList());
        }

        return map;
    }

    /**
     * 单张图片删除（支持物理删除与数据库逻辑删除）
     */
    public Result<String> deleteImage(Integer id, boolean deleteSourceFile) {
        if (id == null) {
            return Result.fail("图片ID不能为空");
        }

        CameraWatchRecord record = cameraWatchRecordService.getById(id);
        if (record == null) {
            return Result.fail("未找到对应图片记录");
        }

        if (deleteSourceFile && record.getFilePath() != null) {
            try {
                File file = new File(record.getFilePath());
                if (file.exists()) {
                    boolean deleted = file.delete();
                    log.info("物理删除图片文件: {} -> {}", file.getAbsolutePath(), deleted);
                }
            } catch (Exception e) {
                log.warn("物理删除文件异常: {}", e.getMessage());
            }
        }

        cameraWatchRecordService.removeById(id);
        if (record.getFileName() != null) {
            processedFileNames.remove(record.getFileName());
        }

        return Result.success("图片记录删除成功" + (deleteSourceFile ? "（已同步删除服务器物理文件）" : ""));
    }

    /**
     * 清空图片记录（支持物理删除）
     */
    public Result<String> clearImages(boolean deletePhysical) {
        try {
            if (deletePhysical) {
                List<CameraWatchRecord> records = cameraWatchRecordService.list();
                for (CameraWatchRecord r : records) {
                    if (r.getFilePath() != null) {
                        File f = new File(r.getFilePath());
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                }
            }

            LambdaUpdateWrapper<CameraWatchRecord> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(CameraWatchRecord::getIsDeleted, 1);
            cameraWatchRecordService.update(updateWrapper);

            processedFileNames.clear();
            return Result.success("记录已清空" + (deletePhysical ? "（已物理清理服务器图片）" : ""));
        } catch (Exception e) {
            log.error("清空记录异常:", e);
            return Result.fail("清空记录异常: " + e.getMessage());
        }
    }

    private String buildWebUrl(String storedName) {
        return "/api/cameraWatch/image?name=" + storedName;
    }

    /**
     * 将图片文件流直接写入 HTTP 响应（跨机器/容器部署最健壮方案）
     */
    public void writeImageStream(Integer id, String name, HttpServletResponse response) {
        try {
            File targetFile = null;
            if (id != null) {
                CameraWatchRecord record = cameraWatchRecordService.getById(id);
                if (record != null && record.getFilePath() != null) {
                    targetFile = new File(record.getFilePath());
                }
            }
            if ((targetFile == null || !targetFile.exists()) && name != null && !name.trim().isEmpty()) {
                String cleanName = new File(name.trim()).getName();
                targetFile = new File(this.serverWatchDir, cleanName);
                if (!targetFile.exists()) {
                    targetFile = new File("/root/desc/cmzj-main/mijia-watcher/image", cleanName);
                }
                if (!targetFile.exists()) {
                    targetFile = new File("uploads/camera_watch", cleanName);
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
            response.setHeader("Cache-Control", "max-age=86400, public");

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

    private void scanExistingImages() {
        if (!isRunning) return;
        try {
            File folder = new File(this.serverWatchDir);
            if (!folder.exists() || !folder.isDirectory()) return;

            // 仅对有效普通文件夹进行首层轻量扫描，避免重复扫描带有 annotated_ 前缀的识别结果图
            File[] files = folder.listFiles(f -> f != null && f.isFile() && !f.getName().startsWith("annotated_"));
            if (files == null || files.length == 0) return;

            Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName().toLowerCase();
                    if (isImageFile(name) && !processedFileNames.contains(file.getName()) && file.length() > 0) {
                        // 检查数据库是否已存在该文件名
                        LambdaQueryWrapper<CameraWatchRecord> qw = new LambdaQueryWrapper<>();
                        qw.eq(CameraWatchRecord::getFileName, file.getName());
                        long count = cameraWatchRecordService.count(qw);
                        if (count == 0) {
                            CameraWatchRecord record = new CameraWatchRecord();
                            record.setFileName(file.getName());
                            record.setStoredName(file.getName());
                            record.setFilePath(file.getAbsolutePath().replace("\\", "/"));
                            record.setWebUrl(buildWebUrl(file.getName()));
                            record.setFileSize(String.format("%.2f KB", file.length() / 1024.0));
                            record.setFileBytes(file.length());
                            record.setUploadTime(LocalDateTime.now());
                            record.setServerWatchDir(this.serverWatchDir);
                            record.setStatus("已同步待识别");

                            // 触发 AI 推理
                            processAiDetection(file, record);

                            cameraWatchRecordService.save(record);
                        }
                        processedFileNames.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("扫描目录存量图片异常:", e);
        }
    }

    private void listenFolderLoop() {
        Path path = Paths.get(this.serverWatchDir);
        while (isRunning) {
            try {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path fileName = (Path) event.context();
                    String nameStr = fileName.toString().toLowerCase();

                    if (isImageFile(nameStr) && !nameStr.startsWith("annotated_")) {
                        Path fullPath = path.resolve(fileName);
                        File targetFile = fullPath.toFile();

                        Thread.sleep(300);
                        if (targetFile.exists() && targetFile.length() > 0 && !processedFileNames.contains(targetFile.getName())) {
                            scanExistingImages();
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
