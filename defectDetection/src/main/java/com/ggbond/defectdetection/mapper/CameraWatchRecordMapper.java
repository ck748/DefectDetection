package com.ggbond.defectdetection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ggbond.defectdetection.pojo.CameraWatchRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小米摄像头抓拍监听记录 Mapper 接口
 */
@Mapper
public interface CameraWatchRecordMapper extends BaseMapper<CameraWatchRecord> {
}
