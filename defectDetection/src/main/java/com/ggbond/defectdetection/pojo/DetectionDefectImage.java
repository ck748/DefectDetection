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
 * @className : DetectionDefectImage
 * @createTime : 2026/9/6 14:08
 */
@Data
@TableName("detection_defect_image")
@AllArgsConstructor
@NoArgsConstructor
public class DetectionDefectImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchId;

    private Long detectionImageId;

    private Integer defectIndex;

    private String imageBase64;

    private String contentType;

    private LocalDateTime createTime;
}