package com.ggbond.defectdetection.controller;

import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.service.VmCameraTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * VM 相机触发控制接口
 * <p>
 * 管理 Java ↔ VisionMaster 的 TCP 通信连接
 */
@RestController
@Slf4j
@RequestMapping("/vmCamera")
public class VmCameraController {

    @Autowired
    private VmCameraTriggerService vmCameraTriggerService;

    /** 启动 TCP Server 监听 */
    @PostMapping("/start")
    public Result start() {
        try {
            vmCameraTriggerService.startListening();
            return Result.success("VM TCP Server 已启动，端口: " + vmCameraTriggerService.getTcpPort());
        } catch (Exception e) {
            log.error("启动 VM TCP Server 失败: {}", e.getMessage(), e);
            return Result.fail("启动失败: " + e.getMessage());
        }
    }

    /** 停止 TCP Server */
    @PostMapping("/stop")
    public Result stop() {
        try {
            vmCameraTriggerService.stopListening();
            return Result.success("VM TCP Server 已停止");
        } catch (Exception e) {
            log.error("停止 VM TCP Server 失败: {}", e.getMessage(), e);
            return Result.fail("停止失败: " + e.getMessage());
        }
    }

    /** 测试触发拍照 */
    @PostMapping("/trigger")
    public Result trigger(@RequestBody(required = false) Map<String, Object> body) {
        int timeout = 10;
        if (body != null && body.containsKey("timeout")) {
            timeout = Integer.parseInt(String.valueOf(body.get("timeout")));
        }

        boolean ok = vmCameraTriggerService.triggerCapture(timeout);
        return ok ? Result.success("拍照完成") : Result.fail("拍照超时或通信失败");
    }

    /** 连接状态 */
    @GetMapping("/status")
    public Result status() {
        return Result.success("查询成功", Map.of(
                "listening", vmCameraTriggerService.isListening(),
                "connected", vmCameraTriggerService.isConnected(),
                "port", vmCameraTriggerService.getTcpPort()
        ));
    }
}
