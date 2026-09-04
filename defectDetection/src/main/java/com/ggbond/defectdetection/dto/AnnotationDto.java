package com.ggbond.defectdetection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标注数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnotationDto {
    private Integer id;
    private Integer rawImageId;
    private Integer taskId;
    private String category;
    private Integer categoryId;
    private Double x;
    private Double y;
    private Double width;
    private Double height;
    private Double confidence;
    private Integer isDifficult;
    private Integer annotatorId;
    private String annotatorName;
    private LocalDateTime annotationTime;
    private Integer isVerified;
    private Integer verifierId;
    private LocalDateTime verifyTime;
    private String remark;
}