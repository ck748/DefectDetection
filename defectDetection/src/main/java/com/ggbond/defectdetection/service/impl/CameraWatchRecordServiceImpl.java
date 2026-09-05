package com.ggbond.defectdetection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggbond.defectdetection.mapper.CameraWatchRecordMapper;
import com.ggbond.defectdetection.pojo.CameraWatchRecord;
import com.ggbond.defectdetection.service.CameraWatchRecordService;
import org.springframework.stereotype.Service;

/**
 * 小米摄像头抓拍监听记录服务实现类
 */
@Service
public class CameraWatchRecordServiceImpl extends ServiceImpl<CameraWatchRecordMapper, CameraWatchRecord> implements CameraWatchRecordService {
}
