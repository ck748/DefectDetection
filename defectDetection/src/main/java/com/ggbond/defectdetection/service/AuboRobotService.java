package com.ggbond.defectdetection.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    // ==================== 7 个示教点位（单位：度） ====================

    /**
     * 7 个扫描点位，从左到右 (P1~P7)
     * 物体宽度 750mm，等距分布
     */
    private static final double[][] SCAN_POSITIONS = {
        { 23.67,  39.18, -106.27,  -59.55,  -96.00,  12.54},  // P1  X=-486.97mm
        { 29.87,  31.71, -123.56,  -68.70,  -96.41,  18.76},  // P2  X=-361.29mm
        { 37.58,  27.01, -135.29,  -74.84,  -96.81,  26.51},  // P3  X=-255.19mm
        { 47.79,  24.02, -143.35,  -78.66,  -97.15,  36.80},  // P4  X=-156.30mm
        { 59.97,  22.43, -147.46,  -79.64,  -97.26,  49.07},  // P5  X= -67.70mm
        { 74.22,  21.84, -148.01,  -77.80,  -96.97,  63.44},  // P6  X=  19.15mm
        { 98.78,  23.65, -141.17,  -70.05,  -95.49,  88.09},  // P7  X= 165.06mm
    };

    /** 扫描运行状态 */
    private volatile boolean scanning = false;
    private volatile boolean scanStopped = false;

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
            "    local robot = require('aubo').sched.select_robot(1)\n" +
            "    robot.set%s(%d, %s)\n" +
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
            sb.append(String.format("%.6f", jointAngles[i]));
        }
        sb.append("}\n");
        sb.append(String.format("    moveJoint(target, %.4f, %.4f, 0.0, 0)\n", velocity, acceleration));
        sb.append("end");
        return sendScript(sb.toString());
    }

    /**
     * 关节运动到指定角度（度数版本）
     * @param jointAnglesDeg 6个关节角度（度）
     * @param velocity 关节速度（rad/s）
     * @param acceleration 关节加速度（rad/s²）
     */
    public boolean moveJointDeg(double[] jointAnglesDeg, double velocity, double acceleration) {
        double[] radians = new double[6];
        for (int i = 0; i < 6; i++) {
            radians[i] = Math.toRadians(jointAnglesDeg[i]);
        }
        return moveJoint(radians, velocity, acceleration);
    }

    /**
     * 直线运动到指定位姿
     * @param pose {x, y, z, rx, ry, rz} 单位：米/弧度
     */
    public boolean moveLine(double[] pose, double velocity, double acceleration) {
        StringBuilder sb = new StringBuilder();
        sb.append("return function(api)\n");
        sb.append("    local robot = require('aubo').sched.select_robot(1)\n");
        sb.append("    local target = {");
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(", ");
            sb.append(pose[i]);
        }
        sb.append("}\n");
        sb.append(String.format("    robot.moveLine(target, %.4f, %.4f, 0.0, 0)\n", velocity, acceleration));
        sb.append("end");
        return sendScript(sb.toString());
    }

    /**
     * 设置速度比例（0.0 ~ 1.0）
     */
    public boolean setSpeedFraction(double fraction) {
        String script = String.format(
            "return function(api)\n" +
            "    local robot = require('aubo').sched.select_robot(1)\n" +
            "    robot.setSpeedFraction(%.2f)\n" +
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
            "    local robot = require('aubo').sched.select_robot(1)\n" +
            "    robot.stopMotion()\n" +
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
            "    local robot = require('aubo').sched.select_robot(1)\n" +
            "    robot.waitDI(%d, %s, %d)\n" +
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
        // P2 点位（度数）
        double[] photoAngles = {29.87, 31.71, -123.56, -68.70, -96.41, 18.76};
        return moveJointDeg(photoAngles, 1.0, 1.0);
    }

    /**
     * 拍照完成后的复位动作
     */
    public boolean moveToHomePosition() {
        // 自定义原点（度数）
        double[] homeAngles = {83.82, -1.02, -106.27, -16.36, -96.00, 12.54};
        return moveJointDeg(homeAngles, 1.0, 1.0);
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

    // ==================== 7 点位扫描 ====================

    /**
     * 执行 7 点位扫描（左→右→左→右→左，共 28 次拍照）
     * 每到一个点位：移动 → 等待稳定 → 等待相机拍照 → 下一个点位
     *
     * 扫描顺序（4 轮）：
     *   第1轮: P1→P2→P3→P4→P5→P6→P7  (7次)
     *   第2轮: P7→P6→P5→P4→P3→P2→P1  (7次)
     *   第3轮: P1→P2→P3→P4→P5→P6→P7  (7次)
     *   第4轮: P7→P6→P5→P4→P3→P2→P1  (7次)
     *
     * @param settleMs   每个点位稳定等待时间(ms)，默认 1500
     * @param cameraWait 等待相机拍照时间(ms)，默认 2000
     * @return 扫描结果摘要
     */
    public String executeScanPattern(int settleMs, int cameraWait) {
        if (scanning) {
            return "扫描正在运行中，请勿重复启动";
        }
        if (!isConnected()) {
            return "机械臂未连接";
        }

        scanning = true;
        scanStopped = false;
        int totalPhotos = 0;
        List<String> scanLog = new ArrayList<>();

        try {
            // 4 轮扫描：奇数轮左→右，偶数轮右→左
            for (int round = 1; round <= 4; round++) {
                if (scanStopped) break;

                boolean leftToRight = (round % 2 == 1);
                scanLog.add(String.format("===== 第%d轮 (%s) =====", round, leftToRight ? "左→右" : "右→左"));
                log.info("[扫描] 第{}轮开始 ({})", round, leftToRight ? "左→右" : "右→左");

                for (int i = 0; i < 7; i++) {
                    if (scanStopped) break;

                    int idx = leftToRight ? i : (6 - i);
                    double[] pos = SCAN_POSITIONS[idx];
                    String posName = "P" + (idx + 1);

                    scanLog.add(String.format("[%d] 移动到 %s (J1=%.1f° J2=%.1f° J3=%.1f° J4=%.1f° J5=%.1f° J6=%.1f°)",
                            totalPhotos + 1, posName, pos[0], pos[1], pos[2], pos[3], pos[4], pos[5]));
                    log.info("[扫描] 第{}次拍照 -> {}", totalPhotos + 1, posName);

                    // 1. 移动到目标点位（度数转弧度）
                    boolean moved = moveJointDeg(pos, 1.0, 1.0);
                    if (!moved) {
                        scanLog.add("[ERROR] 移动失败: " + posName);
                        log.error("[扫描] 移动到 {} 失败", posName);
                        break;
                    }

                    // 2. 等待机械臂稳定（消除残余振动）
                    try { Thread.sleep(settleMs); } catch (InterruptedException e) { break; }

                    // 3. 设置 DO0=ON 通知外部"已到位，可以拍照"
                    setDO(0, true);

                    // 4. 等待相机拍照完成
                    try { Thread.sleep(cameraWait); } catch (InterruptedException e) { break; }

                    // 5. 复位 DO0
                    setDO(0, false);

                    totalPhotos++;
                    scanLog.add(String.format("[%d] %s 拍照完成 ✓", totalPhotos, posName));
                    log.info("[扫描] 第{}次拍照完成 -> {}", totalPhotos, posName);
                }
            }

            // 扫描结束，回到原位
            if (!scanStopped) {
                scanLog.add("===== 扫描完成，共 " + totalPhotos + " 次拍照 =====");
                log.info("[扫描] 完成，共{}次拍照", totalPhotos);
                moveToHomePosition();
            } else {
                scanLog.add("===== 扫描被中止，已完成 " + totalPhotos + " 次拍照 =====");
                log.info("[扫描] 被中止，已完成{}次拍照", totalPhotos);
            }

        } catch (Exception e) {
            scanLog.add("[ERROR] 扫描异常: " + e.getMessage());
            log.error("[扫描] 异常: {}", e.getMessage(), e);
        } finally {
            scanning = false;
            scanStopped = false;
        }

        return String.join("\n", scanLog);
    }

    /** 停止扫描 */
    public void stopScan() {
        scanStopped = true;
        log.info("[扫描] 收到停止信号");
    }

    /** 是否正在扫描 */
    public boolean isScanning() {
        return scanning;
    }
}
