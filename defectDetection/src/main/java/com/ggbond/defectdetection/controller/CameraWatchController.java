package com.ggbond.defectdetection.controller;

import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.service.CameraFolderWatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cameraWatch")
public class CameraWatchController {

    @Autowired
    private CameraFolderWatchService cameraFolderWatchService;

    /**
     * 启动监听
     */
    @PostMapping("/start")
    public Result<String> startWatch(@RequestParam(required = false) String watchPath) {
        return cameraFolderWatchService.startWatch(watchPath);
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
     * 清空捕获图片
     */
    @PostMapping("/clear")
    public Result<String> clearList(@RequestParam(defaultValue = "false") boolean deletePhysical) {
        return cameraFolderWatchService.clearImages(deletePhysical);
    }
}
