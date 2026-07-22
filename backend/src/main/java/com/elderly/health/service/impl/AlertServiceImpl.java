package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.health.entity.*;
import com.elderly.health.mapper.*;
import com.elderly.health.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预警检测服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertRulePersonalMapper alertRulePersonalMapper;
    private final AlertRecordMapper alertRecordMapper;
    private final AlertNotificationMapper alertNotificationMapper;
    private final HealthDataMapper healthDataMapper;
    private final FamilyElderlyRelationMapper familyElderlyRelationMapper;
    private final SysUserRegionMapper sysUserRegionMapper;

    /**
     * 血压数据类型code
     */
    private static final String DATA_TYPE_BLOOD_PRESSURE = "BLOOD_PRESSURE";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAlert(Long userId, String dataType, BigDecimal valueHigh, BigDecimal valueLow, BigDecimal value, Long dataId) {
        // 1. 优先查询个性化规则，无则使用全局规则
        AlertRulePersonal personalRule = alertRulePersonalMapper.selectOne(
                new LambdaQueryWrapper<AlertRulePersonal>()
                        .eq(AlertRulePersonal::getUserId, userId)
                        .eq(AlertRulePersonal::getDataType, dataType)
                        .eq(AlertRulePersonal::getStatus, 1));

        boolean abnormal;
        Integer alertLevel;
        String valueDesc;

        if (personalRule != null) {
            // 使用个性化规则
            abnormal = isAbnormal(personalRule.getMinValue(), personalRule.getMaxValue(),
                    personalRule.getMinValueHigh(), personalRule.getMaxValueHigh(),
                    personalRule.getMinValueLow(), personalRule.getMaxValueLow(),
                    valueHigh, valueLow, value, dataType);
            alertLevel = personalRule.getAlertLevel();
        } else {
            // 使用全局规则
            AlertRule globalRule = alertRuleMapper.selectOne(
                    new LambdaQueryWrapper<AlertRule>()
                            .eq(AlertRule::getDataType, dataType)
                            .eq(AlertRule::getStatus, 1)
                            .last("LIMIT 1"));
            if (globalRule == null) {
                log.info("未找到数据类型 {} 的预警规则，跳过预警检测", dataType);
                return;
            }
            abnormal = isAbnormal(globalRule.getMinValue(), globalRule.getMaxValue(),
                    globalRule.getMinValueHigh(), globalRule.getMaxValueHigh(),
                    globalRule.getMinValueLow(), globalRule.getMaxValueLow(),
                    valueHigh, valueLow, value, dataType);
            alertLevel = globalRule.getAlertLevel();
        }

        if (!abnormal) {
            log.info("数据类型 {} 数值在正常范围内", dataType);
            return;
        }

        // 2. 构建异常值描述
        valueDesc = buildValueDesc(dataType, valueHigh, valueLow, value);

        // 3. 创建预警记录
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setUserId(userId);
        alertRecord.setDataType(dataType);
        alertRecord.setDataId(dataId);
        alertRecord.setValue(valueDesc);
        alertRecord.setAlertLevel(alertLevel);
        alertRecord.setStatus(0);
        alertRecordMapper.insert(alertRecord);

        // 4. 更新 health_data.is_abnormal=1
        HealthData healthData = healthDataMapper.selectById(dataId);
        if (healthData != null) {
            healthData.setIsAbnormal(1);
            healthDataMapper.updateById(healthData);
        }

        // 5. 创建预警通知（发给老人、关联家属、医护）
        List<Long> receiverIds = new ArrayList<>();
        receiverIds.add(userId);

        // 关联家属
        List<FamilyElderlyRelation> relations = familyElderlyRelationMapper.selectList(
                new LambdaQueryWrapper<FamilyElderlyRelation>()
                        .eq(FamilyElderlyRelation::getElderlyId, userId)
                        .eq(FamilyElderlyRelation::getStatus, 1));
        for (FamilyElderlyRelation relation : relations) {
            receiverIds.add(relation.getFamilyId());
        }

        // 辖区医护
        List<SysUserRegion> elderlyRegions = sysUserRegionMapper.selectList(
                new LambdaQueryWrapper<SysUserRegion>()
                        .eq(SysUserRegion::getUserId, userId)
                        .eq(SysUserRegion::getUserType, 1));
        if (!elderlyRegions.isEmpty()) {
            Set<Long> regionIds = elderlyRegions.stream()
                    .map(SysUserRegion::getRegionId)
                    .collect(Collectors.toSet());
            List<SysUserRegion> doctorRegions = sysUserRegionMapper.selectList(
                    new LambdaQueryWrapper<SysUserRegion>()
                            .in(SysUserRegion::getRegionId, regionIds)
                            .eq(SysUserRegion::getUserType, 2));
            for (SysUserRegion dr : doctorRegions) {
                receiverIds.add(dr.getUserId());
            }
        }

        // 去重后创建通知
        Set<Long> uniqueReceiverIds = new java.util.LinkedHashSet<>(receiverIds);
        LocalDateTime now = LocalDateTime.now();
        for (Long receiverId : uniqueReceiverIds) {
            AlertNotification notification = new AlertNotification();
            notification.setAlertRecordId(alertRecord.getId());
            notification.setReceiverId(receiverId);
            notification.setIsRead(0);
            alertNotificationMapper.insert(notification);
        }

        log.info("预警检测完成，用户ID：{}，数据类型：{}，已创建预警记录ID：{}，通知人数：{}",
                userId, dataType, alertRecord.getId(), uniqueReceiverIds.size());
    }

    /**
     * 判断数值是否异常
     */
    private boolean isAbnormal(BigDecimal minValue, BigDecimal maxValue,
                               BigDecimal minValueHigh, BigDecimal maxValueHigh,
                               BigDecimal minValueLow, BigDecimal maxValueLow,
                               BigDecimal valueHigh, BigDecimal valueLow, BigDecimal value,
                               String dataType) {
        if (DATA_TYPE_BLOOD_PRESSURE.equals(dataType)) {
            // 血压：检查高压和低压
            boolean highAbnormal = isOutOfRange(valueHigh, minValueHigh, maxValueHigh);
            boolean lowAbnormal = isOutOfRange(valueLow, minValueLow, maxValueLow);
            return highAbnormal || lowAbnormal;
        } else {
            // 其他类型：检查单值
            return isOutOfRange(value, minValue, maxValue);
        }
    }

    /**
     * 判断数值是否超出范围
     */
    private boolean isOutOfRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            return false;
        }
        if (min != null && value.compareTo(min) < 0) {
            return true;
        }
        if (max != null && value.compareTo(max) > 0) {
            return true;
        }
        return false;
    }

    /**
     * 构建异常值描述
     */
    private String buildValueDesc(String dataType, BigDecimal valueHigh, BigDecimal valueLow, BigDecimal value) {
        if (DATA_TYPE_BLOOD_PRESSURE.equals(dataType)) {
            return "高压:" + valueHigh + "/低压:" + valueLow;
        }
        return String.valueOf(value);
    }
}
