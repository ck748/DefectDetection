package com.ggbond.defectdetection.controller;

import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.CameraWatchRecord;
import com.ggbond.defectdetection.service.CameraFolderWatchService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cameraWatch")
public class CameraWatchController {

    @Autowired
    private CameraFolderWatchService cameraFolderWatchService;

    /**
     * 接收客户端自动监听并上传的米家拍照图片
     * 对应客户端 POST http://192.168.1.3:8081/cameraWatch/upload
     */
    @PostMapping("/upload")
    public Result<CameraWatchRecord> uploadCameraImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "fileName", required = false) String fileName
    ) {
        return cameraFolderWatchService.saveUploadedImage(image, fileName);
    }

    /**
     * 启动监听 / 应用切换服务器端存储目录
     */
    @PostMapping("/start")
    public Result<String> startWatch(
            @RequestParam(required = false) String watchPath,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String path = watchPath;
        if ((path == null || path.trim().isEmpty()) && body != null) {
            path = body.get("watchPath");
        }
        return cameraFolderWatchService.startWatch(path);
    }

    /**
     * 停止监听
     */
    @PostMapping("/stop")
    public Result<String> stopWatch() {
        return cameraFolderWatchService.stopWatch();
    }

    /**
     * 获取当前状态及直接从物理目录扫描获取的最新图片列表（附带AI识别数据）
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        return Result.success("获取成功", cameraFolderWatchService.getStatusAndImages());
    }

    /**
     * 前端触发单张图片的 AI 视觉推理代理（同时支持 GET 和 POST，解决跨域与网络直连问题）
     */
    @RequestMapping(value = "/detect", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<Map<String, Object>> detectSingleImage(
            @RequestParam(value = "fileName", required = false) String fileNameParam,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String fileName = fileNameParam;
        if ((fileName == null || fileName.trim().isEmpty()) && body != null) {
            fileName = body.get("fileName");
        }
        return Result.success("识别完成", cameraFolderWatchService.detectSingleImage(fileName));
    }

    /**
     * 直接输出相机抓拍图片流
     */
    @GetMapping("/image")
    public void getImageStream(@RequestParam(value = "id", required = false) Integer id,
                               @RequestParam(value = "name", required = false) String name,
                               HttpServletResponse response) {
        cameraFolderWatchService.writeImageStream(id, name, response);
    }

    /**
     * 删除单张物理图片
     */
    @PostMapping("/delete")
    public Result<String> deleteImage(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null || !body.containsKey("id")) {
            return Result.fail("参数错误，缺失图片标识");
        }

        Object idObj = body.get("id");
        boolean deleteSourceFile = true;
        if (body.containsKey("deleteSourceFile")) {
            deleteSourceFile = Boolean.parseBoolean(body.get("deleteSourceFile").toString());
        }

        return cameraFolderWatchService.deleteImage(idObj, deleteSourceFile);
    }

    /**
     * 清空物理目录图片
     */
    @PostMapping("/clear")
    public Result<String> clearList(@RequestParam(defaultValue = "false") boolean deletePhysical) {
        return cameraFolderWatchService.clearImages(deletePhysical);
    }
}
