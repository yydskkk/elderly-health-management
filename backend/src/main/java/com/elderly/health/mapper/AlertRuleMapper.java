package com.elderly.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.health.entity.AlertRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 全局预警规则 Mapper 接口
 *
 * @author elderly-health
 */
@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {
}
