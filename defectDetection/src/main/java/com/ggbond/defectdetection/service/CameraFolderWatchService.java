package com.ggbond.defectdetection.service;

import com.ggbond.defectdetection.common.Result;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class CameraFolderWatchService {

    // 默认监听目录
    private String watchDirPath = "D:\\QQ\\小米摄像头图片存储";

    // 目标存储物理路径（映射为静态资源 uploads/images/）
    @Value("${file.upload-dir:defectDetection/uploads/images}")
    private String uploadDir;

    private WatchService watchService;
    private ExecutorService executorService;
    private ScheduledExecutorService scanExecutorService;
    private volatile boolean isRunning = false;

    // 存储捕获的图片记录（内存安全并发容器）
    private final List<Map<String, Object>> capturedImages = new CopyOnWriteArrayList<>();

    // 记录已经处理过的文件名，防止重复上传
    private final Set<String> processedFileNames = Collections.synchronizedSet(new HashSet<>());

    /**
     * 启动/切换监听
     */
    public synchronized Result<String> startWatch(String customPath) {
        if (customPath != null && !customPath.trim().isEmpty()) {
            this.watchDirPath = customPath.trim();
        }

        File folder = new File(this.watchDirPath);
        if (!folder.exists()) {
            boolean ok = folder.mkdirs();
            log.info("创建小米摄像头监控目录: {} -> {}", this.watchDirPath, ok);
        }

        stopWatch();

        try {
            this.isRunning = true;

            // 1. 立即全量扫描当前目录中已存在的存量图片
            scanExistingImages();

            // 2. 注册 NIO WatchService 监听新建与修改事件
            this.watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(this.watchDirPath);
            path.register(this.watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            this.executorService = Executors.newSingleThreadExecutor();
            this.executorService.submit(this::listenFolderLoop);

            // 3. 启动定时扫描兜底（每 1 秒主动比对一次文件夹），双重保障绝对不漏图
            this.scanExecutorService = Executors.newSingleThreadScheduledExecutor();
            this.scanExecutorService.scheduleWithFixedDelay(this::scanExistingImages, 1, 1, TimeUnit.SECONDS);

            log.info("【摄像头监听服务】启动成功，目标目录: {}", this.watchDirPath);
            return Result.success("监听服务启动成功，目标目录：" + this.watchDirPath);
        } catch (Exception e) {
            log.error("启动监听失败:", e);
            return Result.fail("启动目录监听失败：" + e.getMessage());
        }
    }

    /**
     * 停止监听
     */
    public synchronized Result<String> stopWatch() {
        this.isRunning = false;
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
        log.info("【摄像头监听服务】已停止");
        return Result.success("监听已停止");
    }

    /**
     * 获取状态与捕获列表
     */
    public Map<String, Object> getStatusAndImages() {
        Map<String, Object> map = new HashMap<>();
        map.put("running", this.isRunning);
        map.put("watchDir", this.watchDirPath);
        map.put("list", this.capturedImages);
        return map;
    }

    /**
     * 清空当前捕获列表并支持物理删除文件
     */
    public Result<String> clearImages(boolean deletePhysical) {
        if (deletePhysical) {
            for (Map<String, Object> item : this.capturedImages) {
                deletePhysicalFiles(item, true);
            }
        }
        this.capturedImages.clear();
        this.processedFileNames.clear();
        return Result.success("捕获记录已清空" + (deletePhysical ? "（已同步删除本地图片）" : ""));
    }

    /**
     * 单张图片删除（支持物理删除）
     */
    public Result<String> deleteImage(String id, boolean deleteSourceFile) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("图片ID不能为空");
        }

        Map<String, Object> targetItem = null;
        for (Map<String, Object> item : this.capturedImages) {
            if (id.equals(item.get("id"))) {
                targetItem = item;
                break;
            }
        }

        if (targetItem == null) {
            return Result.fail("未找到对应图片记录");
        }

        // 删除物理文件
        deletePhysicalFiles(targetItem, deleteSourceFile);

        // 移除记录与已处理集合
        this.capturedImages.remove(targetItem);
        if (targetItem.get("fileName") != null) {
            this.processedFileNames.remove(targetItem.get("fileName").toString());
        }

        return Result.success("图片删除成功" + (deleteSourceFile ? "（已同步删除本地源文件）" : ""));
    }

    private void deletePhysicalFiles(Map<String, Object> item, boolean deleteSourceFile) {
        try {
            // 1. 删除 uploads 目录下的图片
            String imgUrl = (String) item.get("imgUrl");
            if (imgUrl != null && !imgUrl.isEmpty()) {
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
                File uploadFile = new File(uploadDir, fileName);
                if (uploadFile.exists()) {
                    boolean deleted = uploadFile.delete();
                    log.info("删除上传目录图片: {} -> {}", uploadFile.getAbsolutePath(), deleted);
                }
            }

            // 2. 如果指定删除监听源目录中的图片
            if (deleteSourceFile) {
                String originalFileName = (String) item.get("fileName");
                if (originalFileName != null && !originalFileName.isEmpty()) {
                    File srcFile = new File(this.watchDirPath, originalFileName);
                    if (srcFile.exists()) {
                        boolean deleted = srcFile.delete();
                        log.info("删除监听源目录图片: {} -> {}", srcFile.getAbsolutePath(), deleted);
                    }
                }
            }
        } catch (Exception e) {
            log.error("物理删除文件异常:", e);
        }
    }

    /**
     * 扫描目录存量及新增图片
     */
    private void scanExistingImages() {
        if (!isRunning) return;
        try {
            File folder = new File(this.watchDirPath);
            if (!folder.exists() || !folder.isDirectory()) return;

            File[] files = folder.listFiles();
            if (files == null || files.length == 0) return;

            // 按最后修改时间倒序排列
            Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName().toLowerCase();
                    if (isSupportedMedia(name)) {
                        if (!processedFileNames.contains(file.getName()) && file.length() > 0) {
                            processNewImage(file);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("扫描目录异常:", e);
        }
    }

    private void listenFolderLoop() {
        Path path = Paths.get(this.watchDirPath);
        while (isRunning) {
            try {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path fileName = (Path) event.context();
                    String nameStr = fileName.toString().toLowerCase();

                    if (isSupportedMedia(nameStr)) {
                        Path fullPath = path.resolve(fileName);
                        File targetFile = fullPath.toFile();

                        // 稍微休眠确保文件完全写入
                        Thread.sleep(500);
                        if (targetFile.exists() && targetFile.length() > 0 && !processedFileNames.contains(targetFile.getName())) {
                            processNewImage(targetFile);
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
                    log.error("监听事件处理异常:", e);
                }
            }
        }
    }

    private void processNewImage(File srcFile) {
        try {
            if (processedFileNames.contains(srcFile.getName())) {
                return;
            }

            File targetDir = new File(uploadDir);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String newFileName = "camera_" + timestamp + "_" + srcFile.getName();
            File destFile = new File(targetDir, newFileName);

            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String accessUrl = "/uploads/images/" + newFileName;
            String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(srcFile.lastModified()));
            boolean isVideoFile = isVideo(srcFile.getName().toLowerCase());

            Map<String, Object> item = new HashMap<>();
            item.put("id", UUID.randomUUID().toString());
            item.put("fileName", srcFile.getName());
            item.put("imgUrl", accessUrl);
            item.put("fileUrl", accessUrl);
            item.put("fileType", isVideoFile ? "video" : "image");
            item.put("createTime", timeStr);
            item.put("fileSize", String.format("%.2f KB", srcFile.length() / 1024.0));
            item.put("status", "已同步上传");

            // 头部插入，最新在最前
            capturedImages.add(0, item);
            processedFileNames.add(srcFile.getName());
            log.info("成功捕获半轴{}并推送到前端: {} -> {}", isVideoFile ? "视频" : "图片", srcFile.getName(), accessUrl);
        } catch (Exception e) {
            log.error("处理捕获媒体文件失败:", e);
        }
    }

    private boolean isSupportedMedia(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".bmp")
                || lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov") || lower.endsWith(".mkv")
                || lower.endsWith(".flv") || lower.endsWith(".webm") || lower.endsWith(".wmv");
    }

    private boolean isVideo(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov") || lower.endsWith(".mkv")
                || lower.endsWith(".flv") || lower.endsWith(".webm") || lower.endsWith(".wmv");
    }

    @PreDestroy
    public void cleanup() {
        stopWatch();
    }
}
