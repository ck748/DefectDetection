package com.ggbond.defectdetection.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AGV + 机械臂协调工作流服务
 * <p>
 * 流程：
 * 1. AGV 从 1 号站出发 → 移动到 6 号站停止
 * 2. AGV 到站后 → 触发机械臂 28 点位扫描（左→右→左→右，4 轮）
 * 3. 扫描完成后 → 机械臂回原位
 * 4. 后端发指令让 AGV 去 3 号站
 * 5. AGV 到 3 号站 → 流程完成
 */
@Slf4j
@Service
public class WorkflowService {

    @Autowired
    private AgvSerialService agvSerialService;

    @Autowired
    private AuboRobotService auboRobotService;

    /** 工作流状态 */
    public enum WorkflowState {
        IDLE,               // 空闲
        AGV_TO_STATION6,    // AGV 前往 6 号站
        SCANNING,           // 机械臂 28 点位扫描中
        AGV_TO_STATION3,    // AGV 前往 3 号站
        COMPLETED,          // 完成
        ERROR               // 异常
    }

    private final AtomicReference<WorkflowState> currentState = new AtomicReference<>(WorkflowState.IDLE);
    private final ExecutorService workflowExecutor = Executors.newSingleThreadExecutor();
    private Future<?> currentWorkflow;

    /** 获取当前工作流状态 */
    public WorkflowState getCurrentState() {
        return currentState.get();
    }

    /** 获取状态文本 */
    public Map<String, Object> getStatus() {
        return Map.of(
            "state", currentState.get().name(),
            "stateText", getStateText(currentState.get()),
            "agvConnected", agvSerialService.isConnected(),
            "robotConnected", auboRobotService.isConnected()
        );
    }

    private String getStateText(WorkflowState state) {
        switch (state) {
            case IDLE: return "空闲，等待启动";
            case AGV_TO_STATION6: return "AGV→6号站（等待到站）";
            case SCANNING: return "机械臂 28 点位扫描中...";
            case AGV_TO_STATION3: return "扫描完成→AGV→3号站";
            case COMPLETED: return "流程完成";
            case ERROR: return "流程异常";
            default: return "未知";
        }
    }

    /**
     * 启动完整工作流
     * @param station6 检测区站号（默认 6）
     * @param station3 分拣区站号（默认 3）
     * @param robotDoIndex 机械臂通知 AGV 的 DO 编号（默认 0）
     */
    public synchronized void startWorkflow(int station6, int station3, int robotDoIndex) {
        if (currentState.get() != WorkflowState.IDLE && currentState.get() != WorkflowState.COMPLETED && currentState.get() != WorkflowState.ERROR) {
            log.warn("工作流正在运行中，当前状态: {}", currentState.get());
            return;
        }

        currentWorkflow = workflowExecutor.submit(() -> {
            try {
                runWorkflow(station6, station3, robotDoIndex);
            } catch (Exception e) {
                log.error("工作流执行异常: {}", e.getMessage(), e);
                currentState.set(WorkflowState.ERROR);
            }
        });
    }

    /** 使用默认参数启动 */
    public void startWorkflow() {
        startWorkflow(6, 3, 0);
    }

    /**
     * 执行工作流
     */
    private void runWorkflow(int station6, int station3, int robotDoIndex) throws Exception {
        log.info("===== 工作流启动 =====");

        // ==================== 步骤1: AGV 前往 6 号站 ====================
        currentState.set(WorkflowState.AGV_TO_STATION6);
        log.info("[步骤1] AGV 前往 {} 号站", station6);
        sendCommandRetry(0x9D, station6, 0x00);

        // 轮询等待 AGV 到站
        if (!waitForAgvArrival(station6, 60)) {
            throw new RuntimeException("AGV 未在 60 秒内到达 " + station6 + " 号站");
        }
        log.info("[步骤1] AGV 已到达 {} 号站，准备启动扫描", station6);

        // ==================== 步骤2: 执行 28 点位扫描 ====================
        currentState.set(WorkflowState.SCANNING);
        log.info("[步骤2] 开始 28 点位扫描（4 轮，左→右→左→右）");

        // 执行扫描（同步阻塞，直到 28 次拍照完成）
        String scanResult = auboRobotService.executeScanPattern(1500, 2000);
        log.info("[步骤2] 扫描完成: {}", scanResult);

        // 扫描完成后机械臂回原位
        log.info("[步骤2] 机械臂回原位");
        if (!auboRobotService.moveToHomePosition()) {
            log.warn("[步骤2] 机械臂复位失败，继续后续流程");
        }

        // ==================== 步骤3: AGV 前往 3 号站 ====================
        currentState.set(WorkflowState.AGV_TO_STATION3);
        log.info("[步骤3] 扫描完成，AGV 前往 {} 号站", station3);
        sendCommandRetry(0x9D, station3, 0x00);

        // 轮询等待 AGV 到站
        if (!waitForAgvArrival(station3, 60)) {
            throw new RuntimeException("AGV 未在 60 秒内到达 " + station3 + " 号站");
        }
        log.info("[步骤3] AGV 已到达 {} 号站", station3);

        // 完成
        currentState.set(WorkflowState.COMPLETED);
        log.info("===== 工作流完成 =====");
    }

    /**
     * 重复发送指令 3 次（RS485 信号不稳定时提高可靠性）
     */
    private void sendCommandRetry(int sub, int p1, int p2) {
        for (int i = 1; i <= 3; i++) {
            agvSerialService.sendCommand(sub, p1, p2);
            log.info("[AGV] 第{}/3次发送: sub=0x{}, p1={}, p2={}", i,
                    Integer.toHexString(sub), p1, p2);
            if (i < 3) {
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * 轮询等待 AGV 到站（先确认已离开起始位，避免误判）
     * @param targetStation 目标站号
     * @param timeoutSeconds 超时时间（秒）
     * @return true=到站, false=超时
     */
    private boolean waitForAgvArrival(int targetStation, int timeoutSeconds) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;
        boolean hasLeftStart = false;  // 是否已离开起始位

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                var snapshot = agvSerialService.snapshot();
                if (snapshot != null && snapshot.get("status") != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> status = (Map<String, Object>) snapshot.get("status");
                    if (status != null) {
                        Object arriveStop = status.get("arriveStop");
                        Object currentStation = status.get("currentStation");
                        log.debug("AGV 状态: arriveStop={}, currentStation={}, target={}", arriveStop, currentStation, targetStation);

                        // 第一步：确认 AGV 已离开起始位（currentStation != targetStation 说明在移动中）
                        if (!hasLeftStart) {
                            if (currentStation instanceof Number && ((Number) currentStation).intValue() != targetStation) {
                                hasLeftStart = true;
                                log.info("AGV 已离开起始位，当前站={}, 目标站={}", currentStation, targetStation);
                            }
                        }

                        // 第二步：确认已到站（arriveStop == 1 且 currentStation == targetStation）
                        if (hasLeftStart
                                && arriveStop instanceof Number && ((Number) arriveStop).intValue() == 1
                                && currentStation instanceof Number && ((Number) currentStation).intValue() == targetStation) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("获取 AGV 状态失败: {}", e.getMessage());
            }
            Thread.sleep(1000);
        }
        log.warn("等待 AGV 到站超时: target={}, hasLeftStart={}", targetStation, hasLeftStart);
        return false;
    }

    /**
     * 停止当前工作流
     */
    public synchronized void stopWorkflow() {
        if (currentWorkflow != null) {
            currentWorkflow.cancel(true);
            currentWorkflow = null;
        }
        currentState.set(WorkflowState.IDLE);
        log.info("工作流已停止");
    }

    /**
     * 重置工作流状态
     */
    public void resetWorkflow() {
        currentState.set(WorkflowState.IDLE);
        log.info("工作流状态已重置");
    }
}
