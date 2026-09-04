package com.ggbond.defectdetection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ggbond.defectdetection.pojo.AnnotationData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标注数据Mapper接口
 */
@Mapper
public interface AnnotationDataMapper extends BaseMapper<AnnotationData> {
}