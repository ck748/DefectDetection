package com.ggbond.defectdetection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggbond.defectdetection.mapper.RawImageMapper;
import com.ggbond.defectdetection.pojo.RawImage;
import com.ggbond.defectdetection.service.RawImageService;
import org.springframework.stereotype.Service;

/**
 * 原始图片服务实现类
 */
@Service
public class RawImageServiceImpl extends ServiceImpl<RawImageMapper, RawImage> implements RawImageService {
}