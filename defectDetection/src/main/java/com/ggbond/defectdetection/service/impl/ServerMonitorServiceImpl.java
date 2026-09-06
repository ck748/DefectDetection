package com.ggbond.defectdetection.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.ggbond.defectdetection.pojo.vo.SystemMonitorVO;
import com.ggbond.defectdetection.service.ServerMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : poi
 * @project : defectDetection
 * @package : com.ggbond.defectdetection.service.impl
 * @className : ServerMonitorServiceImpl
 * @createTime : 2026/9/6 11:18
 */
@Slf4j
@Service
public class ServerMonitorServiceImpl implements ServerMonitorService {

    private static final String MONITOR_API_URL = "http://192.168.1.3:5000/api/system";

    @Override
    public SystemMonitorVO getSystemMonitorData() {
        try {
            // 发送 GET 请求获取服务器监控数据，设置 3000ms 超时时间防止请求卡死
            String response = HttpUtil.get(MONITOR_API_URL, 3000);
            // 将 JSON 字符串解析为 SystemMonitorVO 对象
            return JSON.parseObject(response, SystemMonitorVO.class);
        } catch (Exception e) {
            log.error("获取服务器监控数据失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
