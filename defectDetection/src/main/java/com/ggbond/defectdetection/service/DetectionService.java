package com.ggbond.defectdetection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ggbond.defectdetection.pojo.DetectionBatch;
import com.ggbond.defectdetection.pojo.vo.ModelResultVO;

public interface DetectionService extends IService<DetectionBatch> {
    /**
    * 分页查询检测记录
    */
    Page<DetectionBatch> page(long current, long size);

    /**
     * 查询检测详情
     */
    ModelResultVO getDetail(String batchId);

    /**
     * 获取最新的一条检测结果详情
     */
    ModelResultVO getLatestDetail();
}
