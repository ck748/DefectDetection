package com.ggbond.defectdetection.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ggbond.defectdetection.pojo.Defection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI大模型分析服务
 * 用于缺陷等级评估和修复建议生成
 */
@Service
@Slf4j
public class AIAnalysisService {

    @Value("${ai.mode:local}")
    private String aiMode;

    // 本地Ollama配置
    @Value("${ai.local.url:http://localhost:11434/v1/chat/completions}")
    private String localUrl;
    
    @Value("${ai.local.model:qwen2.5:7b}")
    private String localModel;

    // 云端API配置
    @Value("${ai.cloud.url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}")
    private String cloudUrl;
    
    @Value("${ai.cloud.key:}")
    private String cloudApiKey;
    
    @Value("${ai.cloud.model:qwen-turbo}")
    private String cloudModel;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 使用AI分析缺陷列表，返回等级和修复建议
     */
    public Map<String, Object> analyzeDefections(List<Defection> defections) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 构建提示词
            String prompt = buildPrompt(defections);
            
            // 调用AI API
            String aiResponse = callAIAPI(prompt);
            
            // 解析AI响应
            result = parseAIResponse(aiResponse, defections);
            result.put("success", true);
            
        } catch (Exception e) {
            log.error("AI分析失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "AI分析服务暂时不可用，请稍后重试");
            
            // 使用本地算法作为后备方案
            result.putAll(fallbackAnalysis(defections));
        }
        
        return result;
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(List<Defection> defections) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的工业缺陷检测专家。请分析以下产品缺陷，并为每个缺陷评估严重等级（1-5级，5级最严重）和提供修复建议。\n\n");
        prompt.append("缺陷信息如下：\n");
        
        for (int i = 0; i < defections.size(); i++) {
            Defection d = defections.get(i);
            prompt.append(String.format("缺陷%d: \n", i + 1));
            prompt.append(String.format("  - 类型: %s\n", d.getCategory()));
            prompt.append(String.format("  - 置信度: %.2f\n", d.getScore()));
            prompt.append(String.format("  - 尺寸(长x高): %.2f x %.2f\n", d.getL(), d.getH()));
            prompt.append(String.format("  - 位置(x,y): (%.2f, %.2f)\n", d.getX(), d.getY()));
            prompt.append("\n");
        }
        
        prompt.append("请以JSON格式返回分析结果，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"overallAssessment\": \"整体质量评估描述\",\n");
        prompt.append("  \"overallSeverity\": 整体严重等级数字(1-5),\n");
        prompt.append("  \"defections\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"index\": 1,\n");
        prompt.append("      \"severityLevel\": 3,\n");
        prompt.append("      \"repairSuggestion\": \"具体修复建议\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    /**
     * 调用AI API
     */
    private String callAIAPI(String prompt) {
        // 根据模式选择URL和模型
        String apiUrl;
        String model;
        boolean needAuth;
        
        if ("local".equalsIgnoreCase(aiMode)) {
            // 本地Ollama模式
            apiUrl = localUrl;
            model = localModel;
            needAuth = false;
            log.info("使用本地Ollama模型: {}", model);
        } else {
            // 云端API模式
            apiUrl = cloudUrl;
            model = cloudModel;
            needAuth = true;
            
            if (cloudApiKey == null || cloudApiKey.isEmpty()) {
                throw new RuntimeException("AI API Key未配置");
            }
            log.info("使用云端AI模型: {}", model);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // 只有云端模式需要认证
        if (needAuth) {
            headers.setBearerAuth(cloudApiKey);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", new Object[]{
            Map.of("role", "user", "content", prompt)
        });
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            JSONObject jsonResponse = JSON.parseObject(response.getBody());
            return jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        } catch (Exception e) {
            log.error("调用AI API失败: {}", e.getMessage());
            throw new RuntimeException("AI API调用失败: " + e.getMessage());
        }
    }

    /**
     * 解析AI响应
     */
    private Map<String, Object> parseAIResponse(String aiResponse, List<Defection> defections) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 提取JSON部分
            String jsonStr = aiResponse;
            if (aiResponse.contains("```json")) {
                jsonStr = aiResponse.substring(
                    aiResponse.indexOf("```json") + 7,
                    aiResponse.lastIndexOf("```")
                ).trim();
            } else if (aiResponse.contains("```")) {
                jsonStr = aiResponse.substring(
                    aiResponse.indexOf("```") + 3,
                    aiResponse.lastIndexOf("```")
                ).trim();
            }
            
            JSONObject jsonResponse = JSON.parseObject(jsonStr);
            result.put("overallAssessment", jsonResponse.getString("overallAssessment"));
            result.put("overallSeverity", jsonResponse.getInteger("overallSeverity"));
            result.put("defections", jsonResponse.getJSONArray("defections"));
            
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());
            // 如果解析失败，使用后备方案
            return fallbackAnalysis(defections);
        }
        
        return result;
    }

    /**
     * 后备分析方案（当AI不可用时）
     */
    private Map<String, Object> fallbackAnalysis(List<Defection> defections) {
        Map<String, Object> result = new HashMap<>();
        
        DefectionSeverityService severityService = new DefectionSeverityService();
        
        int maxLevel = 0;
        for (Defection defection : defections) {
            severityService.evaluateDefection(defection);
            if (defection.getSeverityLevel() != null && defection.getSeverityLevel() > maxLevel) {
                maxLevel = defection.getSeverityLevel();
            }
        }
        String overallAssessment;
        if (maxLevel <= 2) {
            overallAssessment = "产品存在轻微缺陷，可以进行简单修复处理。建议在出厂前进行简单修复，整体质量可控。";
        } else if (maxLevel <= 3) {
            overallAssessment = "产品存在中等缺陷，需要专业修复处理。建议由专业技术人员进行修复后再出厂。";
        } else {
            overallAssessment = "产品存在严重缺陷，建议重点关注并采取必要措施。需要立即停止生产并进行全面检查和修复。";
        }
        
        result.put("overallAssessment", overallAssessment);
        result.put("overallSeverity", maxLevel);
        result.put("defections", defections);
        result.put("useFallback", true);
        
        return result;
    }
}
