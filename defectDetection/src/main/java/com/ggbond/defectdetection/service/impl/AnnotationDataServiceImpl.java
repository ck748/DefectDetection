package com.ggbond.defectdetection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggbond.defectdetection.mapper.AnnotationDataMapper;
import com.ggbond.defectdetection.pojo.AnnotationData;
import com.ggbond.defectdetection.service.AnnotationDataService;
import org.springframework.stereotype.Service;

/**
 * 标注数据服务实现类
 */
@Service
public class AnnotationDataServiceImpl extends ServiceImpl<AnnotationDataMapper, AnnotationData> implements AnnotationDataService {
}