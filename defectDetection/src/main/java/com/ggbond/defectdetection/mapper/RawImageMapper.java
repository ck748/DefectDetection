package com.ggbond.defectdetection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ggbond.defectdetection.pojo.RawImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 原始图片Mapper接口
 */
@Mapper
public interface RawImageMapper extends BaseMapper<RawImage> {
}