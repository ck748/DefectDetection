package com.ggbond.defectdetection.dto;

import com.ggbond.defectdetection.pojo.SysLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: 19461
 * Date: 2024/2/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardInfoDto {
    private int runTime;  // 运行时长（秒）
    private int defectionsSum;  // 缺陷总数
    private double defectRate;  // 缺陷率（0-1之间的小数）
    private String highestOccurrenceDefect;  // 最高发缺陷名称
    private List<SysLog> latestOperations;  // 最新操作记录列表
}