package com.ggbond.defectdetection.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标注数据实体类
 * 对应数据库表：annotation_data
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("annotation_data")
public class AnnotationData {
    @TableId(type = IdType.AUTO)
    private Integer id;              // 主键ID
    private Integer rawImageId;      // 原始图片ID
    private Integer taskId;          // 任务ID
    private String category;         // 缺陷类别名称
    private Integer categoryId;      // 缺陷类别ID
    private Double x;                // X坐标
    private Double y;                // Y坐标
    private Double width;            // 框宽度
    private Double height;           // 框高度
    private Double confidence;       // 置信度
    private Integer isDifficult;     // 是否困难样本：0-否, 1-是
    private Integer annotatorId;     // 标注人ID
    private String annotatorName;    // 标注人名称
    private LocalDateTime annotationTime; // 标注时间
    private Integer isVerified;      // 是否已审核：0-未审核, 1-已审核, 2-审核不通过
    private Integer verifierId;      // 审核人ID
    private LocalDateTime verifyTime; // 审核时间
    
    @TableLogic  // 逻辑删除标记
    private Integer isDeleted;       // 逻辑删除
    
    private String remark;           // 备注
}