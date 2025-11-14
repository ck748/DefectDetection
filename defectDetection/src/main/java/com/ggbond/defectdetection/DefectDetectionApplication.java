package com.ggbond.defectdetection;

import com.ggbond.defectdetection.common.SpringContextUtil;
import com.ggbond.defectdetection.software.common.Software;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.swing.*;

@SpringBootApplication

@Slf4j
public class DefectDetectionApplication {

    // 应用启动标志，防止重复启动
    private static volatile boolean isApplicationStarted = false;
    
    // 初始化计数器，用于监控重复初始化
    private static int initializationCount = 0;

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        // 检查是否已经启动
        if (isApplicationStarted) {
            System.err.println("⚠️ 警告：应用已经启动，忽略重复启动请求");
            return;
        }
        isApplicationStarted = true;
        System.out.println("✅ 开始启动智检控-精密元件智能监测系统...");
        System.setProperty("sun.java2d.noddraw", "true");
        new SpringApplicationBuilder(DefectDetectionApplication.class).headless(false). run(args);
        ApplicationContext context=SpringContextUtil.getApplicationContext();
        Software software=context.getBean(Software.class);
//        Web web=context.getBean(Web.class);
        Environment environment = context.getBean(Environment.class);

        SwingUtilities.invokeLater(software::init);

        log.info("---------------------------------------------------------");
        log.info("Swagger访问路径：http://127.0.0.1:"+environment.getProperty("server.port")+"/swagger-ui/index.html");
        log.info("---------------------------------------------------------");
    }

    // 项目启动后打印加载的数据源配置
    @PostConstruct
    public void printDataSourceConfig() {
        initializationCount++;
        System.out.println("\n========================================");
        System.out.println("📊 初始化次数：第 " + initializationCount + " 次");
        if (initializationCount > 1) {
            System.err.println("⚠️ 警告：检测到重复初始化！这可能导致资源浪费和界面重复创建。");
        }
        System.out.println("=== 加载的数据源配置 ===");
        System.out.println("URL: " + env.getProperty("spring.datasource.url"));
        System.out.println("Username: " + env.getProperty("spring.datasource.username"));
        System.out.println("Password: " + env.getProperty("spring.datasource.password")); // 可临时打印，排查后删除
        System.out.println("Driver: " + env.getProperty("spring.datasource.driver-class-name"));
        System.out.println("========================================\n");
    }

}
