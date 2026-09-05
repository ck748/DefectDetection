package com.ggbond.defectdetection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.CameraWatchRecord;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

    @Autowired
    private CameraWatchRecordService cameraWatchRecordService;

    private WatchService watchService;
    private ExecutorService executorService;
    private ScheduledExecutorService scanExecutorService;
    private volatile boolean isRunning = false;

    // 联动绑定的本地监听 Node 子进程句柄
    private Process localWatcherProcess;
    private ExecutorService processLogExecutor;

    // 本地快速去重缓存
    private final Set<String> processedFileNames = Collections.synchronizedSet(new HashSet<>());

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    /**
     * 接收客户端（如 mijia-watcher 守护进程）上传的米家拍照图片并入库
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
            record.setStatus("已同步上传");

            cameraWatchRecordService.save(record);
            processedFileNames.add(originalName);

            log.info("📸 米家相机图片成功接收并存盘入库: 原始名={}, 存盘名={}, WebURL={}", originalName, storedName, webUrl);
            return Result.success("图片上传并入库成功", record);
        } catch (Exception e) {
            log.error("保存上传图片失败:", e);
            return Result.fail("保存上传图片失败: " + e.getMessage());
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
                item.put("imgUrl", r.getWebUrl());
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
        String dir = this.serverWatchDir.replace("\\", "/");
        if (dir.contains("uploads/")) {
            String sub = dir.substring(dir.indexOf("uploads/"));
            if (!sub.startsWith("/")) sub = "/" + sub;
            if (!sub.endsWith("/")) sub = sub + "/";
            return sub + storedName;
        }
        return "/uploads/camera_watch/" + storedName;
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

            // 仅对有效普通文件夹进行首层轻量扫描（严禁深层递归；若在Windows本地测试填了根目录，只取顶层直接文件）
            File[] files = folder.listFiles(f -> f != null && f.isFile());
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
                            record.setStatus("已同步");
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

                    if (isImageFile(nameStr)) {
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
