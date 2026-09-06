package com.ggbond.defectdetection.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务器系统状态监控数据 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemMonitorVO {

    private Cpu cpu;
    private Disk disk;
    private Memory memory;
    private Network network;
    private Boolean success;
    private SystemInfo system;
    private Long timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cpu {
        private Integer cores;
        private Integer threads;
        private Double usage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Disk {
        private Double free;
        private Double total;
        private Double usage;
        private Double used;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Memory {
        private Double available;
        private Double total;
        private Double usage;
        private Double used;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Network {
        private Double bytesRecv;
        private Double bytesSent;
        private Long packetsRecv;
        private Long packetsSent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemInfo {
        private String hostname;
        private String machine;
        private String pythonVersion;
        private String release;
        private String system;
        private Long uptime;
        private String version;
    }
}