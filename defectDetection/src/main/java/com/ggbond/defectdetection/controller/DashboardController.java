package com.ggbond.defectdetection.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.dto.DashboardInfoDto;
import com.ggbond.defectdetection.dto.DetectResDto;
import com.ggbond.defectdetection.pojo.Defection;
import com.ggbond.defectdetection.pojo.DetectLog;
import com.ggbond.defectdetection.pojo.SysLog;
import com.ggbond.defectdetection.service.DefectionService;
import com.ggbond.defectdetection.service.DetectLogService;
import com.ggbond.defectdetection.service.SysLogService;
import com.ggbond.defectdetection.software.data.DataModule;
import com.ggbond.defectdetection.software.image.ImageModule;
import com.ggbond.defectdetection.util.ImgUtil;
import com.ggbond.defectdetection.util.SseUtil;
import com.ggbond.defectdetection.util.SystemStatusUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: 19461
 * Date: 2024/2/23
 */
@RestController
@Slf4j
@ResponseBody
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    SseUtil sseUtil;
    
    @Autowired
    DetectLogService detectLogService;
    
    @Autowired
    DefectionService defectionService;
    
    @Autowired
    SysLogService sysLogService;

    /**
     * 获取指定目录下的所有图片并转为 Base64 数组
     */
    @GetMapping("/local-images")
    public Result<List<String>> getLocalImages() {
        String directoryPath = "/root/desc/cmzj-main/defectDetection/detectPicture/2";
        File dir = new File(directoryPath);
        List<String> base64Images = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) {
            log.warn("指定的图片目录不存在或不是一个目录: {}", directoryPath);
            return Result.success("目录不存在", base64Images);
        }

        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".bmp");
        });

        if (files != null) {
            // 可选：按修改时间或文件名排序
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            
            for (File file : files) {
                try {
                    String base64 = ImgUtil.imageToBase64ByPath(file.getAbsolutePath());
                    // 加上 data:image 前缀以便前端直接渲染
                    String prefix = "data:image/jpeg;base64,";
                    if (file.getName().toLowerCase().endsWith(".png")) {
                        prefix = "data:image/png;base64,";
                    }
                    base64Images.add(prefix + base64);
                } catch (Exception e) {
                    log.error("读取图片文件转 Base64 失败: {}", file.getAbsolutePath(), e);
                }
            }
        }

        return Result.success("获取成功", base64Images);
    }

    @GetMapping(value="/data")
    public Result<Map<String, Object>> getDashboardData(HttpSession httpSession) {
        log.info("接收到Dashboard数据请求");
        
        try {
            // 1. 从数据库获取最新的检测结果
            DetectResDto resDto = getLatestDetectResult();
            
            // 2. 获取统计数据
            int runTime = SystemStatusUtil.getContinuousWorkingSeconds();
            int defectionsSum = DataModule.getTotalDefectionsNum();
            double defectRate = DataModule.getDefectiveRate();
            String highestOccurrenceDefect = DataModule.getHighestOccurrenceDefect();
            
            // 3. 查询最新的5条系统操作记录
            LambdaQueryWrapper<SysLog> logLqw = new LambdaQueryWrapper<>();
            logLqw.orderByDesc(SysLog::getOpTime);
            logLqw.last("LIMIT 5");
            java.util.List<SysLog> latestOperations = sysLogService.list(logLqw);
            
            // 4. 封装数据
            Map<String, Object> data = new HashMap<>();
            data.put("imgBase64", resDto.getImgBase64());
            data.put("defections", resDto.getDefections());
            data.put("defectionsSum", resDto.getDefectionsSum());
            data.put("runTime", runTime);
            data.put("defectRate", defectRate);
            data.put("highestOccurrenceDefect", highestOccurrenceDefect);
            data.put("latestOperations", latestOperations);
            
            return Result.success("成功", data);
        } catch (Exception e) {
            log.error("获取Dashboard数据失败", e);
            return Result.fail("获取数据失败");
        }
    }

    @GetMapping(value="/pictureInfo")
    public SseEmitter flushPictureHandler(HttpSession httpSession){

        log.info("接收到Dashboard SSE请求");
        
        // 安全获取userId，避免空指针异常
        Object userObj = httpSession.getAttribute("user");
        int userId;
        if (userObj != null) {
            userId = (int) userObj;
        } else {
            log.warn("Session中没有user属性，使用默认userId=1");
            userId = 1; // 使用默认值
        }

      
        
        // 1. 从数据库获取最新的检测结果
        DetectResDto resDto = getLatestDetectResult();
        
        // 2. 获取统计数据
        int runTime = SystemStatusUtil.getContinuousWorkingSeconds();
        int defectionsSum = DataModule.getTotalDefectionsNum();
        double defectRate = DataModule.getDefectiveRate();
        String highestOccurrenceDefect = DataModule.getHighestOccurrenceDefect();
        
        // 3. 查询最新的5条系统操作记录
        LambdaQueryWrapper<SysLog> logLqw = new LambdaQueryWrapper<>();
        logLqw.orderByDesc(SysLog::getOpTime);
        logLqw.last("LIMIT 5");
        java.util.List<SysLog> latestOperations = sysLogService.list(logLqw);
        
        DashboardInfoDto dashboardInfoDto = new DashboardInfoDto(
            runTime, 
            defectionsSum, 
            defectRate, 
            highestOccurrenceDefect, 
            latestOperations
        );
        
        log.info("📊 数据准备完成: 运行时长={}s, 缺陷总数={}, 缺陷率={}, 最新操作数={}", 
            runTime, defectionsSum, defectRate, latestOperations.size());

        // 4. 现在建立SSE连接
        SseEmitter sseEmitter = sseUtil.connect((long) userId);
        
        // 4.5. 立即发送一条初始化消息，确保EventSource认为连接成功
        try {
            sseEmitter.send(SseEmitter.event()
                .comment("连接已建立")
            );
            log.info("✅ SSE初始化消息发送成功");
        } catch (Exception e) {
            log.error("发送SSE初始化消息失败", e);
        }
        
        // 5. 稍微延迟后发送数据（给EventSource时间进入OPEN状态）
        new Thread(() -> {
            try {
                Thread.sleep(100); // 等待100毫秒
                
                // 发送检测结果
                boolean success1 = sseUtil.sendMessage((long)userId, String.valueOf(Result.IMAGE_CODE), resDto);
                if (success1) {
                    log.info("✅ 检测结果发送成功");
                } else {
                    log.warn("⚠️ 检测结果发送失败");
                }
                
                // 发送统计信息
                boolean success2 = sseUtil.sendMessage((long)userId, String.valueOf(Result.IMAGE_CODE), dashboardInfoDto);
                if (success2) {
                    log.info("✅ 统计信息发送成功");
                } else {
                    log.warn("⚠️ 统计信息发送失败");
                }
            } catch (Exception e) {
                log.error("发送SSE消息失败", e);
            }
        }).start();

        return sseEmitter;
    }
    
    /**
     * 从数据库获取最新的检测结果
     */
    private DetectResDto getLatestDetectResult() {
        DetectResDto result = new DetectResDto();
        
        try {
            // 查询最新的检测记录
            LambdaQueryWrapper<DetectLog> lqw = new LambdaQueryWrapper<>();
            lqw.orderByDesc(DetectLog::getTime);
            lqw.last("LIMIT 1");
            DetectLog latestLog = detectLogService.getOne(lqw);
            
            if (latestLog != null) {
                // 读取图片
                try {
                    String imgBase64 = ImgUtil.imageToBase64ByPath(latestLog.getStoragePath());
                    result.setImgBase64(imgBase64);
                } catch (Exception e) {
                    log.warn("读取检测图片失败: {}", latestLog.getStoragePath(), e);
                    result.setImgBase64("");
                }
                
                // 查询该检测的所有缺陷
                LambdaQueryWrapper<Defection> defLqw = new LambdaQueryWrapper<>();
                defLqw.eq(Defection::getDetectId, latestLog.getId());
                java.util.List<Defection> defections = defectionService.list(defLqw);
                
                result.setDefections(defections);
                result.setDefectionsSum(defections.size());
                
                log.info("从数据库加载最新检测结果，ID: {}, 缺陷数: {}", latestLog.getId(), defections.size());
            } else {
                // 没有检测记录，返回空数据
                log.info("数据库中没有检测记录，返回初始化数据");
                result.setImgBase64("");
                result.setDefections(new java.util.ArrayList<>());
                result.setDefectionsSum(0);
            }
        } catch (Exception e) {
            log.error("获取最新检测结果失败", e);
            result.setImgBase64("");
            result.setDefections(new java.util.ArrayList<>());
            result.setDefectionsSum(0);
        }
        
        return result;
    }


}