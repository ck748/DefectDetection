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
        System.out.println("接收图像，仅执行保存操作");

        // 1. 判断是否为空
        if (img == null || img.isEmpty()) {
            return Result.fail("上传的图片为空");
        }

        // 2. 将上传的文件转为 Base64 格式
        String imgBase64 = ImgUtil.convertToJPGBase64(img);

        // 3. 强制保存到指定的路径，并直接使用原图的名字，不再随机生成新文件名
        String originalFilename = img.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = ImgUtil.generateRandomName(); // 兜底防止无文件名
        }
        
        // 【核心修复】：解决文件重复复制、带入路径的问题
        // 因为 formData 中的 filename 可能带有客户端的绝对路径（如 C:\xxx\yyy.jpg），
        // 导致写入时产生预期外的文件或层级。
        // 这里强制只取纯文件名：
        originalFilename = new java.io.File(originalFilename).getName();
        
        String storagePath = "/root/desc/cmzj-main/defectDetection/detectPicture/2/" + originalFilename;

        // 4. 将原始图片的 Base64 数据保存到物理磁盘
        try {
            ImgUtil.saveImageToFile(imgBase64, storagePath);
            log.info("图片已成功保存到路径: {}", storagePath);

            // 为了不影响前端或其他依赖该接口的调用方，返回一个包含原图且没有缺陷的假数据结果
            DetectResDto dummyRes = new DetectResDto();
            dummyRes.setImgBase64(imgBase64);
            dummyRes.setDefections(new java.util.ArrayList<>());
            dummyRes.setDefectionsSum(0);

            return Result.success("图片保存成功", dummyRes);
        } catch (Exception e) {
            log.error("图片保存失败: {}", e.getMessage(), e);
            return Result.fail("图片保存失败");
        }
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