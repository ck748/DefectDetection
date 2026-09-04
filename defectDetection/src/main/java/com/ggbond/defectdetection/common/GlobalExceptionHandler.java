package com.ggbond.defectdetection.common;

import com.ggbond.defectdetection.service.LogRecordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 全局统一异常捕获并自动写入系统审计日志
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private LogRecordService logRecordService;

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();

        // 提取简短异常堆栈
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        if (stackTrace.length() > 500) {
            stackTrace = stackTrace.substring(0, 500) + "...";
        }

        log.error("❌ [系统全局拦截异常] 接口: {}, 错误: {}", uri, errorMsg, e);

        // 自动入库持久化为系统错误日志
        try {
            logRecordService.recordError("系统服务/报错", "接口异常", uri, "错误信息: " + errorMsg + " | 堆栈片段: " + stackTrace);
        } catch (Exception ex) {
            log.error("写入全局异常日志失败: {}", ex.getMessage());
        }

        return Result.fail("服务处理异常: " + errorMsg);
    }
}
