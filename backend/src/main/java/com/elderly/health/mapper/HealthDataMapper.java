package com.elderly.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.health.entity.HealthData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康数据记录 Mapper 接口
 *
 * @author elderly-health
 */
@Mapper
public interface HealthDataMapper extends BaseMapper<HealthData> {
}
