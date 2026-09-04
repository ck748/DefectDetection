package com.ggbond.defectdetection.controller;

import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AGV + 机械臂协调工作流控制接口
 */
@RestController
@Slf4j
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    /** 获取工作流状态 */
    @GetMapping("/status")
    public Result status() {
        return Result.success("查询成功", workflowService.getStatus());
    }

    /**
     * 启动工作流
     * 参数（可选）：
     * - station6: 检测区站号，默认 6
     * - station3: 分拣区站号，默认 3
     * - robotDoIndex: 机械臂 DO 编号，默认 0
     */
    @PostMapping("/start")
    public Result start(@RequestBody(required = false) Map<String, Object> body) {
        try {
            int station6 = body != null && body.containsKey("station6")
                    ? Integer.parseInt(String.valueOf(body.get("station6"))) : 6;
            int station3 = body != null && body.containsKey("station3")
                    ? Integer.parseInt(String.valueOf(body.get("station3"))) : 3;
            int robotDoIndex = body != null && body.containsKey("robotDoIndex")
                    ? Integer.parseInt(String.valueOf(body.get("robotDoIndex"))) : 0;

            workflowService.startWorkflow(station6, station3, robotDoIndex);
            return Result.success("工作流已启动");
        } catch (Exception e) {
            log.error("启动工作流失败: {}", e.getMessage(), e);
            return Result.fail("启动失败: " + e.getMessage());
        }
    }

    /** 停止工作流 */
    @PostMapping("/stop")
    public Result stop() {
        workflowService.stopWorkflow();
        return Result.success("工作流已停止");
    }

    /** 重置工作流 */
    @PostMapping("/reset")
    public Result reset() {
        workflowService.resetWorkflow();
        return Result.success("工作流已重置");
    }
}
