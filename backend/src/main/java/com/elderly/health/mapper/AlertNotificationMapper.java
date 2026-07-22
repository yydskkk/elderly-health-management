package com.elderly.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.health.entity.AlertNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预警通知 Mapper 接口
 *
 * @author elderly-health
 */
@Mapper
public interface AlertNotificationMapper extends BaseMapper<AlertNotification> {
}
