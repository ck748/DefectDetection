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

    @Autowired
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

        //模型分析
        try {
            res = detectModel.detectOne(imgBase64);
        } catch (Exception e) {
            log.error("模型检测失败，可能是Python预测服务未启动(8090端口): {}", e.getMessage());
            // 如果模型检测失败，返回原图和空的检测结果
            res = new DetectResDto();
            res.setImgBase64(imgBase64);
            res.setDefections(new java.util.ArrayList<>());
            res.setDefectionsSum(0);
        }

        //存储新的检测信息
        //1.获取工单号 (如果没有工单，使用0作为默认值)
        Integer workOrderId = 0;
        try {
            if (CommonResource.getCurrentWorkOder() != null) {
                workOrderId = CommonResource.getCurrentWorkOder().getId();
            } else {
                log.warn("当前没有工单，使用默认工单ID=0");
            }
        } catch (Exception e) {
            log.warn("获取工单ID失败，使用默认工单ID=0: {}", e.getMessage());
        }
        //2.缺陷总数
        int defectionsSum=res.getDefections().size();

        //3.存储相关信息
        //3.1 存储检测信息
        DetectLog detectLog=new DetectLog();
        String name=ImgUtil.generateRandomName();
        detectLog.setName(name);
        detectLog.setDefectionsSum(defectionsSum);
        detectLog.setWorkOrderId(workOrderId);
        detectLog.setStoragePath(ConfigProperties.properties.getModelConfig().getResStoragePath()+"\\"+workOrderId+"\\"+name);
        detectLog.setTime(LocalDateTime.now());
        ImgUtil.saveImageToFile(res.getImgBase64(),detectLog.getStoragePath());
        detectLogService.save(detectLog);

        LambdaQueryWrapper<DetectLog> lqw=new LambdaQueryWrapper<>();
        lqw.eq(DetectLog::getName,name);

        DetectLog detectLog1=detectLogService.getOne(lqw);
        Integer detectId=detectLog1.getId();

        //3.2 存储缺陷信息
        int actualDefectionCount = 0;
        for(Defection defection:res.getDefections()){

            String category=defection.getCategory();
            LambdaQueryWrapper<DefectionCategory> lqw1=new LambdaQueryWrapper<>();
            lqw1.eq(category!=null,DefectionCategory::getName,category);
            if(!defectionCategoryService.exists(lqw1)){
                DefectionCategory defectionCategory=new DefectionCategory(null,category, 0,LocalDateTime.now());
                defectionCategoryService.save(defectionCategory);
            }
            Integer categoryId=defectionCategoryService.getOne(lqw1).getId();
            defection.setCategoryId(categoryId);
            defection.setDetectId(detectId);
            // 在 defection.setDetectId(detectId); 之后添加:
            severityService.evaluateDefection(defection);
            defectionService.save(defection);
            actualDefectionCount++;

        }
        
        // 3.3 更新detect_log的实际缺陷数（防止保存时计算错误）
        if(actualDefectionCount != defectionsSum) {
            log.warn("检测到缺陷数不一致: 保存的detect_log.defections_sum={}, 实际保存的缺陷数={}, 进行修正", 
                defectionsSum, actualDefectionCount);
            detectLog1.setDefectionsSum(actualDefectionCount);
            detectLogService.updateById(detectLog1);
        }

        //3.2 工单进度加1,更新到新工单
        if (CommonResource.getCurrentWorkOder() != null) {
            CommonResource.updateWorkOrder();
        }

        //4. 更新表格dataMap
        DetectResDto resPlus=DetectResDto.generateFromFather(detectLog1);
        dataModule.updateDataMaps(resPlus);

        //更新图像和图表
        realtimeInterface.getDetectImagePanel().updateImageAndTable(res);
        if (CommonResource.getCurrentWorkOder() != null) {
            realtimeInterface.getDetectImagePanel().updateTextLabel(workOrderId,CommonResource.getCurrentNum(),CommonResource.getDetectSum());
        }
        realtimeInterface.updateCharts(dataModule.getDataMaps());

        //将最新结果发送至web端(通过SSE)
        new Thread(sseUtil.sendMessageToAll(String.valueOf(Result.IMAGE_CODE),res),"发送检测结果").start();
        
        //同时直接返回结果给调用方
        return Result.success("检测完成", res);
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

    //图像检测
    public DetectResDto imgDetect(String  imgBase64){

        if(imgBase64==null){
            return null;
        }

        return detectModel.detectOne(imgBase64);
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