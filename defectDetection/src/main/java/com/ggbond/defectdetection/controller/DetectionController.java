package com.ggbond.defectdetection.controller;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.DetectionBatch;
import com.ggbond.defectdetection.pojo.vo.ModelResultVO;
import com.ggbond.defectdetection.service.DetectionResultSaveService;
import com.ggbond.defectdetection.service.DetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : poi
 * @project : defectDetection
 * @package : com.ggbond.defectdetection.controller
 * @className : DetectionController
 * @createTime : 2026/9/6 14:35
 */
@RestController
@RequestMapping("/detection/batch")
@RequiredArgsConstructor
@Slf4j
public class DetectionController {

    private final DetectionService detectionService;
    private final DetectionResultSaveService detectionResultSaveService;

    // Python 结果获取接口地址
    private static final String PYTHON_RESULT_API = "http://192.168.1.3:8090/result";

    /**
     * 接收 Python 端主动推送的检测结果 (回流接口)
     *
     * @param result Python传递过来的检测结果JSON
     * @return 成功或失败信息
     */
    @PostMapping("/callback")
    public Result<String> saveCallback(@RequestBody ModelResultVO result) {
        log.info("接收到 Python 端推送的检测结果, batchId: {}", result.getBatchId());
        try {
            detectionResultSaveService.saveResult(result);
            return Result.success("数据入库成功");
        } catch (Exception e) {
            log.error("保存 Python 推送数据失败: {}", e.getMessage(), e);
            return Result.fail("数据入库失败");
        }
    }

    /**
     * 分页查询检测批次记录
     *
     * @param current 当前页码
     * @param size    每页数量
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<Page<DetectionBatch>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        log.info("分页查询检测记录, current: {}, size: {}", current, size);
        Page<DetectionBatch> pageInfo = detectionService.page(current, size);
        return Result.success("查询成功", pageInfo);
    }

    /**
     * 获取最新检测结果 (直接读取 Python 接口，不读数据库)
     *
     * @return 完整的VO数据
     */
    @GetMapping("/latest")
    public Result<ModelResultVO> getLatest() {
        log.info("触发主动从Python获取最新检测结果...");
        
        try {
            // 尝试从 Python 接口获取最新结果 (设置5秒超时)
            String response = HttpUtil.get(PYTHON_RESULT_API, 5000);
            ModelResultVO pythonResult = JSON.parseObject(response, ModelResultVO.class);
            
            if (pythonResult != null && pythonResult.getBatchId() != null) {
                log.info("从 Python 端获取到最新批次数据 [{}]，直接返回给前端", pythonResult.getBatchId());
                return Result.success("获取最新检测结果成功", pythonResult);
            } else {
                return Result.fail("从 Python 获取的数据解析为空或缺失 batchId");
            }
        } catch (Exception e) {
            log.error("从 Python 获取最新检测结果失败: {}", e.getMessage(), e);
            return Result.fail("从 Python 获取最新检测结果失败，请检查 Python 服务是否正常");
        }
    }

    /**
     * 根据批次ID查询完整检测结果 (ModelResultVO) (纯数据库查询)
     *
     * @param batchId 批次ID
     * @return 完整的VO数据
     */
    @GetMapping("/detail/{batchId}")
    public Result<ModelResultVO> getDetail(@PathVariable String batchId) {
        log.info("查询批次详情, batchId: {}", batchId);
        ModelResultVO detail = detectionService.getDetail(batchId);
        
        if (detail == null) {
            return Result.fail("未找到该批次的检测结果");
        }
        return Result.success("查询成功", detail);
    }
}