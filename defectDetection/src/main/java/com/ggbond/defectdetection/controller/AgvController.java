package com.ggbond.defectdetection.controller;

import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.service.AgvSerialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * AGV 小车控制接口：串口由后端独占，前端通过 REST + SSE 交互
 */
@RestController
@Slf4j
@RequestMapping("/agv")
public class AgvController {

    @Autowired
    private AgvSerialService agvSerialService;

    /** 串口列表 */
    @GetMapping("/ports")
    public Result ports() {
        try {
            List<Map<String, Object>> ports = agvSerialService.listPorts();
            return Result.success("加载成功", ports);
        } catch (Exception e) {
            log.error("获取串口列表失败: {}", e.getMessage(), e);
            return Result.fail("获取串口列表失败: " + e.getMessage());
        }
    }

    /** 连接串口（不传端口名则取第一个可用串口） */
    @PostMapping("/connect")
    public Result connect(@RequestBody(required = false) Map<String, Object> body) {
        try {
            String name = body == null ? null : (String) body.get("portName");
            if (name == null || name.isEmpty()) {
                List<Map<String, Object>> ports = agvSerialService.listPorts();
                if (ports.isEmpty()) {
                    return Result.fail("未找到可用串口");
                }
                name = String.valueOf(ports.get(0).get("name"));
            }
            agvSerialService.connect(name);
            return Result.success("串口已连接", agvSerialService.snapshot());
        } catch (Exception e) {
            log.error("串口连接失败: {}", e.getMessage(), e);
            return Result.fail("串口连接失败: " + e.getMessage());
        }
    }

    /** 断开串口 */
    @PostMapping("/disconnect")
    public Result disconnect() {
        try {
            agvSerialService.disconnect();
            return Result.success("串口已断开");
        } catch (Exception e) {
            log.error("串口断开失败: {}", e.getMessage(), e);
            return Result.fail("串口断开失败: " + e.getMessage());
        }
    }

    /** 当前连接与最新状态 */
    @GetMapping("/status")
    public Result status() {
        return Result.success("加载成功", agvSerialService.snapshot());
    }

    /**
     * 发送指令：{sub, p1, p2} 对应控制帧 A5 5A 08 23 sub p1 p2；
     * {"query": true} 发送状态查询帧
     */
    @PostMapping("/command")
    public Result command(@RequestBody Map<String, Object> body) {
        try {
            if (!agvSerialService.isConnected()) {
                return Result.fail("串口未连接");
            }
            boolean ok;
            if (Boolean.TRUE.equals(body.get("query"))) {
                ok = agvSerialService.sendQuery();
            } else {
                int sub = toInt(body.get("sub"));
                int p1 = toInt(body.get("p1"));
                int p2 = toInt(body.get("p2"));
                ok = agvSerialService.sendCommand(sub, p1, p2);
            }
            return ok ? Result.success("指令已发送") : Result.fail("指令发送失败");
        } catch (Exception e) {
            log.error("AGV 指令发送失败: {}", e.getMessage(), e);
            return Result.fail("指令发送失败: " + e.getMessage());
        }
    }

    /** 状态推送（SSE），事件名 status */
    @GetMapping(value = "/status/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return agvSerialService.subscribe();
    }

    private int toInt(Object o) {
        return o == null ? 0 : Integer.parseInt(String.valueOf(o));
    }
}
