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
 * 原始图片实体类
 * 对应数据库表：raw_image
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("raw_image")
public class RawImage {
    @TableId(type = IdType.AUTO)
    private Integer id;              // 主键ID
    private String imageName;        // 图片名称
    private String imagePath;        // 图片存储路径
    private Long imageSize;          // 图片大小(字节)
    private Integer imageWidth;      // 图片宽度
    private Integer imageHeight;     // 图片高度
    private String uploadSource;     // 上传来源：camera-摄像头, manual-手动上传
    private Integer deviceId;        // 设备ID
    private Integer workOrderId;     // 工单ID
    private LocalDateTime uploadTime; // 上传时间
    private Integer uploadUserId;    // 上传用户ID
    private String uploadUserName;   // 上传用户名称
    private Integer status;          // 状态：0-待标注, 1-标注中, 2-已标注, 3-已检测
    
    @TableLogic  // 逻辑删除标记
    private Integer isDeleted;       // 逻辑删除：0-未删除, 1-已删除
    
    private String remark;           // 备注
}