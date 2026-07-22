package com.elderly.health.service;

import java.math.BigDecimal;

/**
 * 预警检测服务接口
 *
 * @author elderly-health
 */
public interface AlertService {

    /**
     * 检测预警
     * 查询个性化规则（alert_rule_personal），无则使用全局规则（alert_rule）
     * 比对数值是否超出阈值
     * 若超出：创建 alert_record、创建 alert_notification（发给老人、关联家属、医护）
     * 更新 health_data.is_abnormal=1
     *
     * @param userId    老人用户ID
     * @param dataType  数据类型code
     * @param valueHigh 高压值（血压专用）
     * @param valueLow  低压值（血压专用）
     * @param value     单值指标
     * @param dataId    健康数据记录ID
     */
    void checkAlert(Long userId, String dataType, BigDecimal valueHigh, BigDecimal valueLow, BigDecimal value, Long dataId);
}
