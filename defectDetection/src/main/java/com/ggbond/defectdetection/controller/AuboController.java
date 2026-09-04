package com.ggbond.defectdetection.controller;

import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.service.AuboRobotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AUBO 机械臂控制接口（方案A：TCP 发送 Lua 脚本）
 */
@RestController
@Slf4j
@RequestMapping("/aubo")
public class AuboController {

    @Autowired
    private AuboRobotService auboRobotService;

    /** 连接机械臂控制器 */
    @PostMapping("/connect")
    public Result connect(@RequestBody(required = false) Map<String, Object> body) {
        try {
            String host = body != null && body.containsKey("host") ? (String) body.get("host") : null;
            Integer port = body != null && body.containsKey("port") ? Integer.parseInt(String.valueOf(body.get("port"))) : null;

            boolean ok;
            if (host != null && port != null) {
                ok = auboRobotService.connect(host, port);
            } else {
                ok = auboRobotService.connect();
            }
            return ok ? Result.success("机械臂已连接") : Result.fail("机械臂连接失败");
        } catch (Exception e) {
            log.error("机械臂连接失败: {}", e.getMessage(), e);
            return Result.fail("连接失败: " + e.getMessage());
        }
    }

    /** 断开机械臂 */
    @PostMapping("/disconnect")
    public Result disconnect() {
        auboRobotService.disconnect();
        return Result.success("机械臂已断开");
    }

    /** 连接状态 */
    @GetMapping("/status")
    public Result status() {
        return Result.success("查询成功", Map.of("connected", auboRobotService.isConnected()));
    }

    /** 发送自定义 Lua 脚本 */
    @PostMapping("/script")
    public Result sendScript(@RequestBody Map<String, String> body) {
        String script = body.get("script");
        if (script == null || script.isEmpty()) {
            return Result.fail("脚本内容为空");
        }
        boolean ok = auboRobotService.sendScript(script);
        return ok ? Result.success("脚本已发送") : Result.fail("脚本发送失败");
    }

    /** 设置 DO 输出 */
    @PostMapping("/setDO")
    public Result setDO(@RequestBody Map<String, Object> body) {
        int index = Integer.parseInt(String.valueOf(body.get("index")));
        boolean value = Boolean.parseBoolean(String.valueOf(body.get("value")));
        boolean ok = auboRobotService.setDO(index, value);
        return ok ? Result.success(String.format("DO%d = %s", index, value)) : Result.fail("设置失败");
    }

    /** 设置速度比例 */
    @PostMapping("/setSpeed")
    public Result setSpeed(@RequestBody Map<String, Object> body) {
        double fraction = Double.parseDouble(String.valueOf(body.get("fraction")));
        boolean ok = auboRobotService.setSpeedFraction(fraction);
        return ok ? Result.success("速度已设置") : Result.fail("设置失败");
    }

    /** 关节运动 */
    @PostMapping("/moveJoint")
    public Result moveJoint(@RequestBody Map<String, Object> body) {
        try {
            double[] angles = new double[6];
            for (int i = 0; i < 6; i++) {
                angles[i] = Double.parseDouble(String.valueOf(body.get("j" + (i + 1))));
            }
            double velocity = body.containsKey("velocity") ? Double.parseDouble(String.valueOf(body.get("velocity"))) : 1.0;
            double acceleration = body.containsKey("acceleration") ? Double.parseDouble(String.valueOf(body.get("acceleration"))) : 1.0;
            boolean ok = auboRobotService.moveJoint(angles, velocity, acceleration);
            return ok ? Result.success("关节运动指令已发送") : Result.fail("运动指令发送失败");
        } catch (Exception e) {
            return Result.fail("参数错误: " + e.getMessage());
        }
    }

    /** 直线运动 */
    @PostMapping("/moveLine")
    public Result moveLine(@RequestBody Map<String, Object> body) {
        try {
            double[] pose = new double[6];
            String[] keys = {"x", "y", "z", "rx", "ry", "rz"};
            for (int i = 0; i < 6; i++) {
                pose[i] = Double.parseDouble(String.valueOf(body.get(keys[i])));
            }
            double velocity = body.containsKey("velocity") ? Double.parseDouble(String.valueOf(body.get("velocity"))) : 1.0;
            double acceleration = body.containsKey("acceleration") ? Double.parseDouble(String.valueOf(body.get("acceleration"))) : 1.0;
            boolean ok = auboRobotService.moveLine(pose, velocity, acceleration);
            return ok ? Result.success("直线运动指令已发送") : Result.fail("运动指令发送失败");
        } catch (Exception e) {
            return Result.fail("参数错误: " + e.getMessage());
        }
    }

    /** 停止运动 */
    @PostMapping("/stop")
    public Result stop() {
        boolean ok = auboRobotService.stopMotion();
        return ok ? Result.success("已发送停止指令") : Result.fail("停止指令发送失败");
    }

    /** 移动到拍照位置 */
    @PostMapping("/photo/moveToPosition")
    public Result moveToPhotoPosition() {
        boolean ok = auboRobotService.moveToPhotoPosition();
        return ok ? Result.success("已移动到拍照位置") : Result.fail("移动失败");
    }

    /** 回到原位 */
    @PostMapping("/photo/moveToHome")
    public Result moveToHomePosition() {
        boolean ok = auboRobotService.moveToHomePosition();
        return ok ? Result.success("已回到原位") : Result.fail("移动失败");
    }

    /** 执行完整拍照流程（移动→拍照→通知AGV→复位） */
    @PostMapping("/photo/execute")
    public Result executePhotoWorkflow(@RequestBody(required = false) Map<String, Object> body) {
        int agvDoIndex = body != null && body.containsKey("agvDoIndex")
                ? Integer.parseInt(String.valueOf(body.get("agvDoIndex"))) : 0;
        boolean ok = auboRobotService.executePhotoWorkflow(agvDoIndex);
        return ok ? Result.success("拍照流程执行完成") : Result.fail("拍照流程执行失败");
    }
}
