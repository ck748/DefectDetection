package com.ggbond.defectdetection.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AUBO 机械臂通信服务（方案A：TCP 发送 Lua 脚本到 30002 端口）
 * <p>
 * 协议说明：
 * - 端口 30002：SCRIPT 协议，发送 Lua 脚本字符串，以 \r\n\r\n 结尾
 * - 端口 30004：RPC 协议（JSON-RPC 2.0），后续方案B使用
 * - 端口 30010：RTDE 协议，实时数据订阅
 */
@Slf4j
@Service
public class AuboRobotService {

    private static final String DEFAULT_HOST = "192.168.1.6";
    private static final int SCRIPT_PORT = 30002;
    private static final int RPC_PORT = 30004;

    private volatile Socket scriptSocket;
    private volatile OutputStream scriptOut;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    /**
     * 连接到机械臂控制器的 SCRIPT 端口（30002）
     */
    public synchronized boolean connect() {
        return connect(DEFAULT_HOST, SCRIPT_PORT);
    }

    public synchronized boolean connect(String host, int port) {
        try {
            disconnect();
            scriptSocket = new Socket(host, port);
            scriptSocket.setSoTimeout(5000);
            scriptOut = scriptSocket.getOutputStream();
            connected.set(true);
            log.info("AUBO 机械臂已连接: {}:{}", host, port);
            return true;
        } catch (Exception e) {
            log.error("AUBO 机械臂连接失败: {}:{}", host, port, e);
            connected.set(false);
            return false;
        }
    }

    /**
     * 断开连接
     */
    public synchronized void disconnect() {
        connected.set(false);
        try {
            if (scriptOut != null) scriptOut.close();
        } catch (Exception ignored) {}
        try {
            if (scriptSocket != null) scriptSocket.close();
        } catch (Exception ignored) {}
        scriptOut = null;
        scriptSocket = null;
        log.info("AUBO 机械臂已断开");
    }

    public boolean isConnected() {
        return connected.get() && scriptSocket != null && scriptSocket.isConnected();
    }

    /**
     * 发送 Lua 脚本到控制器
     * 脚本必须以 \r\n\r\n 结尾
     */
    public synchronized boolean sendScript(String luaScript) {
        if (!isConnected()) {
            log.warn("AUBO 机械臂未连接，无法发送脚本");
            return false;
        }
        try {
            // 脚本以 \r\n\r\n 结尾
            String payload = luaScript.endsWith("\r\n\r\n") ? luaScript : luaScript + "\r\n\r\n";
            scriptOut.write(payload.getBytes(StandardCharsets.UTF_8));
            scriptOut.flush();
            log.info("AUBO 脚本已发送, 长度: {}", payload.length());
            return true;
        } catch (Exception e) {
            log.error("AUBO 脚本发送失败: {}", e.getMessage(), e);
            connected.set(false);
            return false;
        }
    }

    // ==================== 常用 Lua 脚本封装 ====================

    /**
     * 设置数字输出 DO（通用 I/O）
     * @param index DO 编号（0-15 对应 DO00-DO07, DO10-DO17）
     * @param value true=高电平(24V), false=低电平(0V)
     */
    public boolean setDO(int index, boolean value) {
        String script = String.format(
            "return function(api)\n" +
            "    local _ENV = require('aubo').sched.select_robot(1)\n" +
            "    setDO(%d, %s)\n" +
            "end",
            index, value ? "true" : "false"
        );
        return sendScript(script);
    }

    /**
     * 设置数字输出 DO（安全 I/O / 可配置 I/O）
     * @param prefix "DO"=通用, "CO"=可配置
     */
    public boolean setDigitalOutput(String prefix, int index, boolean value) {
        String script = String.format(
            "return function(api)\n" +
            "    local _ENV = require('aubo').sched.select_robot(1)\n" +
            "    set%s(%d, %s)\n" +
            "end",
            prefix, index, value ? "true" : "false"
        );
        return sendScript(script);
    }

    /**
     * 关节运动到指定角度
     * @param jointAngles 6个关节角度（弧度）
     * @param velocity 关节速度（rad/s）
     * @param acceleration 关节加速度（rad/s²）
     */
    public boolean moveJoint(double[] jointAngles, double velocity, double acceleration) {
        StringBuilder sb = new StringBuilder();
        sb.append("return function(api)\n");
        sb.append("    local _ENV = require('aubo').sched.select_robot(1)\n");
        sb.append("    local target = {");
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(", ");
            sb.append(jointAngles[i]);
        }
        sb.append("}\n");
        sb.append(String.format("    moveJoint(target, %.4f, %.4f, 0.0, 0)\n", velocity, acceleration));
        sb.append("end");
        return sendScript(sb.toString());
    }

    /**
     * 直线运动到指定位姿
     * @param pose {x, y, z, rx, ry, rz} 单位：米/弧度
     */
    public boolean moveLine(double[] pose, double velocity, double acceleration) {
        StringBuilder sb = new StringBuilder();
        sb.append("return function(api)\n");
        sb.append("    local _ENV = require('aubo').sched.select_robot(1)\n");
        sb.append("    local target = {");
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(", ");
            sb.append(pose[i]);
        }
        sb.append("}\n");
        sb.append(String.format("    moveLine(target, %.4f, %.4f, 0.0, 0)\n", velocity, acceleration));
        sb.append("end");
        return sendScript(sb.toString());
    }

    /**
     * 设置速度比例（0.0 ~ 1.0）
     */
    public boolean setSpeedFraction(double fraction) {
        String script = String.format(
            "return function(api)\n" +
            "    local _ENV = require('aubo').sched.select_robot(1)\n" +
            "    setSpeedFraction(%.2f)\n" +
            "end", fraction
        );
        return sendScript(script);
    }

    /**
     * 停止机器人运动
     */
    public boolean stopMotion() {
        String script =
            "return function(api)\n" +
            "    local _ENV = require('aubo').sched.select_robot(1)\n" +
            "    stopMotion()\n" +
            "end";
        return sendScript(script);
    }

    /**
     * 等待数字输入 DI 信号
     * @param index DI 编号
     * @param value 等待的值（true/false）
     * @param timeout 超时时间（秒）
     */
    public boolean waitDI(int index, boolean value, int timeout) {
        String script = String.format(
            "return function(api)\n" +
            "    local _ENV = require('aubo').sched.select_robot(1)\n" +
            "    waitDI(%d, %s, %d)\n" +
            "end",
            index, value ? "true" : "false", timeout
        );
        return sendScript(script);
    }

    // ==================== 工作流封装 ====================

    /**
     * 拍照准备动作：机械臂移动到拍照位置
     * TODO: 根据实际机械臂安装位置调整角度
     */
    public boolean moveToPhotoPosition() {
        // 示例：移动到检测区上方的安全位置
        double[] photoPose = {0.0, -0.2, 0.3, 3.14, 0.0, 0.0};
        return moveLine(photoPose, 1.0, 1.0);
    }

    /**
     * 拍照完成后的复位动作
     */
    public boolean moveToHomePosition() {
        double[] homeAngles = {0.0, -0.13, -1.32, 0.38, -1.57, 0.0};
        return moveJoint(homeAngles, 1.0, 1.0);
    }

    /**
     * 完整拍照流程：移动到拍照位 → 触发拍照 → 通知AGV
     * @param agvDoIndex 用于通知AGV的DO编号
     */
    public boolean executePhotoWorkflow(int agvDoIndex) {
        // 步骤1: 移动到拍照位置
        if (!moveToPhotoPosition()) return false;

        // 步骤2: 设置DO信号通知外部（模拟拍照完成）
        // 实际上海康相机到了之后，这里会触发相机拍照
        if (!setDO(agvDoIndex, true)) return false;

        // 步骤3: 等待一小段时间（模拟拍照时间）
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // 步骤4: 复位DO
        if (!setDO(agvDoIndex, false)) return false;

        // 步骤5: 回到原位
        return moveToHomePosition();
    }
}
