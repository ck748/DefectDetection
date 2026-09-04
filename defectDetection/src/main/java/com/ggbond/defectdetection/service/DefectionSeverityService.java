package com.ggbond.defectdetection.service;

import com.ggbond.defectdetection.pojo.Defection;
import org.springframework.stereotype.Service;

@Service
public class DefectionSeverityService {

    /**
     * 评估缺陷严重程度并生成修复建议
     */
    public void evaluateDefection(Defection defection) {
        int severityLevel = calculateSeverityLevel(defection);
        String suggestion = generateRepairSuggestion(defection, severityLevel);

        defection.setSeverityLevel(severityLevel);
        defection.setRepairSuggestion(suggestion);
    }

    /**
     * 计算严重程度等级 (1-5级)
     * 基于: 置信度score、缺陷尺寸(l*h)、缺陷类型category
     */
    private int calculateSeverityLevel(Defection defection) {
        double score = defection.getScore();
        double area = defection.getL() * defection.getH();
        String category = defection.getCategory();

        // 基础分数 (基于置信度)
        double baseScore = score * 3; // 0-3分

        // 尺寸影响 (面积越大越严重)
        double sizeScore = Math.min(area * 2, 2.0); // 0-2分

        // 类型权重
        double categoryWeight = getCategoryWeight(category);

        // 综合评分
        double totalScore = (baseScore + sizeScore) * categoryWeight;

        // 映射到1-5级
        if (totalScore < 1.0) return 1;
        if (totalScore < 2.0) return 2;
        if (totalScore < 3.0) return 3;
        if (totalScore < 4.0) return 4;
        return 5;
    }

    /**
     * 不同缺陷类型的权重系数
     */
    private double getCategoryWeight(String category) {
        if (category == null) return 1.0;

        switch (category) {
            case "划痕": return 0.8;
            case "凹陷": return 1.2;
            case "突出": return 1.1;
            case "抛光": return 0.7;
            default: return 1.0;
        }
    }

    /**
     * 生成修复建议
     */
    private String generateRepairSuggestion(Defection defection, int level) {
        String category = defection.getCategory();

        StringBuilder suggestion = new StringBuilder();
        suggestion.append("严重程度: ").append(level).append("级 | ");

        // 根据缺陷类型提供建议
        switch (category) {
            case "划痕":
                if (level <= 2) {
                    suggestion.append("轻微划痕,建议使用抛光处理");
                } else if (level <= 4) {
                    suggestion.append("中度划痕,需要打磨后重新喷涂");
                } else {
                    suggestion.append("严重划痕,建议更换部件或深度修复");
                }
                break;

            case "凹陷":
                if (level <= 2) {
                    suggestion.append("轻微凹陷,可使用钣金修复");
                } else if (level <= 4) {
                    suggestion.append("中度凹陷,需要专业钣金整形");
                } else {
                    suggestion.append("严重凹陷,建议更换受损部件");
                }
                break;

            case "突出":
                if (level <= 2) {
                    suggestion.append("轻微突出,打磨平整即可");
                } else if (level <= 4) {
                    suggestion.append("中度突出,需要切割打磨处理");
                } else {
                    suggestion.append("严重突出,建议重新加工或更换");
                }
                break;

            case "抛光":
                if (level <= 2) {
                    suggestion.append("抛光不均,重新抛光处理");
                } else {
                    suggestion.append("抛光严重不合格,需要重新打磨抛光");
                }
                break;

            default:
                suggestion.append("未知缺陷类型,建议人工检查");
        }

        // 添加尺寸信息
        double area = defection.getL() * defection.getH();
        suggestion.append(" | 缺陷面积: ").append(String.format("%.4f", area));

        return suggestion.toString();
    }
}