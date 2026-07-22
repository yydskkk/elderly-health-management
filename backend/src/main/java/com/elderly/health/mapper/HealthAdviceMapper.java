package com.elderly.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.health.entity.HealthAdvice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康建议 Mapper 接口
 *
 * @author elderly-health
 */
@Mapper
public interface HealthAdviceMapper extends BaseMapper<HealthAdvice> {
}
