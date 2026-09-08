package com.ggbond.defectdetection.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.dto.DashboardInfoDto;
import com.ggbond.defectdetection.dto.DetectResDto;
import com.ggbond.defectdetection.pojo.Defection;
import com.ggbond.defectdetection.pojo.DetectLog;
import com.ggbond.defectdetection.pojo.DetectionDefectImage;
import com.ggbond.defectdetection.pojo.DetectionImage;
import com.ggbond.defectdetection.pojo.DetectionBatch;
import com.ggbond.defectdetection.pojo.SysLog;
import com.ggbond.defectdetection.mapper.DetectionImageMapper;
import com.ggbond.defectdetection.mapper.DetectionDefectImageMapper;
import com.ggbond.defectdetection.mapper.DetectionBatchMapper;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
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

    @Autowired
    DetectionImageMapper detectionImageMapper;

    @Autowired
    DetectionDefectImageMapper detectionDefectImageMapper;

    @Autowired
    DetectionBatchMapper detectionBatchMapper;

    @GetMapping(value="/data")
    public Result<Map<String, Object>> getDashboardData(HttpSession httpSession) {
        log.info("接收到Dashboard数据请求");
        
        try {
            // 1. 从数据库获取最近的检测结果（轻量级，不含base64，最多28条）
            java.util.List<Map<String, Object>> recentResults = getRecentDetectResults(28);
            
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
            data.put("recentResults", recentResults);
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
                // 读取标注图（有红框）
                try {
                    String imgBase64 = ImgUtil.imageToBase64ByPath(latestLog.getStoragePath());
                    result.setImgBase64(imgBase64);
                } catch (Exception e) {
                    log.warn("读取检测图片失败: {}", latestLog.getStoragePath(), e);
                    result.setImgBase64("");
                }
                
                // 读取原图（无红框），文件名为 storagePath + "_original"
                String originalPath = latestLog.getStoragePath().replaceAll("(\\.[a-zA-Z]+)$", "_original$1");
                try {
                    String originalImgBase64 = ImgUtil.imageToBase64ByPath(originalPath);
                    result.setOriginalImgBase64(originalImgBase64);
                } catch (Exception e) {
                    log.debug("读取原图失败（可能为旧数据）: {}", originalPath);
                    // 原图不存在时，用标注图作为原图
                    result.setOriginalImgBase64(result.getImgBase64());
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

    /**
     * 从数据库获取最近的N条检测结果（轻量级，不含base64图片）
     * 同时查询旧模型（DetectLog）和新模型（DetectionImage）的数据
     */
    private java.util.List<Map<String, Object>> getRecentDetectResults(int limit) {
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        try {
            // ===== 旧模型数据（DetectLog 表）=====
            LambdaQueryWrapper<DetectLog> lqw = new LambdaQueryWrapper<>();
            lqw.orderByDesc(DetectLog::getTime);
            lqw.last("LIMIT " + limit);
            java.util.List<DetectLog> recentLogs = detectLogService.list(lqw);
            
            for (DetectLog detectLog : recentLogs) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", detectLog.getId());
                item.put("name", detectLog.getName());
                item.put("time", detectLog.getTime());
                item.put("storagePath", detectLog.getStoragePath());
                item.put("defectionsSum", detectLog.getDefectionsSum());
                item.put("source", "old");
                
                // 查询该检测的所有缺陷
                LambdaQueryWrapper<Defection> defLqw = new LambdaQueryWrapper<>();
                defLqw.eq(Defection::getDetectId, detectLog.getId());
                java.util.List<Defection> defections = defectionService.list(defLqw);
                if (defections == null) defections = new java.util.ArrayList<>();
                item.put("defections", defections);
                
                results.add(item);
            }
            
            // ===== 新模型数据（DetectionImage 表）=====
            LambdaQueryWrapper<DetectionImage> newLqw = new LambdaQueryWrapper<>();
            newLqw.orderByDesc(DetectionImage::getCreateTime);
            newLqw.last("LIMIT " + limit);
            java.util.List<DetectionImage> newImages = detectionImageMapper.selectList(newLqw);
            
            for (DetectionImage img : newImages) {
                Map<String, Object> item = new HashMap<>();
                // 使用 "new_" 前缀避免与旧模型ID冲突
                item.put("id", "new_" + img.getId());
                item.put("name", img.getFilename() != null ? img.getFilename() : ("工件-" + img.getId()));
                item.put("time", img.getCreateTime());
                item.put("storagePath", "");
                int scratchCount = img.getScratchCount() != null ? img.getScratchCount() : 0;
                item.put("defectionsSum", scratchCount);
                item.put("source", "new");
                
                // 构造缺陷列表（新模型只有划痕计数，无单独缺陷记录）
                java.util.List<Map<String, Object>> defections = new java.util.ArrayList<>();
                for (int i = 0; i < scratchCount; i++) {
                    Map<String, Object> defect = new HashMap<>();
                    defect.put("category", "划痕");
                    defections.add(defect);
                }
                item.put("defections", defections);
                
                results.add(item);
            }
            
            // 按时间正序返回（旧的在前，新的在后）
            java.util.Collections.sort(results, (a, b) -> {
                Object timeA = a.get("time");
                Object timeB = b.get("time");
                if (timeA instanceof java.time.LocalDateTime && timeB instanceof java.time.LocalDateTime) {
                    return ((java.time.LocalDateTime) timeA).compareTo((java.time.LocalDateTime) timeB);
                }
                return 0;
            });
        } catch (Exception e) {
            this.log.error("获取最近检测结果失败", e);
        }
        
        return results;
    }

    /**
     * 根据ID获取单条检测结果的图片（base64）
     * 支持旧模型（纯数字ID）和新模型（new_前缀ID）
     */
    @GetMapping("/image/{id}")
    @ResponseBody
    public Result<Map<String, String>> getImageById(@PathVariable String id) {
        try {
            Map<String, String> imageMap = new HashMap<>();
            
            // 判断是新模型还是旧模型
            if (id.startsWith("new_")) {
                // ===== 新模型：从 DetectionImage + DetectionDefectImage 表获取 =====
                Long detectionImageId = Long.parseLong(id.substring(4));
                DetectionImage detectionImage = detectionImageMapper.selectById(detectionImageId);
                
                if (detectionImage == null) {
                    return Result.fail("检测图片不存在");
                }
                
                // 查询该图片的第一个缺陷裁剪图作为标注图
                LambdaQueryWrapper<DetectionDefectImage> defLqw = new LambdaQueryWrapper<>();
                defLqw.eq(DetectionDefectImage::getDetectionImageId, detectionImageId);
                defLqw.orderByAsc(DetectionDefectImage::getDefectIndex);
                defLqw.last("LIMIT 1");
                DetectionDefectImage defectImage = detectionDefectImageMapper.selectOne(defLqw);
                
                if (defectImage != null) {
                    String base64 = defectImage.getImageBase64();
                    imageMap.put("imgBase64", base64 != null ? base64 : "");
                    imageMap.put("originalImgBase64", base64 != null ? base64 : "");
                    if (defectImage.getContentType() != null) {
                        imageMap.put("contentType", defectImage.getContentType());
                    }
                } else {
                    // 没有缺陷裁剪图，返回空
                    imageMap.put("imgBase64", "");
                    imageMap.put("originalImgBase64", "");
                }
                
                log.info("从新模型获取图片, detectionImageId: {}, 有缺陷图: {}", 
                    detectionImageId, defectImage != null);
            } else {
                // ===== 旧模型：从文件路径读取 =====
                Integer detectId = Integer.parseInt(id);
                DetectLog detectLog = detectLogService.getById(detectId);
                if (detectLog == null) {
                    return Result.fail("检测记录不存在");
                }
                
                // 读取标注图（有红框）
                try {
                    String imgBase64 = ImgUtil.imageToBase64ByPath(detectLog.getStoragePath());
                    imageMap.put("imgBase64", imgBase64);
                } catch (Exception e) {
                    this.log.warn("读取标注图失败: {}", detectLog.getStoragePath());
                    imageMap.put("imgBase64", "");
                }
                
                // 读取原图（无红框）
                String originalPath = detectLog.getStoragePath().replaceAll("(\\.[a-zA-Z]+)$", "_original$1");
                try {
                    String originalImgBase64 = ImgUtil.imageToBase64ByPath(originalPath);
                    imageMap.put("originalImgBase64", originalImgBase64);
                } catch (Exception e) {
                    this.log.debug("读取原图失败（可能为旧数据）: {}", originalPath);
                    imageMap.put("originalImgBase64", imageMap.get("imgBase64"));
                }
            }
            
            return Result.success("成功", imageMap);
        } catch (Exception e) {
            this.log.error("获取图片失败, id={}", id, e);
            return Result.fail("获取图片失败");
        }
    }

    /**
     * 清空所有检测记录（删除数据库记录 + 图片文件）
     * 同时清除旧模型（DetectLog/Defection）和新模型（DetectionBatch/Image/DefectImage）的数据
     */
    @GetMapping("/clear")
    @ResponseBody
    public Result<String> clearAllRecords() {
        try {
            int totalDeleted = 0;
            
            // ===== 旧模型数据清理 =====
            java.util.List<DetectLog> allLogs = detectLogService.list();
            
            // 删除旧模型对应的图片文件
            for (DetectLog detectLog : allLogs) {
                try {
                    String path = detectLog.getStoragePath();
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) file.delete();
                    String originalPath = path.replaceAll("(\\.[a-zA-Z]+)$", "_original$1");
                    java.io.File originalFile = new java.io.File(originalPath);
                    if (originalFile.exists()) originalFile.delete();
                } catch (Exception e) {
                    this.log.warn("删除图片文件失败: {}", detectLog.getStoragePath());
                }
            }
            
            // 删除旧模型缺陷记录和检测记录
            defectionService.remove(new LambdaQueryWrapper<>());
            int oldCount = allLogs.size();
            detectLogService.remove(new LambdaQueryWrapper<>());
            totalDeleted += oldCount;
            log.info("已清空旧模型检测记录 {} 条", oldCount);
            
            // ===== 新模型数据清理 =====
            int defectImageCount = detectionDefectImageMapper.delete(new LambdaQueryWrapper<>());
            int imageCount = detectionImageMapper.delete(new LambdaQueryWrapper<>());
            int batchCount = detectionBatchMapper.delete(new LambdaQueryWrapper<>());
            totalDeleted += defectImageCount + imageCount + batchCount;
            log.info("已清空新模型数据: 批次 {} 条, 检测图 {} 条, 缺陷图 {} 条", batchCount, imageCount, defectImageCount);
            
            return Result.success("成功", "已清空共 " + totalDeleted + " 条检测记录");
        } catch (Exception e) {
            this.log.error("清空检测记录失败", e);
            return Result.fail("清空失败: " + e.getMessage());
        }
    }


}