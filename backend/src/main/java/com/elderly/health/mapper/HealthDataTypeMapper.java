package com.elderly.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.health.entity.HealthDataType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据类型字典 Mapper 接口
 *
 * @author elderly-health
 */
@Mapper
public interface HealthDataTypeMapper extends BaseMapper<HealthDataType> {
}
