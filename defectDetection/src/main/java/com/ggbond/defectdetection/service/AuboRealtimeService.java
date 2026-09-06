package com.ggbond.defectdetection.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 机械臂目标角度服务
 * <p>
 * 由于 AUBO 控制器未开放实时数据接口，改为显示最后发送的目标关节角度。
 * 当用户通过 Web 发送移动指令时，目标角度会被记录并展示在前端。
 */
@Slf4j
@Service
public class AuboRealtimeService {

    @Autowired
    private AuboRobotService auboRobotService;

    /** 连接状态（只要机械臂连接就为 true） */
    @Getter
    private final java.util.concurrent.atomic.AtomicBoolean connected = new java.util.concurrent.atomic.AtomicBoolean(false);

    /** 最后更新时间戳 */
    @Getter
    private volatile long lastUpdateTime = 0;

    /**
     * 获取最新目标关节角度（弧度）
     */
    public double[] getJointAngles() {
        return auboRobotService.getLastTargetAngles();
    }

    /**
     * 获取最新目标关节角度（度）
     */
    public double[] getJointAnglesDeg() {
        return auboRobotService.getLastTargetAnglesDeg();
    }

    /**
     * 启动服务（标记为已连接）
     */
    public void start() {
        if (auboRobotService.isConnected()) {
            connected.set(true);
            lastUpdateTime = System.currentTimeMillis();
            log.info("[AUBO-RT] 目标角度服务已启动");
        } else {
            connected.set(false);
            log.warn("[AUBO-RT] 机械臂未连接，无法启动");
        }
    }

    /**
     * 停止服务
     */
    public void stop() {
        connected.set(false);
        log.info("[AUBO-RT] 目标角度服务已停止");
    }
}
