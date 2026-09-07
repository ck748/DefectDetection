package com.ggbond.defectdetection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ggbond.defectdetection.mapper.DetectionBatchMapper;
import com.ggbond.defectdetection.mapper.DetectionDefectImageMapper;
import com.ggbond.defectdetection.mapper.DetectionImageMapper;
import com.ggbond.defectdetection.pojo.DetectionBatch;
import com.ggbond.defectdetection.pojo.DetectionDefectImage;
import com.ggbond.defectdetection.pojo.DetectionImage;
import com.ggbond.defectdetection.pojo.vo.ModelResultVO;
import com.ggbond.defectdetection.service.DetectionResultSaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : poi
 * @project : defectDetection
 * @package : com.ggbond.defectdetection.service.impl
 * @className : DetectionResultSaveServiceImpl
 * @createTime : 2026/9/6 14:38
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionResultSaveServiceImpl implements DetectionResultSaveService {

    private final DetectionBatchMapper detectionBatchMapper;
    private final DetectionImageMapper detectionImageMapper;
    private final DetectionDefectImageMapper detectionDefectImageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveResult(ModelResultVO result) {
        if (result == null || result.getBatchId() == null) {
            log.warn("检测结果为空或缺少 batchId，无法保存");
            return;
        }

        String batchId = result.getBatchId();

        // 1. 判断这个 batchId 是否已经保存，如果存在直接返回，防止重复入库
        Long count = detectionBatchMapper.selectCount(
                new LambdaQueryWrapper<DetectionBatch>().eq(DetectionBatch::getBatchId, batchId)
        );
        if (count != null && count > 0) {
            log.info("批次 [{}] 的检测结果已存在，跳过保存", batchId);
            return;
        }

        log.info("开始保存批次 [{}] 的检测结果...", batchId);
        LocalDateTime now = LocalDateTime.now();

        // 2. 解析时间
        LocalDateTime detectionTime = null;
        if (result.getTimestamp() != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                detectionTime = LocalDateTime.parse(result.getTimestamp(), formatter);
            } catch (Exception e) {
                log.warn("时间格式解析失败: {}", result.getTimestamp());
                detectionTime = now;
            }
        }

        // 3. 组装 DetectionBatch 并保存
        DetectionBatch batch = new DetectionBatch();
        batch.setBatchId(batchId);
        batch.setDetectionTime(detectionTime);
        batch.setRuntime(result.getRuntime());

        // Batch 信息
        if (result.getBatch() != null) {
            batch.setExpectedImages(result.getBatch().getExpectedImages());
            batch.setActualImages(result.getBatch().getActualImages());
            batch.setImageComplete(result.getBatch().getImageComplete());
        }

        // Collection 信息
        if (result.getCollection() != null) {
            batch.setEndpointNormalCount(result.getCollection().getEndpointNormalCount());
            batch.setEndpointNormalRequired(result.getCollection().getEndpointNormalRequired());
            batch.setEndpointComplete(result.getCollection().getEndpointComplete());
            batch.setTrustedCollection(result.getCollection().getTrustedCollection());
        }

        // Defect 信息
        if (result.getDefect() != null) {
            batch.setScratchCount(result.getDefect().getScratchCount());
            batch.setScratchImageCount(result.getDefect().getScratchImageCount());
        }

        // Quality 信息
        if (result.getQuality() != null) {
            batch.setUnclearImageCount(result.getQuality().getUnclearImageCount());
            batch.setUnclearRoiCount(result.getQuality().getUnclearRoiCount());
        }

        // Qwen 信息
        if (result.getQwen() != null) {
            batch.setQwenStatus(result.getQwen().getStatus());
            batch.setQwenReport(result.getQwen().getReport());
            batch.setQwenSeverity(result.getQwen().getSeverity());
            batch.setQwenNeedRecheck(result.getQwen().getNeedRecheck());
            batch.setQwenDisposalAdvice(result.getQwen().getDisposalAdvice());
        }

        // FinalResult 信息
        if (result.getFinalResult() != null) {
            batch.setFinalStatus(result.getFinalResult().getStatus());
            batch.setNeedRecheck(result.getFinalResult().getNeedRecheck());
            batch.setDisposalAdvice(result.getFinalResult().getDisposalAdvice());
        }

        batch.setCreateTime(now);
        batch.setUpdateTime(now);

        // 插入批次表
        detectionBatchMapper.insert(batch);

        // 4. 遍历图片并保存
        List<ModelResultVO.ImageResult> images = result.getImages();
        if (images != null && !images.isEmpty()) {
            for (ModelResultVO.ImageResult imgRes : images) {
                DetectionImage image = new DetectionImage();
                image.setBatchId(batchId);
                image.setFilename(imgRes.getFilename());
                image.setStatus(imgRes.getStatus());
                image.setEndpointNormalCount(imgRes.getEndpointNormalCount());
                image.setScratchCount(imgRes.getScratchCount());
                image.setUnclearRoiCount(imgRes.getUnclearRoiCount());
                image.setCreateTime(now);

                // 插入图片表，MyBatis-Plus 插入后会自动回填 ID (前提是实体配置了 @TableId(type = IdType.AUTO))
                detectionImageMapper.insert(image);

                // 5. 遍历缺陷图片 Base64 并保存
                List<ModelResultVO.DefectImage> defectImages = imgRes.getDefectImages();
                if (defectImages != null && !defectImages.isEmpty()) {
                    for (ModelResultVO.DefectImage defImgRes : defectImages) {
                        DetectionDefectImage defectImage = new DetectionDefectImage();
                        defectImage.setBatchId(batchId);
                        defectImage.setDetectionImageId(image.getId()); // 关联图片主键
                        defectImage.setDefectIndex(defImgRes.getDefectIndex());
                        defectImage.setImageBase64(defImgRes.getImageBase64());
                        defectImage.setContentType(defImgRes.getContentType());
                        defectImage.setCreateTime(now);

                        // 插入缺陷图片表
                        detectionDefectImageMapper.insert(defectImage);
                    }
                }
            }
        }

        log.info("批次 [{}] 的检测结果保存成功！", batchId);
    }
}