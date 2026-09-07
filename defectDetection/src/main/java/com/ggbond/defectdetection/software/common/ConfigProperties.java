package com.ggbond.defectdetection.software.common;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 配置信息类,支持从 ClassPath / 外部文件读取与保存
 */
@Data
@Slf4j
@Component("ConfigProperties")
public class ConfigProperties {

    public static ConfigProperties properties = new ConfigProperties();

    private ModelConfig modelConfig = new ModelConfig();
    private ChartsConfig chartsConfig = new ChartsConfig();
    private WarnsConfig warnsConfig = new WarnsConfig();
    private RunningConfig runningConfig = new RunningConfig();

    private static final String EXTERNAL_CONFIG_FILE = "config/config.json";
    private static final String SRC_CONFIG_FILE = "src/main/resources/config.json";

    interface Savable {
        void saveConfig();
    }

    private static void saveJsonConfig(String sectionKey, Object sectionValue) {
        try {
            String content = readConfigString();
            JSONObject jsonObject = JSONObject.parseObject(content);
            if (jsonObject == null) {
                jsonObject = new JSONObject();
            }
            jsonObject.put(sectionKey, sectionValue);
            String updatedContent = jsonObject.toJSONString();

            // 优先写入外部挂载目录 config/，其次尝试源码路径
            Path targetPath = Paths.get(EXTERNAL_CONFIG_FILE);
            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }
            Files.write(targetPath, updatedContent.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("保存配置信息失败: {}", e.getMessage());
        }
    }

    private static String readConfigString() {
        // 1. 尝试从外部配置文件读取 (如 Docker volume 挂载的 config/config.json)
        try {
            Path extPath = Paths.get(EXTERNAL_CONFIG_FILE);
            if (Files.exists(extPath)) {
                return new String(Files.readAllBytes(extPath), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}

        // 2. 尝试从本地开发目录读取 (src/main/resources/config.json)
        try {
            Path srcPath = Paths.get(SRC_CONFIG_FILE);
            if (Files.exists(srcPath)) {
                return new String(Files.readAllBytes(srcPath), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}

        // 3. 从 Jar 包类路径读取 (ClassPathResource)
        try {
            ClassPathResource resource = new ClassPathResource("config.json");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.warn("从 ClassPath 读取 config.json 失败: {}", e.getMessage());
        }

        // 4. 默认兜底 JSON，保证绝不抛出 NullPointerException
        return "{\"model\":{\"id\":1,\"ip\":\"127.0.0.1\",\"port\":8090,\"resStoragePath\":\"./detectPicture\"},\"charts\":{\"granularity\":10},\"warns\":{\"continuousWorkingMinutesWarningLine\":3600,\"defectiveRateWarningLine\":1.0,\"waringInterval\":360},\"running\":{\"detectInterval\":5}}";
    }

    @Data
    public static class ModelConfig implements Savable {
        int id = 1;
        String ip = "127.0.0.1";
        int port = 8090;
        String resStoragePath = "./detectPicture";

        @Override
        public void saveConfig() {
            saveJsonConfig("model", this);
        }
    }

    @Data
    public static class ChartsConfig implements Savable {
        int granularity = 10;

        @Override
        public void saveConfig() {
            saveJsonConfig("charts", this);
        }
    }

    @Data
    public static class WarnsConfig implements Savable {
        double defectiveRateWarningLine = 1.0;
        int continuousWorkingMinutesWarningLine = 3600;
        Long waringInterval = 360L;

        @Override
        public void saveConfig() {
            saveJsonConfig("warns", this);
        }
    }

    @Data
    public static class RunningConfig implements Savable {
        int detectInterval = 5;

        @Override
        public void saveConfig() {
            saveJsonConfig("running", this);
        }
    }

    @PostConstruct
    public void loadProperties() {
        try {
            String content = readConfigString();
            JSONObject jsonObject = JSONObject.parseObject(content);
            if (jsonObject == null) {
                jsonObject = new JSONObject();
            }

            // 获取模型配置
            String modelConfigStr = jsonObject.getString("model");
            if (modelConfigStr != null) {
                this.modelConfig = JSONObject.parseObject(modelConfigStr, ModelConfig.class);
            }

            // 获取图表配置
            String chartsConfigStr = jsonObject.getString("charts");
            if (chartsConfigStr != null) {
                this.chartsConfig = JSONObject.parseObject(chartsConfigStr, ChartsConfig.class);
            }

            // 加载告警配置
            String warnsConfigStr = jsonObject.getString("warns");
            if (warnsConfigStr != null) {
                this.warnsConfig = JSONObject.parseObject(warnsConfigStr, WarnsConfig.class);
            }

            // 加载运行配置信息
            String runningConfigStr = jsonObject.getString("running");
            if (runningConfigStr != null) {
                this.runningConfig = JSONObject.parseObject(runningConfigStr, RunningConfig.class);
            }

            // 保证非空兜底
            if (this.modelConfig == null) this.modelConfig = new ModelConfig();
            if (this.chartsConfig == null) this.chartsConfig = new ChartsConfig();
            if (this.warnsConfig == null) this.warnsConfig = new WarnsConfig();
            if (this.runningConfig == null) this.runningConfig = new RunningConfig();

            // 赋值静态单例
            properties.modelConfig = this.modelConfig;
            properties.chartsConfig = this.chartsConfig;
            properties.warnsConfig = this.warnsConfig;
            properties.runningConfig = this.runningConfig;
            log.info("【系统配置】ConfigProperties 初始化加载成功");
        } catch (Exception e) {
            log.error("【系统配置】ConfigProperties 初始化异常: {}", e.getMessage(), e);
        }
    }

    public void saveProperties() {
        if (modelConfig != null) modelConfig.saveConfig();
        if (chartsConfig != null) chartsConfig.saveConfig();
        if (warnsConfig != null) warnsConfig.saveConfig();
        if (runningConfig != null) runningConfig.saveConfig();
    }
}
