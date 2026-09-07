package com.ggbond.defectdetection.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : poi
 * @project : defectDetection
 * @package : com.ggbond.defectdetection.pojo
 * @className : DetectionBatch
 * @createTime : 2026/9/6 14:07
 */
@Data
@TableName("detection_batch")
@AllArgsConstructor
@NoArgsConstructor
public class DetectionBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchId;

    private LocalDateTime detectionTime;

    private Double runtime;

    private Integer expectedImages;

    private Integer actualImages;

    private Boolean imageComplete;

    private Integer endpointNormalCount;

    private Integer endpointNormalRequired;

    private Boolean endpointComplete;

    private Boolean trustedCollection;

    private Integer scratchCount;

    private Integer scratchImageCount;

    private Integer unclearImageCount;

    private Integer unclearRoiCount;

    private String qwenStatus;

    private String qwenReport;

    private String qwenSeverity;

    private Boolean qwenNeedRecheck;

    private String qwenDisposalAdvice;

    private String finalStatus;

    private Boolean needRecheck;

    private String disposalAdvice;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}