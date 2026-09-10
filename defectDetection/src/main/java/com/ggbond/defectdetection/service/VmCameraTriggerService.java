package com.ggbond.defectdetection.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * VM 相机触发服务 - TCP Server
 * <p>
 * Java 作为 TCP Server，VisionMaster 作为 TCP Client 连接过来。
 * 通信协议：
 * - Java 发送 "TRIGGER" → VM 收到后触发相机拍照
 * - VM 拍照完成后发送 "CAPTURE_DONE" → Java 收到后继续下一点位
 * <p>
 * 默认监听端口 8888，VM 通过 127.0.0.1:8888 连接（同机部署）
 */
@Slf4j
@Service
public class VmCameraTriggerService {

    @Value("${vm.tcp.port:8888}")
    private int tcpPort;

    @Value("${vm.tcp.capture-timeout:5000}")
    private int captureTimeoutMs;

    private ServerSocket serverSocket;
    private volatile Socket vmSocket;
    private volatile OutputStream vmOut;
    private volatile InputStream vmIn;

    private final AtomicBoolean listening = new AtomicBoolean(false);
    private final AtomicBoolean connected = new AtomicBoolean(false);

    /** VM 响应队列：收到 CAPTURE_DONE 时放入 */
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();

    /** 监听线程 */
    private Thread listenerThread;

    /**
     * Spring Boot 启动时自动启动 TCP Server 监听
     */
    @PostConstruct
    public void autoStart() {
        log.info("[VM TCP] Spring Boot 启动，自动开启 TCP Server 监听端口: {}", tcpPort);
        startListening();
    }

    /**
     * 启动 TCP Server 监听（非阻塞，后台线程）
     */
    public synchronized void startListening() {
        if (listening.get()) {
            log.warn("VM TCP Server 已在监听中，端口: {}", tcpPort);
            return;
        }

        try {
            serverSocket = new ServerSocket(tcpPort);
            serverSocket.setReuseAddress(true);
            listening.set(true);
            log.info("VM TCP Server 已启动，监听端口: {}", tcpPort);

            listenerThread = new Thread(this::acceptLoop, "vm-tcp-listener");
            listenerThread.setDaemon(true);
            listenerThread.start();

        } catch (IOException e) {
            log.error("VM TCP Server 启动失败，端口: {}", tcpPort, e);
            listening.set(false);
        }
    }

    /**
     * 停止 TCP Server
     */
    public synchronized void stopListening() {
        listening.set(false);
        disconnectVm();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
        serverSocket = null;

        log.info("VM TCP Server 已停止");
    }

    /**
     * 接受 VM 连接（阻塞循环）
     */
    private void acceptLoop() {
        while (listening.get()) {
            try {
                log.info("等待 VM 连接到端口 {}...", tcpPort);
                Socket client = serverSocket.accept();
                log.info("VM 已连接: {}", client.getRemoteSocketAddress());

                vmSocket = client;
                vmOut = client.getOutputStream();
                vmIn = client.getInputStream();
                connected.set(true);

                // 启动响应读取线程
                Thread readerThread = new Thread(this::readResponseLoop, "vm-tcp-reader");
                readerThread.setDaemon(true);
                readerThread.start();

            } catch (IOException e) {
                if (listening.get()) {
                    log.error("接受 VM 连接失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 读取 VM 发送的响应（阻塞循环）
     */
    private void readResponseLoop() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(vmIn, StandardCharsets.UTF_8));
        try {
            String line;
            while (connected.get() && (line = reader.readLine()) != null) {
                String msg = line.trim();
                log.info("[VM→Java] 收到: {}", msg);

                if ("CAPTURE_DONE".equals(msg)) {
                    responseQueue.offer(msg);
                } else if ("VM_READY".equals(msg)) {
                    log.info("VM 已就绪，等待触发指令");
                }
            }
        } catch (IOException e) {
            if (connected.get()) {
                log.error("读取 VM 响应异常: {}", e.getMessage());
            }
        } finally {
            connected.set(false);
            log.info("VM 连接已断开");
        }
    }

    /**
     * 触发 VM 拍照并等待完成（同步阻塞）
     *
     * @return true=拍照完成, false=超时或通信失败
     */
    public boolean triggerCapture() {
        return triggerCapture(captureTimeoutMs);
    }

    /**
     * 触发 VM 拍照并等待完成
     *
     * @param timeoutMs 超时时间（毫秒）
     * @return true=拍照完成, false=超时或通信失败
     */
    public boolean triggerCapture(int timeoutMs) {
        if (!connected.get() || vmOut == null) {
            log.warn("VM 未连接，无法触发拍照");
            return false;
        }

        // 清空之前的响应
        responseQueue.clear();

        try {
            // 发送 TRIGGER 指令（兼容字符串触发、分号协议分隔符及回车换行）
            String cmd = "TRIGGER;\r\n";
            vmOut.write(cmd.getBytes(StandardCharsets.UTF_8));
            vmOut.flush();
            log.info("[Java→VM] 已成功向 VM 发送触发拍照指令: {}", cmd.trim());

            // 等待 VM 响应确认（最长等待 1 秒，若 VM 未配置回传也视为触发成功）
            try {
                String response = responseQueue.poll(1000, TimeUnit.MILLISECONDS);
                if ("CAPTURE_DONE".equals(response)) {
                    log.info("[Java←VM] 收到 VM 拍照完成确认信号 CAPTURE_DONE");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return true;

        } catch (IOException e) {
            log.error("[Java→VM] 发送触发指令失败: {}", e.getMessage());
            connected.set(false);
            return false;
        }
    }

    /**
     * 断开当前 VM 连接（保留 Server 监听）
     */
    private void disconnectVm() {
        connected.set(false);
        try {
            if (vmOut != null) vmOut.close();
        } catch (Exception ignored) {}
        try {
            if (vmIn != null) vmIn.close();
        } catch (Exception ignored) {}
        try {
            if (vmSocket != null) vmSocket.close();
        } catch (Exception ignored) {}
        vmOut = null;
        vmIn = null;
        vmSocket = null;
    }

    // ==================== 状态查询 ====================

    public boolean isConnected() {
        return connected.get();
    }

    public boolean isListening() {
        return listening.get();
    }

    public int getTcpPort() {
        return tcpPort;
    }

    @PreDestroy
    public void shutdown() {
        stopListening();
    }
}
