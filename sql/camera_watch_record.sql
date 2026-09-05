-- ============================================================
-- 小米摄像头目录监听与抓拍记录专属表
-- ============================================================
USE defect_detection;

DROP TABLE IF EXISTS camera_watch_record;

CREATE TABLE camera_watch_record
(
    id               INTEGER PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    file_name        VARCHAR(255) NOT NULL COMMENT '原始图片文件名',
    stored_name      VARCHAR(255) NOT NULL COMMENT '服务器存储文件名',
    file_path        VARCHAR(500) NOT NULL COMMENT '服务器物理存储路径',
    web_url          VARCHAR(500) NOT NULL COMMENT 'Web访问静态URL',
    file_size        VARCHAR(50)           COMMENT '格式化文件大小(如 124.50 KB)',
    file_bytes       BIGINT                COMMENT '文件字节大小(Bytes)',
    upload_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传/捕获时间',
    server_watch_dir VARCHAR(500)          COMMENT '对应服务器端存储目录',
    status           VARCHAR(50)  DEFAULT '已上传' COMMENT '图片处理状态',
    is_deleted       TINYINT      DEFAULT 0 COMMENT '逻辑删除(0-正常, 1-已删除)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小米摄像头自动监听抓拍记录表';
