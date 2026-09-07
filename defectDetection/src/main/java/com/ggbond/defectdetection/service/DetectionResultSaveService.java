package com.ggbond.defectdetection.service;

import com.ggbond.defectdetection.pojo.vo.ModelResultVO;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : poi
 * @project : defectDetection
 * @package : com.ggbond.defectdetection.service
 * @className : DetectionResultSaveService
 * @createTime : 2026/9/6 14:30
 */
public interface DetectionResultSaveService {

    /**
     * 保存一批完整检测结果 (核心为写)
     */
    void saveResult(ModelResultVO result);
}