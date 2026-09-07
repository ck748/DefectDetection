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
 * @className : DetectionImage
 * @createTime : 2026/9/6 14:08
 */
@Data
@TableName("detection_image")
@AllArgsConstructor
@NoArgsConstructor
public class DetectionImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchId;

    private String filename;

    private String status;

    private Integer endpointNormalCount;

    private Integer scratchCount;

    private Integer unclearRoiCount;

    private LocalDateTime createTime;
}
