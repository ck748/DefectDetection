package com.ggbond.defectdetection.controller;

import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.CameraWatchRecord;
import com.ggbond.defectdetection.service.CameraFolderWatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cameraWatch")
public class CameraWatchController {

    @Autowired
    private CameraFolderWatchService cameraFolderWatchService;

    /**
     * 接收客户端自动监听并上传的米家拍照图片
     * 对应客户端 POST http://192.168.1.3:8081/cameraWatch/upload
     */
    @PostMapping("/upload")
    public Result<CameraWatchRecord> uploadCameraImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "fileName", required = false) String fileName
    ) {
        return cameraFolderWatchService.saveUploadedImage(image, fileName);
    }

    /**
     * 启动监听 / 应用切换服务器端存储目录
     * 兼容 Query Param 以及 JSON Body 传参
     */
    @PostMapping("/start")
    public Result<String> startWatch(
            @RequestParam(required = false) String watchPath,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String path = watchPath;
        if ((path == null || path.trim().isEmpty()) && body != null) {
            path = body.get("watchPath");
        }
        return cameraFolderWatchService.startWatch(path);
    }

    /**
     * 停止监听
     */
    @PostMapping("/stop")
    public Result<String> stopWatch() {
        return cameraFolderWatchService.stopWatch();
    }

    /**
     * 获取当前状态及已捕获图片列表
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        return Result.success("获取成功", cameraFolderWatchService.getStatusAndImages());
    }

    /**
     * 删除单张图片（支持物理删除）
     */
    @PostMapping("/delete")
    public Result<String> deleteImage(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null || !body.containsKey("id")) {
            return Result.fail("参数错误，缺失图片ID");
        }

        Object idObj = body.get("id");
        Integer id = null;
        if (idObj instanceof Number) {
            id = ((Number) idObj).intValue();
        } else if (idObj != null) {
            try {
                id = Integer.parseInt(idObj.toString());
            } catch (NumberFormatException ignored) {}
        }

        boolean deleteSourceFile = false;
        if (body.containsKey("deleteSourceFile")) {
            deleteSourceFile = Boolean.parseBoolean(body.get("deleteSourceFile").toString());
        }

        return cameraFolderWatchService.deleteImage(id, deleteSourceFile);
    }

    /**
     * 清空捕获图片
     */
    @PostMapping("/clear")
    public Result<String> clearList(@RequestParam(defaultValue = "false") boolean deletePhysical) {
        return cameraFolderWatchService.clearImages(deletePhysical);
    }
}
