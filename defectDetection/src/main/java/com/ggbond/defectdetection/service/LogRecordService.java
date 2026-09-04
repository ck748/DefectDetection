package com.ggbond.defectdetection.service;

import com.ggbond.defectdetection.common.BaseContext;
import com.ggbond.defectdetection.pojo.Manager;
import com.ggbond.defectdetection.pojo.SysLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 统一业务审计与运行日志记录服务
 */
@Slf4j
@Service
public class LogRecordService {

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private ManagerService managerService;

    /**
     * 写入一条审计/操作日志
     *
     * @param resourceType 资源类型（如：用户管理、项目配置、AI质检模型、缺陷复核、产线硬件、系统报错等）
     * @param operation    操作动作（如：新增、修改、删除、检测、登录、切换等）
     * @param target       操作目标（如：工号、工件SN码、参数项）
     * @param status       状态（成功、失败、告警）
     * @param details      详情描述（具体参数、变化前后对比、报错原因）
     */
    public void record(String resourceType, String operation, String target, String status, String details) {
        try {
            SysLog sysLog = new SysLog();
            sysLog.setOpTime(LocalDateTime.now());
            sysLog.setOperation(operation != null ? operation : "操作");
            sysLog.setTarget(target != null ? target : "-");
            sysLog.setResourceType(resourceType != null ? resourceType : "常规业务");
            sysLog.setStatus(status != null ? status : "成功");
            sysLog.setDetails(details != null ? details : "");

            // 自动提取当前请求上下文信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            Integer operatorId = 1; // 默认工号/账号1
            Integer operatorType = 0; // 默认管理员 (0:管理员, 1:操作员, 2:第三方api)
            String clientIp = "127.0.0.1";

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                clientIp = getClientIp(request);

                HttpSession session = request.getSession(false);
                if (session != null && session.getAttribute("user") != null) {
                    Object userObj = session.getAttribute("user");
                    if (userObj instanceof Integer) {
                        operatorId = (Integer) userObj;
                    } else if (userObj instanceof Long) {
                        operatorId = Math.toIntExact((Long) userObj);
                    }
                } else if (BaseContext.getCurrentId() != null) {
                    operatorId = Math.toIntExact(BaseContext.getCurrentId());
                }
            }

            sysLog.setOperator(operatorId);
            sysLog.setOperatorType(operatorType);
            sysLog.setClientIp(clientIp);

            sysLogService.save(sysLog);
            log.info("📝 [业务日志] [{}] {} -> 目标: {}, 状态: {}, 详情: {}", resourceType, operation, target, status, details);
        } catch (Exception e) {
            log.error("写入业务日志失败: {}", e.getMessage());
        }
    }

    public void recordSuccess(String resourceType, String operation, String target, String details) {
        record(resourceType, operation, target, "成功", details);
    }

    public void recordError(String resourceType, String operation, String target, String errorMsg) {
        record(resourceType, operation, target, "失败", errorMsg);
    }

    public void recordWarn(String resourceType, String operation, String target, String warnMsg) {
        record(resourceType, operation, target, "告警", warnMsg);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            try {
                // 动态获取本机真实网卡局域网IP
                java.net.InetAddress local = java.net.InetAddress.getLocalHost();
                if (local != null && local.getHostAddress() != null && !local.getHostAddress().startsWith("127.")) {
                    return local.getHostAddress();
                }
            } catch (Exception ignored) {}
        }
        return ip != null ? ip : "127.0.0.1";
    }
}
