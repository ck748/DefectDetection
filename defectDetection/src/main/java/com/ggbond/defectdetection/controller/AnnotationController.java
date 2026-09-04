package com.ggbond.defectdetection.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.AnnotationData;
import com.ggbond.defectdetection.pojo.RawImage;
import com.ggbond.defectdetection.service.AnnotationDataService;
import com.ggbond.defectdetection.service.LogRecordService;
import com.ggbond.defectdetection.service.RawImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import javax.imageio.ImageIO;

/**
 * 数据标注控制器
 * 处理摄像头拍照上传、图片管理、标注任务、标注数据等接口
 */
@RestController
@Slf4j
@ResponseBody
@RequestMapping("/annotation")
public class AnnotationController {

    @Autowired
    private RawImageService rawImageService;

    @Autowired
    private AnnotationDataService annotationDataService;

    @Autowired
    private LogRecordService logRecordService;

    /**
     * 数据标注上传接口
     */
    @PostMapping("/upload/camera")
    public Result uploadCameraImage(@RequestParam("image") MultipartFile image,
                                    @RequestParam(required = false) Integer deviceId,
                                    @RequestParam(required = false) Integer workOrderId,
                                    @RequestParam(required = false) Integer userId,
                                    @RequestParam(required = false) String userName) {
        try {
            // 检查文件
            if (image.isEmpty()) {
                return Result.fail("上传文件为空");
            }

            // 生成唯一文件名（使用时间戳）
            String originalFilename = image.getOriginalFilename();
            String fileExtension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 使用精确的时间戳：yyyy-MM-dd_HH-mm-ss-SSS
            String timestamp = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
            );
            String uniqueFilename = timestamp + fileExtension;

            // 保存文件到指定目录
            String uploadDir = "uploads/images/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            Path filePath = Paths.get(uploadDir + uniqueFilename);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 保存到数据库
            RawImage rawImage = new RawImage();
            rawImage.setImageName(originalFilename);
            rawImage.setImagePath(filePath.toString());
            rawImage.setImageSize(image.getSize());
            rawImage.setUploadSource("camera");
            rawImage.setDeviceId(deviceId);
            rawImage.setWorkOrderId(workOrderId);
            rawImage.setUploadTime(LocalDateTime.now());
            rawImage.setUploadUserId(userId);
            rawImage.setUploadUserName(userName);
            rawImage.setStatus(0); // 待标注状态

            rawImageService.save(rawImage);

            // 记录真实系统审计日志
            try {
                logRecordService.recordSuccess("数据标注/图像管理", "上传图片", originalFilename, "上传图片: " + originalFilename);
            } catch (Exception ex) {
                log.warn("记录图片上传日志失败: {}", ex.getMessage());
            }

            log.info("📸 摄像头图片上传成功: 文件名={}, 设备ID={}, 大小={}KB",
                originalFilename, deviceId, image.getSize() / 1024);

            return Result.success("图片上传成功", rawImage);
        } catch (IOException e) {
            log.error("图片上传失败: {}", e.getMessage(), e);
            return Result.fail("图片上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("图片上传异常: {}", e.getMessage(), e);
            return Result.fail("图片上传异常: " + e.getMessage());
        }
    }

    /**
     * 摄像头调用
     */
    @PostMapping("/upload/camera/auto")
    public Result autoUploadFromCamera(@RequestParam("image") MultipartFile image,
                                       @RequestParam(required = false, defaultValue = "0") Integer deviceId) {
        try {
            // 检查文件
            if (image.isEmpty()) {
                return Result.fail("上传文件为空");
            }

            // 生成唯一文件名（带时间戳）
            String timestamp = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
            );
            String fileExtension = ".jpg"; 
            String originalFilename = image.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = "camera_" + deviceId + "_" + timestamp + fileExtension;

        
            String uploadDir = "uploads/camera/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            Path filePath = Paths.get(uploadDir + uniqueFilename);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 保存到数据库
            RawImage rawImage = new RawImage();
            rawImage.setImageName(uniqueFilename);
            rawImage.setImagePath(filePath.toString());
            rawImage.setImageSize(image.getSize());
            rawImage.setUploadSource("camera");
            rawImage.setDeviceId(deviceId);
            rawImage.setUploadTime(LocalDateTime.now());
            rawImage.setStatus(0); // 待标注状态

            rawImageService.save(rawImage);

            // 记录真实系统审计日志
            try {
                logRecordService.recordSuccess("数据标注/图像管理", "上传图片", uniqueFilename, "上传图片: " + uniqueFilename);
            } catch (Exception ex) {
                log.warn("记录图片自动上传日志失败: {}", ex.getMessage());
            }

            log.info("📸 摄像头自动上传成功: ID={}, 文件={}, 大小={}KB",
                rawImage.getId(), uniqueFilename, image.getSize() / 1024);

            return Result.success("上传成功", rawImage.getId());
        } catch (IOException e) {
            log.error("❌ 摄像头自动上传失败: {}", e.getMessage(), e);
            return Result.fail("上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ 摄像头自动上传异常: {}", e.getMessage(), e);
            return Result.fail("上传异常: " + e.getMessage());
        }
    }

    /**
     * 获取待标注图片列表（按时间戳排序）
     */
    @GetMapping("/images/pending")
    public Result getPendingImages(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   @RequestParam(required = false) Integer workOrderId) {
        try {
            IPage<RawImage> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<RawImage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RawImage::getStatus, 0); // 待标注状态
            queryWrapper.eq(workOrderId != null, RawImage::getWorkOrderId, workOrderId);
            // 按文件名（时间戳）降序排列，最新的在前面
            queryWrapper.orderByDesc(RawImage::getImageName);
            queryWrapper.orderByDesc(RawImage::getUploadTime);

            rawImageService.page(pageInfo, queryWrapper);

            return Result.success("获取待标注图片成功", pageInfo);
        } catch (Exception e) {
            log.error("获取待标注图片失败: {}", e.getMessage(), e);
            return Result.fail("获取待标注图片失败: " + e.getMessage());
        }
    }

    /**
     * 打回图片到待标注状态
     */
    @PutMapping("/images/{imageId}/reject")
    public Result rejectAnnotation(@PathVariable Integer imageId) {
        try {
            RawImage rawImage = rawImageService.getById(imageId);
            if (rawImage == null || rawImage.getIsDeleted() == 1) {
                return Result.fail("图片不存在或已被删除");
            }
            
            // 将状态改为待标注
            rawImage.setStatus(0);
            boolean updated = rawImageService.updateById(rawImage);
            
            if (updated) {
               
                LambdaQueryWrapper<AnnotationData> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(AnnotationData::getRawImageId, imageId);
                List<AnnotationData> annotationList = annotationDataService.list(queryWrapper);
                
                if (!annotationList.isEmpty()) {
                    List<Integer> ids = annotationList.stream()
                        .map(AnnotationData::getId)
                        .collect(java.util.stream.Collectors.toList());
                    annotationDataService.removeByIds(ids);
                    log.info("✅ 已删除 {} 条标注数据", ids.size());
                }
                
                log.info("✅ 图片已打回到待标注状态: imageId={}", imageId);
                return Result.success("打回成功");
            } else {
                return Result.fail("打回失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("打回图片失败: {}", e.getMessage(), e);
            return Result.fail("打回失败: " + e.getMessage());
        }
    }

    /**
     * 删除图片（
     */
    @DeleteMapping("/images/{imageId}")
    public Result deleteImage(@PathVariable Integer imageId) {
        try {
            // 先检查图片是否存在
            RawImage rawImage = rawImageService.getById(imageId);
            if (rawImage == null) {
                return Result.fail("图片不存在或已被删除");
            }
            
            // 使用MyBatis Plus的removeById方法，会自动处理逻辑删除
            boolean deleted = rawImageService.removeById(imageId);
            
            if (deleted) {
                // 同时删除相关标注数据（也是逻辑删除）
                LambdaQueryWrapper<AnnotationData> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(AnnotationData::getRawImageId, imageId);
                List<AnnotationData> annotationList = annotationDataService.list(queryWrapper);
                
                if (!annotationList.isEmpty()) {
                    // 批量逻辑删除
                    List<Integer> ids = annotationList.stream()
                        .map(AnnotationData::getId)
                        .collect(java.util.stream.Collectors.toList());
                    annotationDataService.removeByIds(ids);
                    log.info("✅ 已删除 {} 条标注数据", ids.size());
                }
                
                log.info("✅ 图片已删除: imageId={}", imageId);

                // 记录真实系统审计日志
                try {
                    String imgName = rawImage.getImageName() != null ? rawImage.getImageName() : ("ID_" + imageId);
                    logRecordService.recordSuccess("数据标注/图像管理", "删除图片", imgName, "删除图片: " + imgName);
                } catch (Exception ex) {
                    log.warn("记录删除图片日志失败: {}", ex.getMessage());
                }

                return Result.success("删除成功");
            } else {
                return Result.fail("删除失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("删除图片失败: {}", e.getMessage(), e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取已标注图片列表（按时间戳排序）
     * 包括：已标注(status=2) 和 已检测(status=3) 的图片
     */
    @GetMapping("/images/annotated")
    public Result getAnnotatedImages(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(required = false) Integer workOrderId) {
        try {
            IPage<RawImage> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<RawImage> queryWrapper = new LambdaQueryWrapper<>();
            // 只显示已标注(2)的图片，检测图片(3)在历史检测页面显示
            queryWrapper.eq(RawImage::getStatus, 2);
            // MyBatis Plus会自动添加 is_deleted = 0 的条件，不需要手动添加
            queryWrapper.eq(workOrderId != null, RawImage::getWorkOrderId, workOrderId);
            // 按文件名（时间戳）降序排列，最新的在前面
            queryWrapper.orderByDesc(RawImage::getImageName);
            queryWrapper.orderByDesc(RawImage::getUploadTime);

            rawImageService.page(pageInfo, queryWrapper);
            
            log.info("✅ 获取已标注/已检测图片: 第{}页, 总数={}", page, pageInfo.getTotal());

            return Result.success("获取已标注图片成功", pageInfo);
        } catch (Exception e) {
            log.error("获取已标注图片失败: {}", e.getMessage(), e);
            return Result.fail("获取已标注图片失败: " + e.getMessage());
        }
    }



    /**
     * 保存标注数据
     保存到annotated目录
     */
    @PostMapping("/data")
    public Result saveAnnotationData(@RequestBody List<AnnotationData> annotationDataList) {
        try {
            if (annotationDataList == null || annotationDataList.isEmpty()) {
                return Result.fail("标注数据为空");
            }

            // 保存标注数据
            annotationDataService.saveBatch(annotationDataList);

            // 检查是否有裂纹或划痕的标注
            boolean hasDefect = annotationDataList.stream()
                .anyMatch(data -> "裂纹".equals(data.getCategory()) || "划痕".equals(data.getCategory()));

            // 更新图片状态为已标注
            if (!annotationDataList.isEmpty()) {
                Integer rawImageId = annotationDataList.get(0).getRawImageId();
                RawImage rawImage = rawImageService.getById(rawImageId);
                if (rawImage != null) {
                    rawImage.setStatus(2); // 已标注状态
                    rawImageService.updateById(rawImage);
                    
                    // 如果有缺陷标注，生成带框的图片
                    if (hasDefect) {
                        try {
                            String annotatedImagePath = generateAnnotatedImage(rawImage, annotationDataList);
                            log.info("✅ 已生成带标注框的图片: {}", annotatedImagePath);
                        } catch (Exception e) {
                            log.error("❌ 生成带标注框的图片失败: {}", e.getMessage(), e);
                           
                        }
                    }
                }
            }

            return Result.success("保存标注数据成功");
        } catch (Exception e) {
            log.error("保存标注数据失败: {}", e.getMessage(), e);
            return Result.fail("保存标注数据失败: " + e.getMessage());
        }
    }

    /**
     * 生成带标注框的图片
     * @param rawImage 原始图片信息
     * @param annotationDataList 标注数据列表
     * @return 生成的图片路径
     */
    private String generateAnnotatedImage(RawImage rawImage, List<AnnotationData> annotationDataList) throws IOException {
        // 读取原始图片
        File originalFile = new File(rawImage.getImagePath());
        if (!originalFile.exists()) {
            throw new IOException("原始图片不存在: " + rawImage.getImagePath());
        }
        
        BufferedImage originalImage = ImageIO.read(originalFile);
        if (originalImage == null) {
            throw new IOException("无法读取图片: " + rawImage.getImagePath());
        }
        
        // 创建Graphics2D对象绘制
        Graphics2D g2d = originalImage.createGraphics();
        
        
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                            java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        
       
        for (AnnotationData annotation : annotationDataList) {
           
            if (!"裂纹".equals(annotation.getCategory()) && !"划痕".equals(annotation.getCategory())) {
                continue;
            }


            int x = annotation.getX() != null ? annotation.getX().intValue() : 0;
            int y = annotation.getY() != null ? annotation.getY().intValue() : 0;
            int width = annotation.getWidth() != null ? annotation.getWidth().intValue() : 0;
            int height = annotation.getHeight() != null ? annotation.getHeight().intValue() : 0;


            Color boxColor;
            Color shadowColor;
            if ("裂纹".equals(annotation.getCategory())) {
                boxColor = new Color(255, 0, 0);      
                shadowColor = new Color(139, 0, 0);   
            } else {
                boxColor = new Color(255, 165, 0);   
                shadowColor = new Color(255, 140, 0); 
            }
            
          
            g2d.setColor(shadowColor);
            g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawRect(x, y, width, height);
            
            
            g2d.setColor(boxColor);
            g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawRect(x, y, width, height);
            
            
            String labelText = annotation.getCategory();
            Font labelFont = new Font("Microsoft YaHei", Font.BOLD, 20);
            g2d.setFont(labelFont);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(labelText);
            int textHeight = fm.getHeight();
            int padding = 8;
            int labelWidth = textWidth + padding * 2;
            int labelHeight = textHeight + padding;
            
          
            g2d.setColor(new Color(0, 0, 0, 100)); 
            g2d.fillRoundRect(x + 2, y - labelHeight + 2, labelWidth, labelHeight, 8, 8);
            
           
            GradientPaint gradient = new GradientPaint(
                x, y - labelHeight, boxColor,
                x, y, shadowColor
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(x, y - labelHeight, labelWidth, labelHeight, 8, 8);
            
         
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y - labelHeight, labelWidth, labelHeight, 8, 8);
            
          
            g2d.setColor(new Color(0, 0, 0, 150)); 
            g2d.drawString(labelText, x + padding + 1, y - padding + 1);
            
            g2d.setColor(Color.WHITE); 
            g2d.drawString(labelText, x + padding, y - padding);
        }
        
        g2d.dispose();
        
        // 创建保存目录
        String annotatedDir = "uploads/annotated/";
        File dir = new File(annotatedDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 生成新文件名（使用时间戳）
        String originalFileName = originalFile.getName();
        String fileExtension = ".jpg";
        if (originalFileName.toLowerCase().endsWith(".png")) {
            fileExtension = ".png";
        } else if (originalFileName.toLowerCase().endsWith(".jpeg")) {
            fileExtension = ".jpeg";
        }
        
       
        String timestamp = LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
        );
        String annotatedFileName = "annotated_" + timestamp + fileExtension;
        String annotatedFilePath = annotatedDir + annotatedFileName;
        
        // 保存带标注的图片
        File annotatedFile = new File(annotatedFilePath);
        String format = "jpg";
        if (fileExtension.toLowerCase().endsWith(".png")) {
            format = "png";
        }
        ImageIO.write(originalImage, format, annotatedFile);
        
        log.info("📝 标注框绘制完成: {} 个标注，保存至 {}", annotationDataList.size(), annotatedFilePath);
        
        return annotatedFilePath;
    }

    /**
     * 获取图片的标注数据
     * MyBatis Plus会自动过滤is_deleted=1的数据
     */
    @GetMapping("/data/image/{imageId}")
    public Result getAnnotationDataByImageId(@PathVariable Integer imageId) {
        try {
            LambdaQueryWrapper<AnnotationData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AnnotationData::getRawImageId, imageId);
            // MyBatis Plus会自动添加 is_deleted = 0 的条件
            List<AnnotationData> annotationDataList = annotationDataService.list(queryWrapper);

            return Result.success("获取标注数据成功", annotationDataList);
        } catch (Exception e) {
            log.error("获取标注数据失败: {}", e.getMessage(), e);
            return Result.fail("获取标注数据失败: " + e.getMessage());
        }
    }



    /**
     * 访问上传的图片文件
     * 支持从摄像头目录(uploads/camera/)、标注目录(uploads/images/)和带框图片目录(uploads/annotated/)
     */
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            File file = null;
            
            // 1. 先尝试从标注带框目录读取
            File annotatedFile = new File("uploads/annotated/" + filename);
            if (annotatedFile.exists()) {
                file = annotatedFile;
                log.info("从标注带框目录访问文件: {}", file.getAbsolutePath());
            } else {
                // 2. 再尝试从摄像头目录读取
                File cameraFile = new File("uploads/camera/" + filename);
                if (cameraFile.exists()) {
                    file = cameraFile;
                    log.info("从摄像头目录访问文件: {}", file.getAbsolutePath());
                } else {
                    // 3. 最后尝试从标注目录读取
                    File imageFile = new File("uploads/images/" + filename);
                    if (imageFile.exists()) {
                        file = imageFile;
                        log.info("从标注目录访问文件: {}", file.getAbsolutePath());
                    }
                }
            }
            
            if (file == null || !file.exists()) {
                log.warn("文件不存在: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // 读取文件
            Resource resource = new FileSystemResource(file);
            
            // 获取文件MIME类型
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("读取文件失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}