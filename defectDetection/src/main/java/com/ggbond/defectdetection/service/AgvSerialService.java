package com.ggbond.defectdetection.service;

import com.fazecast.jSerialComm.SerialPort;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AGV 小车串口控制服务（XBOT-AGV RS485 协议，9600 8N1）。
 * 后端独占串口：打开 COM 口、周期查询状态、解析 0x25 状态帧并推送给前端。
 */
@Slf4j
@Service
public class AgvSerialService {

    /** 查询指令帧：00 01 04 A5 5A 06 26 FF + 校验 */
    private static final byte[] QUERY_FRAME = buildFrame(new byte[]{
            (byte) 0xA5, (byte) 0x5A, (byte) 0x06, (byte) 0x26, (byte) 0xFF});

    private volatile SerialPort port;
    private volatile String portName;
    private volatile Map<String, Object> latestStatus;
    private volatile long lastStatusTime;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Object writeLock = new Object();
    private final Object parseLock = new Object();
    private byte[] rxTail = new byte[0];

    /** 指令队列：所有写入操作排队，避免半双工冲突 */
    private final LinkedBlockingQueue<byte[]> commandQueue = new LinkedBlockingQueue<>();
    /** 指令发送线程 */
    private volatile Thread writerThread;
    /** 是否有指令正在发送 */
    private final AtomicBoolean sendingCommand = new AtomicBoolean(false);
    /** 最大重试次数 */
    private static final int MAX_RETRY = 3;
    /** 重试间隔(ms) */
    private static final long RETRY_DELAY_MS = 300;
    /** 指令发送后保护间隔(ms)，期间暂停轮询 */
    private static final long COMMAND_GUARD_MS = 1500;
    /** 写入前等待总线空闲(ms) */
    private static final long BUS_IDLE_WAIT_MS = 100;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "agv-serial-scheduler");
        t.setDaemon(true);
        return t;
    });
    private volatile ScheduledFuture<?> pollTask;

    public AgvSerialService() {
        scheduler.scheduleWithFixedDelay(this::heartbeat, 15, 15, TimeUnit.SECONDS);
    }

    /** 启动指令发送线程（连接时调用） */
    private void startWriterThread() {
        if (writerThread != null && writerThread.isAlive()) return;
        writerThread = new Thread(this::writerLoop, "agv-serial-writer");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    /** 指令发送循环：从队列取指令，带重试机制 */
    private void writerLoop() {
        while (isConnected()) {
            try {
                byte[] frame = commandQueue.poll(100, TimeUnit.MILLISECONDS);
                if (frame == null) continue;

                sendingCommand.set(true);
                // 暂停轮询，避免半双工冲突
                if (pollTask != null) {
                    pollTask.cancel(false);
                    pollTask = null;
                }

                boolean success = false;
                for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
                    boolean ok = writeRaw(frame);
                    if (ok) {
                        success = true;
                        if (attempt > 1) {
                            log.info("[AGV] 第{}次重试成功", attempt);
                        }
                        break;
                    }
                    log.warn("[AGV] 第{}次写入失败，{}ms后重试", attempt, RETRY_DELAY_MS);
                    Thread.sleep(RETRY_DELAY_MS);
                }

                if (!success) {
                    log.error("[AGV] 指令发送失败，已重试{}次: {}", MAX_RETRY, bytesToHex(frame));
                }

                // 保护间隔：让AGV有时间处理指令
                Thread.sleep(COMMAND_GUARD_MS);
                sendingCommand.set(false);

                // 恢复轮询
                if (pollTask == null && isConnected()) {
                    pollTask = scheduler.scheduleWithFixedDelay(this::sendQuery, 300, 1000, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /* ================= 串口管理 ================= */

    public List<Map<String, Object>> listPorts() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SerialPort p : SerialPort.getCommPorts()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", p.getSystemPortName());
            m.put("description", p.getDescriptivePortName());
            list.add(m);
        }
        return list;
    }

    public synchronized void connect(String name) {
        if (isConnected()) {
            if (Objects.equals(name, portName)) {
                return;
            }
            disconnect();
        }
        SerialPort p = SerialPort.getCommPort(name);
        p.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        p.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 200, 0);
        if (!p.openPort()) {
            throw new IllegalStateException("无法打开串口 " + name + "，请检查是否被其他程序占用");
        }
        port = p;
        portName = name;
        latestStatus = null;
        lastStatusTime = 0;
        synchronized (parseLock) {
            rxTail = new byte[0];
        }
        Thread reader = new Thread(this::readLoop, "agv-serial-reader");
        reader.setDaemon(true);
        reader.start();
        startWriterThread();
        if (pollTask != null) {
            pollTask.cancel(false);
        }
        pollTask = scheduler.scheduleWithFixedDelay(this::sendQuery, 300, 1000, TimeUnit.MILLISECONDS);
        log.info("AGV 串口已连接：{}", name);
        Map<String, Object> evt = new LinkedHashMap<>();
        evt.put("connected", true);
        evt.put("portName", name);
        broadcast(evt);
    }

    public synchronized void disconnect() {
        if (pollTask != null) {
            pollTask.cancel(false);
            pollTask = null;
        }
        SerialPort p = port;
        port = null;
        if (p != null && p.isOpen()) {
            p.closePort();
        }
        latestStatus = null;
        lastStatusTime = 0;
        portName = null;
        log.info("AGV 串口已断开");
        Map<String, Object> evt = new LinkedHashMap<>();
        evt.put("connected", false);
        broadcast(evt);
    }

    public boolean isConnected() {
        SerialPort p = port;
        return p != null && p.isOpen();
    }

    public String getPortName() {
        return portName;
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("connected", isConnected());
        m.put("portName", portName);
        m.put("lastStatusTime", lastStatusTime);
        m.put("status", latestStatus);
        return m;
    }

    /* ================= 指令发送 ================= */

    /** 控制指令帧：00 01 04 + A5 5A 08 23 <sub> <p1> <p2> + 校验（入队，异步发送） */
    public boolean sendCommand(int sub, int p1, int p2) {
        byte[] body = {(byte) 0xA5, (byte) 0x5A, (byte) 0x08, (byte) 0x23,
                (byte) sub, (byte) p1, (byte) p2};
        byte[] frame = buildFrame(body);
        log.info("[AGV] 指令入队: sub=0x{}, p1=0x{}, p2=0x{}, 帧={} ({} bytes)",
                Integer.toHexString(sub), Integer.toHexString(p1), Integer.toHexString(p2),
                bytesToHex(frame), frame.length);
        return commandQueue.offer(frame);
    }

    public boolean sendQuery() {
        // 如果正在发送指令,跳过本次轮询
        if (sendingCommand.get()) return false;
        // 轮询前也清空缓冲区
        SerialPort p = port;
        if (p != null && p.isOpen()) {
            p.flushIOBuffers();
        }
        return writeRaw(QUERY_FRAME);
    }

    /** 直接写入串口（无锁，由writerThread独占调用） */
    private boolean writeRaw(byte[] frame) {
        SerialPort p = port;
        if (p == null || !p.isOpen()) {
            return false;
        }
        // 写入前清空接收缓冲区，避免旧数据干扰
        p.flushIOBuffers();
        // 短暂等待总线空闲
        try { Thread.sleep(BUS_IDLE_WAIT_MS); } catch (InterruptedException ignored) {}
        int written = p.writeBytes(frame, frame.length);
        boolean ok = written == frame.length;
        log.info("[AGV] 串口写入: 期望{} bytes, 实际{} bytes, 成功={}", frame.length, written, ok);
        // 写入后等待数据真正发出（9600波特率下11字节约11ms）
        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        return ok;
    }

    /** 旧版同步写入（保留兼容） */
    private boolean write(byte[] frame) {
        SerialPort p = port;
        if (p == null || !p.isOpen()) {
            log.warn("[AGV] 串口未打开，无法写入");
            return false;
        }
        synchronized (writeLock) {
            return writeRaw(frame);
        }
    }

    private static byte[] buildFrame(byte[] body) {
        byte[] frame = new byte[3 + body.length + 1];
        frame[0] = 0x00;
        frame[1] = 0x01;
        frame[2] = 0x04;
        System.arraycopy(body, 0, frame, 3, body.length);
        int sum = 0;
        for (byte b : body) {
            sum += b & 0xFF;
        }
        frame[frame.length - 1] = (byte) (sum & 0xFF);
        return frame;
    }

    /* ================= 数据读取与解析 ================= */

    private void readLoop() {
        SerialPort p = port;
        byte[] buf = new byte[512];
        while (port == p && p.isOpen()) {
            int n = p.readBytes(buf, buf.length);
            if (n <= 0) {
                continue;
            }
            synchronized (parseLock) {
                byte[] merged = new byte[rxTail.length + n];
                System.arraycopy(rxTail, 0, merged, 0, rxTail.length);
                System.arraycopy(buf, 0, merged, rxTail.length, n);
                rxTail = parseFrames(merged);
            }
        }
    }

    private byte[] parseFrames(byte[] data) {
        int i = 0;
        while (i < data.length - 1) {
            if ((data[i] & 0xFF) != 0xA5 || (data[i + 1] & 0xFF) != 0x5A) {
                i++;
                continue;
            }
            if (i + 2 >= data.length) {
                break;
            }
            int len = data[i + 2] & 0xFF;
            if (len < 4 || len > 64) {
                i += 2;
                continue;
            }
            if (i + len > data.length) {
                break;
            }
            int sum = 0;
            for (int k = 0; k < len - 1; k++) {
                sum += data[i + k] & 0xFF;
            }
            if ((sum & 0xFF) == (data[i + len - 1] & 0xFF)
                    && (data[i + 3] & 0xFF) == 0x25 && len >= 29) {
                handleStatusFrame(data, i);
            }
            i += len;
        }
        return Arrays.copyOfRange(data, i, data.length);
    }

    private void handleStatusFrame(byte[] f, int o) {
        Map<String, Object> st = new LinkedHashMap<>();
        st.put("connected", true);
        st.put("portName", portName);
        st.put("battery", u(f, o + 5));
        st.put("setSpeed", (u(f, o + 7) << 8) | u(f, o + 6));
        st.put("stopFlag", u(f, o + 8));
        st.put("obstacleStop", u(f, o + 9));
        st.put("arriveStop", u(f, o + 10));
        st.put("cmdStop", u(f, o + 11));
        st.put("realSpeed", (u(f, o + 13) << 8) | u(f, o + 12));
        st.put("currentStation", u(f, o + 14));
        st.put("targetStation", u(f, o + 15));
        st.put("mode", u(f, o + 18));
        st.put("charging", u(f, o + 27));
        latestStatus = st;
        lastStatusTime = System.currentTimeMillis();
        broadcast(st);
    }

    private static int u(byte[] f, int i) {
        return f[i] & 0xFF;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }

    /* ================= SSE 推送 ================= */

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        log.info("[AGV] SSE 订阅者已添加, 当前订阅者数量: {}", emitters.size());
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("[AGV] SSE 订阅者完成, 剩余数量: {}", emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
            log.info("[AGV] SSE 订阅者超时, 剩余数量: {}", emitters.size());
        });
        emitter.onError(ex -> {
            emitters.remove(emitter);
            log.info("[AGV] SSE 订阅者错误: {}, 剩余数量: {}", ex.getMessage(), emitters.size());
        });
        try {
            Map<String, Object> hello = new LinkedHashMap<>();
            hello.put("connected", isConnected());
            hello.put("portName", portName);
            emitter.send(SseEmitter.event().name("status").data(hello));
            log.info("[AGV] SSE 初始消息已发送");
        } catch (Exception e) {
            log.warn("SSE 初始状态推送失败：{}", e.getMessage());
        }
        return emitter;
    }

    private void broadcast(Map<String, Object> payload) {
        log.info("[AGV] 广播状态数据, 订阅者数量: {}, 数据: {}", emitters.size(), payload);
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name("status").data(payload));
                log.info("[AGV] 状态数据已发送");
            } catch (Exception ex) {
                log.warn("[AGV] 发送状态数据失败: {}, 移除订阅者", ex.getMessage());
                emitters.remove(e);
                e.complete();
            }
        }
    }

    private void heartbeat() {
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().comment("hb"));
            } catch (Exception ex) {
                emitters.remove(e);
                e.complete();
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        disconnect();
        scheduler.shutdownNow();
        for (SseEmitter e : emitters) {
            e.complete();
        }
        emitters.clear();
    }
}
