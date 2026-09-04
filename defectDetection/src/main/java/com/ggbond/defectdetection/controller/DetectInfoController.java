package com.ggbond.defectdetection.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.dto.ChartsDto;
import com.ggbond.defectdetection.dto.DetectResDto;
import com.ggbond.defectdetection.pojo.Defection;
import com.ggbond.defectdetection.pojo.DetectLog;
import com.ggbond.defectdetection.pojo.Manager;
import com.ggbond.defectdetection.service.AIAnalysisService;
import com.ggbond.defectdetection.service.DefectionService;
import com.ggbond.defectdetection.service.DetectLogService;
import com.ggbond.defectdetection.service.LogRecordService;
import com.ggbond.defectdetection.service.WorkOrderService;
import com.ggbond.defectdetection.software.common.ConfigProperties;
import com.ggbond.defectdetection.software.data.DataModule;
import com.ggbond.defectdetection.software.info.LogPoint;
import com.ggbond.defectdetection.software.info.OpEnum;
import com.ggbond.defectdetection.util.ImgUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Author: 19461
 * Date: 2024/2/23
 */
@RestController
@Slf4j
@ResponseBody
@RequestMapping("/detectInfo")

public class DetectInfoController {

    @Autowired
    DetectLogService detectLogService;

    @Autowired
    DefectionService defectionService;

    @Autowired
    WorkOrderService workOrderService;

    @Autowired
    DataModule dataModule;

    @Autowired
    AIAnalysisService aiAnalysisService;

    @Autowired
    LogRecordService logRecordService;

    @Autowired(required = false)
    com.ggbond.defectdetection.service.CameraFolderWatchService cameraFolderWatchService;

    /**
     * 摄像头目录监听相关接口 - 统一挂载在 /detectInfo 下确保代理完全放行
     */
    @PostMapping("/cameraWatch/start")
    public Result cameraWatchStartHandler(@RequestBody(required = false) Map<String, String> params) {
        String watchPath = (params != null && params.containsKey("watchPath")) ? params.get("watchPath") : null;
        if (cameraFolderWatchService == null) {
            return Result.fail("监听服务未初始化");
        }
        return cameraFolderWatchService.startWatch(watchPath);
    }

    @PostMapping("/cameraWatch/stop")
    public Result cameraWatchStopHandler() {
        if (cameraFolderWatchService == null) {
            return Result.fail("监听服务未初始化");
        }
        return cameraFolderWatchService.stopWatch();
    }

    @GetMapping("/cameraWatch/status")
    public Result cameraWatchStatusHandler() {
        if (cameraFolderWatchService == null) {
            return Result.success("未启动", Collections.emptyMap());
        }
        return Result.success("获取成功", cameraFolderWatchService.getStatusAndImages());
    }

    @PostMapping("/cameraWatch/clear")
    public Result cameraWatchClearHandler(@RequestBody(required = false) Map<String, Object> params) {
        if (cameraFolderWatchService == null) {
            return Result.fail("监听服务未初始化");
        }
        boolean deletePhysical = params != null && Boolean.TRUE.equals(params.get("deletePhysical"));
        return cameraFolderWatchService.clearImages(deletePhysical);
    }

    @PostMapping("/cameraWatch/delete")
    public Result cameraWatchDeleteHandler(@RequestBody Map<String, Object> params) {
        if (cameraFolderWatchService == null) {
            return Result.fail("监听服务未初始化");
        }
        if (params == null || !params.containsKey("id")) {
            return Result.fail("图片ID参数缺失");
        }
        String id = String.valueOf(params.get("id"));
        boolean deleteSourceFile = Boolean.TRUE.equals(params.get("deleteSourceFile"));
        return cameraFolderWatchService.deleteImage(id, deleteSourceFile);
    }

    /**
     * 视频/多媒体流式播放接口，完整支持 HTTP 206 Range 分片传输
     */
    @GetMapping("/cameraWatch/mediaStream")
    public void mediaStreamHandler(@RequestParam("fileName") String fileName,
                                  jakarta.servlet.http.HttpServletRequest request,
                                  jakarta.servlet.http.HttpServletResponse response) {
        try {
            java.io.File file = new java.io.File("defectDetection/defectDetection/uploads/images", fileName);
            if (!file.exists()) {
                file = new java.io.File("defectDetection/uploads/images", fileName);
            }
            if (!file.exists()) {
                file = new java.io.File("uploads/images", fileName);
            }
            if (!file.exists()) {
                // 如果是绝对路径或者直接在当前目录下
                file = new java.io.File(fileName);
            }
            if (!file.exists()) {
                log.warn("视频流文件不存在: {}", fileName);
                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            long fileLength = file.length();
            String range = request.getHeader("Range");

            String mimeType = "video/mp4";
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".webm")) mimeType = "video/webm";
            else if (lower.endsWith(".ogg")) mimeType = "video/ogg";
            else if (lower.endsWith(".avi")) mimeType = "video/x-msvideo";
            else if (lower.endsWith(".mov")) mimeType = "video/quicktime";

            response.setContentType(mimeType);
            response.setHeader("Accept-Ranges", "bytes");

            if (range == null) {
                response.setHeader("Content-Length", String.valueOf(fileLength));
                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK);
                try (java.io.InputStream in = new java.io.FileInputStream(file);
                     java.io.OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }
            } else {
                long start = 0;
                long end = fileLength - 1;
                String[] parts = range.replace("bytes=", "").split("-");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    start = Long.parseLong(parts[0]);
                }
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    end = Long.parseLong(parts[1]);
                }
                long contentLength = end - start + 1;

                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                response.setHeader("Content-Length", String.valueOf(contentLength));

                try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "r");
                     java.io.OutputStream out = response.getOutputStream()) {
                    raf.seek(start);
                    byte[] buffer = new byte[8192];
                    long bytesToRead = contentLength;
                    while (bytesToRead > 0) {
                        int readLen = (int) Math.min(buffer.length, bytesToRead);
                        int read = raf.read(buffer, 0, readLen);
                        if (read == -1) break;
                        out.write(buffer, 0, read);
                        bytesToRead -= read;
                    }
                }
            }
        } catch (Exception e) {
            log.error("流式读取视频异常:", e);
        }
    }


    @GetMapping("/info/history")
    public Result getHistoryInfoGetHandler(int page, int pageSize,
                                        @RequestParam(required = false) Integer jobId,
                                        @RequestParam(required = false) List<LocalDateTime> dateRange){

        IPage<DetectLog> pageInfo=new Page<>(page,pageSize);
        LocalDateTime dateR=null;
        LocalDateTime dateL=null;

        if(dateRange!=null){
            dateL=dateRange.get(0);
            dateR=dateRange.get(1);
        }

        LambdaQueryWrapper<DetectLog> lqw=new LambdaQueryWrapper<>();

        lqw.eq(jobId!=null,DetectLog::getWorkOrderId,jobId);
        lqw.le(dateR!=null,DetectLog::getTime,dateR);
        lqw.ge(dateL!=null,DetectLog::getTime,dateL);
        lqw.orderByDesc(DetectLog::getTime).orderByDesc(DetectLog::getId);

        detectLogService.page(pageInfo,lqw);
        int totalPages= Math.toIntExact(detectLogService.count(lqw));

        List<DetectLog> detectLogList=pageInfo.getRecords();
        List<DetectResDto> detectResDtoList=DetectResDto.getDtoFromEntities(detectLogList);


        if(detectResDtoList==null){
            return  Result.fail("获取失败,请稍后再试");
        }
        for (DetectResDto detectResDto : detectResDtoList) {
            detectResDto.totals =totalPages;
        }
        return Result.success("获取成功",detectResDtoList);
    }

    @PostMapping("/info/history")
    public Result getHistoryPostInfoHandler(int page, int pageSize,
                                        @RequestParam(required = false) Integer jobId,
                                        @RequestParam(required = false) List<LocalDateTime> dateRange){

        IPage<DetectLog> pageInfo=new Page<>(page,pageSize);
        LocalDateTime dateR=null;
        LocalDateTime dateL=null;

        if(dateRange!=null){
            dateL=dateRange.get(0);
            dateR=dateRange.get(1);
        }

        LambdaQueryWrapper<DetectLog> lqw=new LambdaQueryWrapper<>();

        lqw.eq(jobId!=null,DetectLog::getWorkOrderId,jobId);
        lqw.le(dateR!=null,DetectLog::getTime,dateR);
        lqw.ge(dateL!=null,DetectLog::getTime,dateL);
        lqw.orderByDesc(DetectLog::getTime).orderByDesc(DetectLog::getId);

        detectLogService.page(pageInfo,lqw);
        int totalPages= Math.toIntExact(detectLogService.count(lqw));

        List<DetectLog> detectLogList=pageInfo.getRecords();
        List<DetectResDto> detectResDtoList=DetectResDto.getDtoFromEntities(detectLogList);


        if(detectResDtoList==null){
            return  Result.fail("获取失败,请稍后再试");
        }
        for (DetectResDto detectResDto : detectResDtoList) {
            detectResDto.totals =totalPages;
        }
        return Result.success("获取成功",detectResDtoList);
    }


    @GetMapping("/info/details")
    public Result<DetectResDto> getDetailsHandler(int id){

        DetectLog detectLog=detectLogService.getById(id);

        if(detectLog==null){
            return Result.fail("加载失败,请稍后再试");
        }

        LambdaQueryWrapper<Defection> lqw=new LambdaQueryWrapper<>();
        lqw.eq(Defection::getDetectId,id);

        List<Defection> defectionList=defectionService.list(lqw);

        DetectResDto detectResDto=DetectResDto.generateFromFather(detectLog) ;
        detectResDto.setDefections(defectionList);
        try {
            String imgBase64= ImgUtil.imageToBase64ByPath(detectLog.getStoragePath());
            detectResDto.setImgBase64(imgBase64);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("加载失败,请稍后再试");
        }
        return Result.success("加载成功",detectResDto);
    }

    @DeleteMapping("/info/delete")
    @LogPoint(value = OpEnum.Delete, mainRole = Manager.class,target = DetectLog.class)
    public Result deleteRecordHandler(HttpSession session,@RequestBody ArrayList<Integer> ids){
        log.info("要删除的ids:{}",ids);
        if (ids == null || ids.isEmpty()) {
            return Result.fail("删除列表为空");
        }

        // 查询要删除的记录文件名
        List<DetectLog> detectLogs = detectLogService.listByIds(ids);
        List<String> fileNames = new ArrayList<>();
        if (detectLogs != null) {
            for (DetectLog dl : detectLogs) {
                String path = dl.getStoragePath();
                if (path != null && !path.isEmpty()) {
                    String name = path.replace("\\", "/");
                    name = name.substring(name.lastIndexOf("/") + 1);
                    fileNames.add(name);
                } else {
                    fileNames.add("ID_" + dl.getId() + ".jpg");
                }
            }
        }

        if(detectLogService.removeBatchByIds(ids)){
            // 写入系统审计日志
            try {
                for (String name : fileNames) {
                    logRecordService.recordSuccess("历史检测/质检记录", "删除图片", name, "删除图片: " + name);
                }
            } catch (Exception ex) {
                log.warn("记录历史检测图片删除日志失败: {}", ex.getMessage());
            }
            return Result.success("删除成功");
        }else{
            return Result.fail("删除失败,请稍后再试");
        }
    }

    @GetMapping("/charts/load")
    public Result loadChartsHandler(){

        Map<String, LinkedHashMap> dataMaps = dataModule.getDataMaps();
        List<ChartsDto> chartsDtoList=new LinkedList<>();

        //图1
        ChartsDto<Map> chartsDto1=new ChartsDto<>();
        chartsDto1.setIndex(1);
        chartsDto1.setName("缺陷率变化图");
        chartsDto1.setType("折线图");
        chartsDto1.setSource(dataMaps.get("lineData"));

        chartsDtoList.add(chartsDto1);

        //图2
        ChartsDto<Map> chartsDto2=new ChartsDto<>();
        chartsDto2.setIndex(2);
        chartsDto2.setName("缺陷率变化图");
        chartsDto2.setType("柱状图");
        chartsDto2.setSource(dataMaps.get("lineData"));

        chartsDtoList.add(chartsDto2);

        //图3
        ChartsDto<Map> chartsDto3=new ChartsDto<>();
        chartsDto3.setIndex(3);
        chartsDto3.setName("缺陷占比环状图");
        chartsDto3.setType("环形图");
        chartsDto3.setSource(dataMaps.get("pieData"));

        chartsDtoList.add(chartsDto3);

        //图4
        ChartsDto<Map> chartsDto4=new ChartsDto<>();
        chartsDto4.setIndex(4);
        chartsDto4.setName("api使用次数柱状图");
        chartsDto4.setType("柱状图");
        chartsDto4.setSource(dataMaps.get("apiData"));

        chartsDtoList.add(chartsDto4);

        //图5
        ChartsDto<Map> chartsDto5=new ChartsDto<>();
        chartsDto5.setIndex(5);
        chartsDto5.setName("缺陷总分布图");
        chartsDto5.setType("柱状图");
        chartsDto5.setSource(dataMaps.get("defectionData"));

        chartsDtoList.add(chartsDto5);


        return Result.success("加载成功",chartsDtoList);
    }


    @PutMapping("/charts/set")
    public Result getDataHandler(@RequestParam(required = false,name = "workOrderNumber") Integer workOrderId,
                                 @RequestParam(required = false,name = "N") Integer granularity ){

        if(workOrderId!=null&&workOrderService.getById(workOrderId)==null){
            return Result.fail("工单号不存在");
        }

        Map<String, Map> dataMaps = dataModule.getChartsByWorkOrderId(workOrderId,granularity);

        List<ChartsDto> chartsDtoList=new LinkedList<>();

        //图1
        ChartsDto<Map> chartsDto1=new ChartsDto<>();
        chartsDto1.setIndex(1);
        chartsDto1.setName("缺陷率变化图");
        chartsDto1.setType("折线图");
        chartsDto1.setSource(dataMaps.get("lineData"));

        chartsDtoList.add(chartsDto1);

        //图2
        ChartsDto<Map> chartsDto2=new ChartsDto<>();
        chartsDto2.setIndex(2);
        chartsDto2.setName("缺陷率变化图");
        chartsDto2.setType("柱状图");
        chartsDto2.setSource(dataMaps.get("lineData"));

        chartsDtoList.add(chartsDto2);

        //图3
        ChartsDto<Map> chartsDto3=new ChartsDto<>();
        chartsDto3.setIndex(3);
        chartsDto3.setName("缺陷占比环状图图");
        chartsDto3.setType("环形图");
        chartsDto3.setSource(dataMaps.get("pieData"));

        chartsDtoList.add(chartsDto3);

        return Result.success("加载成功",chartsDtoList);
    }

    /**
     * AI分析缺陷等级和修复建议
     */
    @PostMapping("/ai/analyze")
    public Result aiAnalyzeHandler(@RequestParam Integer detectId) {
        try {
            // 获取检测记录
            DetectLog detectLog = detectLogService.getById(detectId);
            if (detectLog == null) {
                return Result.fail("检测记录不存在");
            }

            // 获取缺陷列表
            LambdaQueryWrapper<Defection> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Defection::getDetectId, detectId);
            List<Defection> defectionList = defectionService.list(lqw);

            // 如果没有缺陷，返回成功结果
            if (defectionList == null || defectionList.isEmpty()) {
                Map<String, Object> noDefectResult = new HashMap<>();
                noDefectResult.put("overallAssessment", "产品质量优秀，未检测到任何缺陷。该产品符合质量标准，可以正常出厂。");
                noDefectResult.put("overallSeverity", 0);
                noDefectResult.put("defections", new ArrayList<>());
                noDefectResult.put("success", true);
                return Result.success("AI分析完成", noDefectResult);
            }

            // 调用AI分析
            Map<String, Object> analysisResult = aiAnalysisService.analyzeDefections(defectionList);

            if (!(boolean) analysisResult.getOrDefault("success", false)) {
                return Result.fail(analysisResult.getOrDefault("error", "AI分析失败").toString());
            }

            // 更新缺陷信息到数据库
            for (Defection defection : defectionList) {
                if (defection.getSeverityLevel() != null && defection.getRepairSuggestion() != null) {
                    defectionService.updateById(defection);
                }
            }

            return Result.success("AI分析完成", analysisResult);

        }finally {
            
        }
    }


}