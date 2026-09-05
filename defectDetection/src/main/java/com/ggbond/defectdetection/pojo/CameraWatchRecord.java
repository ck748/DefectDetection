package com.ggbond.defectdetection.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 小米摄像头目录监听与抓拍记录实体类
 * 对应数据库表：camera_watch_record
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("camera_watch_record")
public class CameraWatchRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;              // 自增主键ID

    private String fileName;         // 原始抓拍图片文件名

    private String storedName;       // 服务器内部保存唯一文件名

    private String filePath;         // 服务器物理存储路径

    private String webUrl;           // Web静态访问相对路径（如 /uploads/camera_watch/xxx.jpg）

    private String fileSize;         // 格式化大小（如 124.50 KB）

    private Long fileBytes;          // 文件字节大小

    private LocalDateTime uploadTime;// 上传/捕获时间

    private String serverWatchDir;   // 关联归属的服务器端监听存储目录

    private String status;           // 状态说明（默认：已上传/已同步）

    @TableLogic
    private Integer isDeleted;       // 逻辑删除标记：0-正常，1-已删除
}
