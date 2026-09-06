package com.ggbond.defectdetection.controller;

import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.vo.SystemMonitorVO;
import com.ggbond.defectdetection.service.ServerMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : poi
 * @project : defectDetection
 * @package : com.ggbond.defectdetection.controller
 * @className : ServerMonitorController
 * @createTime : 2026/9/6 11:23
 */
@RestController
@RequestMapping("/server/monitor")
@Slf4j
public class ServerMonitorController {

    @Autowired
    private ServerMonitorService serverMonitorService;

    /**
     * 获取系统状态监控数据
     * @return Result<SystemMonitorVO>
     */
    @GetMapping("/info")
    public Result<SystemMonitorVO> getSystemMonitorInfo() {
        log.info("接收到获取服务器监控数据请求");
        SystemMonitorVO monitorData = serverMonitorService.getSystemMonitorData();
        
        if (monitorData != null) {
            return Result.success("获取服务器监控数据成功", monitorData);
        } else {
            return Result.fail("获取服务器监控数据失败");
        }
    }
}
