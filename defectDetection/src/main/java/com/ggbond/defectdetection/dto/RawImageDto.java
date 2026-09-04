package com.ggbond.defectdetection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 原始图片数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RawImageDto {
    private Integer id;
    private String imageName;
    private String imagePath;
    private Long imageSize;
    private Integer imageWidth;
    private Integer imageHeight;
    private String uploadSource;
    private Integer deviceId;
    private Integer workOrderId;
    private LocalDateTime uploadTime;
    private Integer uploadUserId;
    private String uploadUserName;
    private Integer status;
    private String remark;
}