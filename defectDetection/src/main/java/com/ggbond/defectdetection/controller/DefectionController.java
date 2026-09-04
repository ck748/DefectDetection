package com.ggbond.defectdetection.controller;

import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.DefectionCategory;
import com.ggbond.defectdetection.service.DefectionCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 缺陷类别控制器
 */
@RestController
@Slf4j
@ResponseBody
@RequestMapping("/defection")

public class DefectionController {

    @Autowired
    private DefectionCategoryService defectionCategoryService;

    /**
     * 获取所有缺陷类别
     */
    @GetMapping("/categories")
    public Result getCategories() {
        try {
            List<DefectionCategory> categories = defectionCategoryService.list();
            log.info("获取缺陷类别成功，共{}个", categories.size());
            return Result.success("获取缺陷类别成功", categories);
        } catch (Exception e) {
            log.error("获取缺陷类别失败: {}", e.getMessage(), e);
            return Result.fail("获取缺陷类别失败: " + e.getMessage());
        }
    }
}
