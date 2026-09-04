package com.ggbond.defectdetection.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: 19461
 * Date: 2024/2/15
 * Updated for Axle Defect Quality Warnings
 */
@Data
public class Warnings {
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 半轴编号 (如 2403511-P301, CN744139 等) */
    @TableField(value = "axle_code", exist = true)
    private String axleCode;

    /** 缺陷名称 (划痕、裂纹) */
    private String type;

    /** 质量类别 (内部探伤缺陷、外观表面缺陷、表面微裂纹等) */
    @TableField(value = "category", exist = true)
    private String category;

    /** 紧急程度 (1: 提示, 2: 警告, 3: 高危) */
    private Integer level;

    /** 发生时间 */
    private LocalDateTime createTime;

    /** 质量分析 (预留对接AI质检专家报告，可为空) */
    private String content;
}
