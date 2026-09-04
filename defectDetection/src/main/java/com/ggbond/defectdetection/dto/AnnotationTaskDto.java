package com.ggbond.defectdetection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标注任务数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnotationTaskDto {
    private Integer id;
    private String taskName;
    private String taskType;
    private String description;
    private Integer imageCount;
    private Integer annotatedCount;
    private Integer status;
    private Integer priority;
    private LocalDateTime createTime;
    private Integer createUserId;
    private String createUserName;
    private Integer assignUserId;
    private String assignUserName;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private LocalDateTime deadline;
    private String remark;
}