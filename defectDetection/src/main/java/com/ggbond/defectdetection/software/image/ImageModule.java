package com.ggbond.defectdetection.software.image;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.config.YAMLPropertySourceFactory;
import com.ggbond.defectdetection.dto.DetectResDto;
import com.ggbond.defectdetection.pojo.*;
import com.ggbond.defectdetection.service.*;
import com.ggbond.defectdetection.software.common.CommonResource;
import com.ggbond.defectdetection.software.common.ConfigProperties;
import com.ggbond.defectdetection.software.common.Software;
import com.ggbond.defectdetection.software.common.SysStatus;
import com.ggbond.defectdetection.software.data.DataModule;
import com.ggbond.defectdetection.software.face.RealtimeInterface;
import com.ggbond.defectdetection.util.ImgUtil;
import com.ggbond.defectdetection.util.SseUtil;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 图像处理模块,包含图像预处理,模型识别,局部异常处理
 * <p>
 * Author: 19461
 * Date: 2024/2/16
 */
@Data
@Slf4j
@Component("ImageModule")
@DependsOn("ConfigProperties")
@RestController //接受ip摄像机的图像需要
@RequestMapping("/detect")
public class ImageModule {

    @Autowired
    SseUtil sseUtil;

    @Autowired
    CheckService checkService;

    @Autowired
    DefectionService defectionService;

    @Autowired
    DetectLogService detectLogService;

    @Autowired
    DefectionCategoryService defectionCategoryService;

    @Autowired
    DataModule dataModule;

    @Autowired
    private DefectionSeverityService severityService;

    @Autowired(required = false)
    private RealtimeInterface realtimeInterface;


    private static DetectResDto res=new DetectResDto();

    public static DetectResDto getRes(){
        return res;
    }

    private DetectModel detectModel=new DetectModel();

    public ImageModule(){

    }

    //接收图像
    @PostMapping("/img")
    public Result imgProcess(@RequestPart("img") MultipartFile img) throws Exception {
        System.out.println("接收图像");

        /*
        if(CommonResource.getStatus() != SysStatus.WORKING){
            //同步控制暂停设备
            return;
        }
        */

        //判断是否为图片并转为jpeg格式
        String imgBase64 = ImgUtil.convertToJPGBase64(img);
        log.info("[ImageModule] 图片转换完成，base64长度: {}", imgBase64.length());

        // 【关键修复】先保存原始图片到临时路径，避免后续被覆盖
        String tempOriginalPath = ConfigProperties.properties.getModelConfig().getResStoragePath() + "/temp_original_" + System.currentTimeMillis() + ".jpg";
        try {
            ImgUtil.saveImageToFile(imgBase64, tempOriginalPath);
            log.info("[ImageModule] 原始图片已保存到临时路径: {}", tempOriginalPath);
        } catch (Exception e) {
            log.error("[ImageModule] 保存原始图片失败: {}", e.getMessage(), e);
        }

        // 旧模型检测已禁用，由 ModelMain_V4.py 新模型统一处理（含批次检测+Qwen分析+回调）
        // 此处仅保存原图，不做检测，检测结果由 Python 端回调 /detection/batch/callback 写入
        DetectResDto currentRes = new DetectResDto();
        currentRes.setImgBase64(imgBase64);
        currentRes.setOriginalImgBase64(imgBase64);
        currentRes.setDefections(new java.util.ArrayList<>());
        currentRes.setDefectionsSum(0);
        log.info("[ImageModule] 旧模型检测已跳过，图片将由 ModelMain_V4.py 新模型处理");

        // 设置原图到 SSE 消息中（左侧面板显示原图，右侧显示标注图）
        if (currentRes.getOriginalImgBase64() == null) {
            currentRes.setOriginalImgBase64(imgBase64);
            log.info("[ImageModule] 设置 originalImgBase64，长度: {}", imgBase64.length());
        } else {
            log.info("[ImageModule] originalImgBase64 已存在，长度: {}", currentRes.getOriginalImgBase64().length());
        }

        // 更新 static res 仅用于 GUI 显示（不影响 SSE 推送）
        res = currentRes;

        // ===== 图片文件保存（GUI 桌面端显示用）=====
        // 注意：不再保存到 DetectLog 表，避免与新模型 DetectionImage 表产生重复记录
        // 新模型的检测结果由 ModelMain_V4.py 回调 /detection/batch/callback 统一入库
        Integer workOrderId = 0;
        try {
            if (CommonResource.getCurrentWorkOder() != null) {
                workOrderId = CommonResource.getCurrentWorkOder().getId();
            }
        } catch (Exception e) {
            log.warn("获取工单ID失败: {}", e.getMessage());
        }

        // 保存标注图和原图到文件（供 GUI 桌面端显示）
        String name = ImgUtil.generateRandomName();
        String storagePath = ConfigProperties.properties.getModelConfig().getResStoragePath() + "/" + workOrderId + "/" + name;
        try {
            ImgUtil.saveImageToFile(currentRes.getImgBase64(), storagePath);
            String originalPath = storagePath.replaceAll("(\\.[a-zA-Z]+)$", "_original$1");
            ImgUtil.saveImageToFile(imgBase64, originalPath);
            log.info("[ImageModule] 图片文件保存成功: {}", storagePath);
        } catch (Exception e) {
            log.error("[ImageModule] 图片文件保存失败: {}", e.getMessage(), e);
        }

        // ===== 更新内存统计数据（不写数据库）=====
        int defectionsSum = currentRes.getDefections() != null ? currentRes.getDefections().size() : 0;
        currentRes.setDefectionsSum(defectionsSum);
        dataModule.updateDataMaps(currentRes);

        //3.4 工单进度加1,更新到新工单
        if (CommonResource.getCurrentWorkOder() != null) {
            CommonResource.updateWorkOrder();
        }

        //更新图像和图表 - 只在GUI可用时更新
        if(realtimeInterface != null) {
            realtimeInterface.getDetectImagePanel().updateImageAndTable(currentRes);
            if (CommonResource.getCurrentWorkOder() != null) {
                realtimeInterface.getDetectImagePanel().updateTextLabel(workOrderId, CommonResource.getCurrentNum(), CommonResource.getDetectSum());
            }
            realtimeInterface.updateCharts(dataModule.getDataMaps());
        }

        //将最新结果发送至web端(通过SSE) — 使用局部变量 currentRes，避免并发覆盖
        new Thread(sseUtil.sendMessageToAll(String.valueOf(Result.IMAGE_CODE), currentRes),"发送检测结果").start();
        
        //同时直接返回结果给调用方
        return Result.success("检测完成", currentRes);
    }


    //初始化工作:读取配置,加载模型,测试连接
    @PostConstruct
    public void init(){
        //加载模型
        try {
            // 使用相对路径，兼容不同环境
            String initImagePath = "src/main/resources/assets/init.png";
            java.io.File initFile = new java.io.File(initImagePath);
            
            if (initFile.exists()) {
                res.setImgBase64(ImgUtil.imageToBase64ByPath(initImagePath));
            } else {
                log.warn("初始化图片不存在: {}", initImagePath);
                // 设置空的Base64字符串而不是null
                res.setImgBase64("");
            }
            
            // 初始化为空列表而不是null，避免前端报错
            res.setDefections(new java.util.ArrayList<>());
            res.setDefectionsSum(0);
        } catch (IOException e) {
            log.error("初始化图片加载失败", e);
            res.setImgBase64("");
            res.setDefections(new java.util.ArrayList<>());
            res.setDefectionsSum(0);
        }
        detectModel.init();
    }

    //获取当前的结果
    public DetectResDto getCurrentDetectRes(){
        return res;
    }

    //图像检测（旧模型已禁用，由 ModelMain_V4.py 统一处理）
    public DetectResDto imgDetect(String  imgBase64){

        if(imgBase64==null){
            return null;
        }

        // 旧模型检测已禁用，返回原图和空缺陷列表
        DetectResDto res = new DetectResDto();
        res.setImgBase64(imgBase64);
        res.setOriginalImgBase64(imgBase64);
        res.setDefections(new java.util.ArrayList<>());
        res.setDefectionsSum(0);
        log.info("[ImageModule.imgDetect] 旧模型检测已跳过");
        return res;
    }


    public static boolean ConnectModelTest(){
        return DetectModel.testHttpConnection();
    }

    //保存为抽检信息
    public boolean saveAsCheckInfo(DetectResDto detectResDto){

        Check checkInfo=Check.convertTOCheck(detectResDto);

        return checkService.save(checkInfo);
    }
}