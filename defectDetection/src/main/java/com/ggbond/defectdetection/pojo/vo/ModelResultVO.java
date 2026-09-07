package com.ggbond.defectdetection.pojo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Python YOLO + Qwen 模型检测结果
 */
@Data
public class ModelResultVO {

    /**
     * 返回码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 批次ID
     */
    private String batchId;

    /**
     * 检测时间
     */
    private String timestamp;

    /**
     * 模型检测耗时，单位：秒
     */
    private Double runtime;

    /**
     * 批次信息
     */
    private Batch batch;

    /**
     * 采集完整性
     */
    private Collection collection;

    /**
     * 缺陷统计
     */
    private Defect defect;

    /**
     * 图片质量
     */
    private Quality quality;

    /**
     * Qwen综合分析结果
     */
    private Qwen qwen;

    /**
     * 最终检测结论
     */
    @JsonProperty("final")
    private FinalResult finalResult;

    /**
     * 每张图片检测结果
     */
    private List<ImageResult> images;


    // =========================================================
    // 批次信息
    // =========================================================

    @Data
    public static class Batch {

        /**
         * 标准图片数量
         */
        private Integer expectedImages;

        /**
         * 实际图片数量
         */
        private Integer actualImages;

        /**
         * 图片数量是否完整
         */
        private Boolean imageComplete;
    }


    // =========================================================
    // 采集完整性
    // =========================================================

    @Data
    public static class Collection {

        /**
         * endpoint_normal检测数量
         */
        private Integer endpointNormalCount;

        /**
         * endpoint_normal要求数量
         */
        private Integer endpointNormalRequired;

        /**
         * endpoint_normal是否完整
         */
        private Boolean endpointComplete;

        /**
         * 是否属于可信采集
         */
        private Boolean trustedCollection;
    }


    // =========================================================
    // 缺陷统计
    // =========================================================

    @Data
    public static class Defect {

        /**
         * scratch缺陷总数量
         */
        private Integer scratchCount;

        /**
         * 存在scratch的图片数量
         */
        private Integer scratchImageCount;

        /**
         * 缺陷类型统计
         *
         * 例如：
         * {
         *     "裂痕": 2
         * }
         */
        private Map<String, Integer> defectTypes;
    }


    // =========================================================
    // 图片质量
    // =========================================================

    @Data
    public static class Quality {

        /**
         * 存在不清晰ROI的图片数量
         */
        private Integer unclearImageCount;

        /**
         * 不清晰ROI数量
         */
        private Integer unclearRoiCount;

        /**
         * 不清晰图片名称
         */
        private List<String> unclearImages;
    }


    // =========================================================
    // Qwen结果
    // =========================================================

    @Data
    public static class Qwen {

        /**
         * Qwen状态
         *
         * COMPLETED / ERROR
         */
        private String status;

        /**
         * Qwen综合检测报告
         */
        private String report;

        /**
         * Qwen判断出的缺陷类型
         *
         * 例如：
         * ["裂痕"]
         */
        private List<String> defectTypes;

        /**
         * 严重程度
         *
         * 严重 / 一般 / 轻微 / 正常
         */
        private String severity;

        /**
         * 是否需要复检
         */
        private Boolean needRecheck;

        /**
         * 处置建议
         */
        private String disposalAdvice;
    }


    // =========================================================
    // 最终结论
    // =========================================================

    @Data
    public static class FinalResult {

        /**
         * 最终状态
         *
         * OK
         * NG
         * RECHECK
         */
        private String status;

        /**
         * 是否需要复检
         */
        private Boolean needRecheck;

        /**
         * 最终处置建议
         */
        private String disposalAdvice;
    }


    // =========================================================
    // 单张图片结果
    // =========================================================

    @Data
    public static class ImageResult {

        /**
         * 图片文件名
         */
        private String filename;

        /**
         * 当前图片状态
         */
        private String status;

        /**
         * endpoint_normal数量
         */
        @JsonProperty("endpoint_normal_count")
        private Integer endpointNormalCount;

        /**
         * scratch数量
         */
        @JsonProperty("scratch_count")
        private Integer scratchCount;

        /**
         * 不清晰ROI数量
         */
        @JsonProperty("unclear_roi_count")
        private Integer unclearRoiCount;

        /**
         * 缺陷图片
         */
        @JsonProperty("defect_images")
        private List<DefectImage> defectImages;
    }


    // =========================================================
    // 缺陷Base64图片
    // =========================================================

    @Data
    public static class DefectImage {

        /**
         * 当前图片中的缺陷序号
         */
        private Integer defectIndex;

        /**
         * Base64图片内容
         */
        private String imageBase64;

        /**
         * 图片类型
         *
         * image/jpeg
         */
        private String contentType;
    }
}