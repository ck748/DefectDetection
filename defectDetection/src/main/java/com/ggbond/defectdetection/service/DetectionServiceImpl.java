package com.ggbond.defectdetection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggbond.defectdetection.mapper.DetectionBatchMapper;
import com.ggbond.defectdetection.mapper.DetectionDefectImageMapper;
import com.ggbond.defectdetection.mapper.DetectionImageMapper;
import com.ggbond.defectdetection.pojo.DetectionBatch;
import com.ggbond.defectdetection.pojo.DetectionDefectImage;
import com.ggbond.defectdetection.pojo.DetectionImage;
import com.ggbond.defectdetection.pojo.vo.ModelResultVO;
import com.ggbond.defectdetection.service.DetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : poi
 * @project : defectDetection
 * @package : com.ggbond.defectdetection.service.impl
 * @className : DetectionServiceImpl
 * @createTime : 2026/9/6 14:22
 */
@Service
@RequiredArgsConstructor
public class DetectionServiceImpl extends ServiceImpl<DetectionBatchMapper, DetectionBatch> implements DetectionService {

    private final DetectionImageMapper detectionImageMapper;
    
    private final DetectionDefectImageMapper detectionDefectImageMapper;

    @Override
    public Page<DetectionBatch> page(long current, long size) {
        // 分页查询并按创建时间倒序
        return this.page(new Page<>(current, size), 
                new LambdaQueryWrapper<DetectionBatch>().orderByDesc(DetectionBatch::getCreateTime));
    }

    @Override
    public ModelResultVO getLatestDetail() {
        // 获取最新的一条 batch 记录
        DetectionBatch latestBatch = this.getOne(
                new LambdaQueryWrapper<DetectionBatch>()
                        .orderByDesc(DetectionBatch::getCreateTime)
                        .last("limit 1")
        );
        if (latestBatch == null) {
            return null;
        }
        // 复用 getDetail 逻辑
        return this.getDetail(latestBatch.getBatchId());
    }

    @Override
    public ModelResultVO getDetail(String batchId) {
        // 1. 根据 batchId 查询 DetectionBatch
        DetectionBatch batch = this.getOne(
                new LambdaQueryWrapper<DetectionBatch>().eq(DetectionBatch::getBatchId, batchId)
        );
        if (batch == null) {
            return null;
        }

        // 2. 组装最外层信息
        ModelResultVO vo = new ModelResultVO();
        vo.setCode(200);
        vo.setMessage("success");
        vo.setBatchId(batch.getBatchId());
        if (batch.getDetectionTime() != null) {
            vo.setTimestamp(batch.getDetectionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        vo.setRuntime(batch.getRuntime());

        // 组装 Batch
        ModelResultVO.Batch batchInfo = new ModelResultVO.Batch();
        batchInfo.setExpectedImages(batch.getExpectedImages());
        batchInfo.setActualImages(batch.getActualImages());
        batchInfo.setImageComplete(batch.getImageComplete());
        vo.setBatch(batchInfo);

        // 组装 Collection
        ModelResultVO.Collection collection = new ModelResultVO.Collection();
        collection.setEndpointNormalCount(batch.getEndpointNormalCount());
        collection.setEndpointNormalRequired(batch.getEndpointNormalRequired());
        collection.setEndpointComplete(batch.getEndpointComplete());
        collection.setTrustedCollection(batch.getTrustedCollection());
        vo.setCollection(collection);

        // 组装 Defect
        ModelResultVO.Defect defect = new ModelResultVO.Defect();
        defect.setScratchCount(batch.getScratchCount());
        defect.setScratchImageCount(batch.getScratchImageCount());
        vo.setDefect(defect);

        // 组装 Quality
        ModelResultVO.Quality quality = new ModelResultVO.Quality();
        quality.setUnclearImageCount(batch.getUnclearImageCount());
        quality.setUnclearRoiCount(batch.getUnclearRoiCount());
        vo.setQuality(quality);

        // 组装 Qwen
        ModelResultVO.Qwen qwen = new ModelResultVO.Qwen();
        qwen.setStatus(batch.getQwenStatus());
        qwen.setReport(batch.getQwenReport());
        qwen.setSeverity(batch.getQwenSeverity());
        qwen.setNeedRecheck(batch.getQwenNeedRecheck());
        qwen.setDisposalAdvice(batch.getQwenDisposalAdvice());
        vo.setQwen(qwen);

        // 组装 FinalResult
        ModelResultVO.FinalResult finalResult = new ModelResultVO.FinalResult();
        finalResult.setStatus(batch.getFinalStatus());
        finalResult.setNeedRecheck(batch.getNeedRecheck());
        finalResult.setDisposalAdvice(batch.getDisposalAdvice());
        vo.setFinalResult(finalResult);

        // 3. 查询这个 batchId 下的 DetectionImage
        List<DetectionImage> images = detectionImageMapper.selectList(
                new LambdaQueryWrapper<DetectionImage>().eq(DetectionImage::getBatchId, batchId)
        );

        List<ModelResultVO.ImageResult> imageResults = new ArrayList<>();
        
        // 4. 遍历每张图片
        for (DetectionImage img : images) {
            ModelResultVO.ImageResult imgRes = new ModelResultVO.ImageResult();
            imgRes.setFilename(img.getFilename());
            imgRes.setStatus(img.getStatus());
            imgRes.setEndpointNormalCount(img.getEndpointNormalCount());
            imgRes.setScratchCount(img.getScratchCount());
            imgRes.setUnclearRoiCount(img.getUnclearRoiCount());
            
            // 5. 查询该图片对应的 DetectionDefectImage
            List<DetectionDefectImage> defectImages = detectionDefectImageMapper.selectList(
                    new LambdaQueryWrapper<DetectionDefectImage>().eq(DetectionDefectImage::getDetectionImageId, img.getId())
            );
            
            List<ModelResultVO.DefectImage> defectImageResults = new ArrayList<>();
            for (DetectionDefectImage defImg : defectImages) {
                ModelResultVO.DefectImage defImgRes = new ModelResultVO.DefectImage();
                defImgRes.setDefectIndex(defImg.getDefectIndex());
                defImgRes.setImageBase64(defImg.getImageBase64());
                defImgRes.setContentType(defImg.getContentType());
                defectImageResults.add(defImgRes);
            }
            imgRes.setDefectImages(defectImageResults);
            
            imageResults.add(imgRes);
        }
        
        // 6. 放入 VO
        vo.setImages(imageResults);

        return vo;
    }
}
