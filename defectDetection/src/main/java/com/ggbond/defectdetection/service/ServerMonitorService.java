package com.ggbond.defectdetection.service;

import com.ggbond.defectdetection.pojo.vo.SystemMonitorVO;

/**
 * 服务器数据获取接口
 */
public interface ServerMonitorService {

    /**
     * 获取系统状态监控数据
     * @return SystemMonitorVO
     */
    SystemMonitorVO getSystemMonitorData();
}
