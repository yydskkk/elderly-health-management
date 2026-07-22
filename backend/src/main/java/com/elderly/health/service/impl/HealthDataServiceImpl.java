package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.health.dto.HealthDataAddDTO;
import com.elderly.health.entity.FamilyElderlyRelation;
import com.elderly.health.entity.HealthData;
import com.elderly.health.entity.HealthDataType;
import com.elderly.health.entity.SysUser;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.FamilyElderlyRelationMapper;
import com.elderly.health.mapper.HealthDataMapper;
import com.elderly.health.mapper.HealthDataTypeMapper;
import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.service.AlertService;
import com.elderly.health.service.HealthDataService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.HealthDataStatisticsVO;
import com.elderly.health.vo.HealthDataTrendVO;
import com.elderly.health.vo.HealthDataTypeVO;
import com.elderly.health.vo.HealthDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 健康数据服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthDataServiceImpl implements HealthDataService {

    private final HealthDataMapper healthDataMapper;
    private final HealthDataTypeMapper healthDataTypeMapper;
    private final SysUserMapper sysUserMapper;
    private final FamilyElderlyRelationMapper familyElderlyRelationMapper;
    private final AlertService alertService;

    /**
     * 血压数据类型code
     */
    private static final String DATA_TYPE_BLOOD_PRESSURE = "BLOOD_PRESSURE";

    /**
     * 老年用户录入健康数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addByElderly(HealthDataAddDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        saveHealthData(currentUserId, currentUserId, dto);
    }

    /**
     * 家属代录入健康数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addByFamily(Long elderlyId, HealthDataAddDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 校验当前家属与该老人存在已确认关联
        Long count = familyElderlyRelationMapper.selectCount(
                new LambdaQueryWrapper<FamilyElderlyRelation>()
                        .eq(FamilyElderlyRelation::getFamilyId, currentUserId)
                        .eq(FamilyElderlyRelation::getElderlyId, elderlyId)
                        .eq(FamilyElderlyRelation::getStatus, 1));
        if (count == null || count == 0) {
            throw new BusinessException("未与该老人建立已确认的关联关系");
        }

        saveHealthData(elderlyId, currentUserId, dto);
    }

    /**
     * 保存健康数据并触发预警检测
     */
    private void saveHealthData(Long userId, Long recorderId, HealthDataAddDTO dto) {
        // 校验数据类型存在
        HealthDataType dataType = healthDataTypeMapper.selectOne(
                new LambdaQueryWrapper<HealthDataType>()
                        .eq(HealthDataType::getCode, dto.getDataType()));
        if (dataType == null) {
            throw new BusinessException("数据类型不存在：" + dto.getDataType());
        }

        // 血压类型需 valueHigh 和 valueLow
        if (DATA_TYPE_BLOOD_PRESSURE.equals(dto.getDataType())) {
            if (dto.getValueHigh() == null || dto.getValueLow() == null) {
                throw new BusinessException("血压类型需提供高压值和低压值");
            }
        } else {
            // 其他类型需 value
            if (dto.getValue() == null) {
                throw new BusinessException("该数据类型需提供数值");
            }
        }

        // 保存 health_data 记录
        HealthData healthData = new HealthData();
        healthData.setUserId(userId);
        healthData.setDataType(dto.getDataType());
        healthData.setValueHigh(dto.getValueHigh());
        healthData.setValueLow(dto.getValueLow());
        healthData.setValue(dto.getValue());
        healthData.setMeasureTime(dto.getMeasureTime());
        healthData.setRecorderId(recorderId);
        healthData.setIsAbnormal(0);
        healthDataMapper.insert(healthData);

        // 调用 AlertService.checkAlert 检测预警
        alertService.checkAlert(userId, dto.getDataType(), dto.getValueHigh(), dto.getValueLow(),
                dto.getValue(), healthData.getId());
    }

    /**
     * 查询健康数据列表
     */
    @Override
    public List<HealthDataVO> list(String dataType, LocalDateTime startTime, LocalDateTime endTime, Long elderlyId) {
        if (!StringUtils.hasText(dataType)) {
            throw new BusinessException("数据类型不能为空");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String roleCode = SecurityUtils.getCurrentUserRole();

        Long targetUserId = resolveTargetUserId(roleCode, currentUserId, elderlyId);

        LambdaQueryWrapper<HealthData> wrapper = new LambdaQueryWrapper<>();
        if (targetUserId != null) {
            wrapper.eq(HealthData::getUserId, targetUserId);
        }
        wrapper.eq(HealthData::getDataType, dataType);
        if (startTime != null) {
            wrapper.ge(HealthData::getMeasureTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(HealthData::getMeasureTime, endTime);
        }
        wrapper.orderByDesc(HealthData::getMeasureTime);

        List<HealthData> list = healthDataMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询录入人姓名
        Set<Long> recorderIds = list.stream()
                .map(HealthData::getRecorderId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> recorderNameMap = new java.util.HashMap<>();
        if (!recorderIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(recorderIds);
            for (SysUser u : users) {
                recorderNameMap.put(u.getId(), u.getName());
            }
        }

        List<HealthDataVO> result = new ArrayList<>();
        for (HealthData hd : list) {
            HealthDataVO vo = new HealthDataVO();
            BeanUtils.copyProperties(hd, vo);
            vo.setRecorderName(recorderNameMap.get(hd.getRecorderId()));
            result.add(vo);
        }
        return result;
    }

    /**
     * 查询健康数据趋势
     */
    @Override
    public List<HealthDataTrendVO> trend(String dataType, Integer days, Long elderlyId) {
        if (!StringUtils.hasText(dataType)) {
            throw new BusinessException("数据类型不能为空");
        }
        if (days == null || days <= 0) {
            days = 30;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String roleCode = SecurityUtils.getCurrentUserRole();
        Long targetUserId = resolveTargetUserId(roleCode, currentUserId, elderlyId);

        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<HealthData> wrapper = new LambdaQueryWrapper<>();
        if (targetUserId != null) {
            wrapper.eq(HealthData::getUserId, targetUserId);
        }
        wrapper.eq(HealthData::getDataType, dataType)
                .ge(HealthData::getMeasureTime, startTime)
                .orderByAsc(HealthData::getMeasureTime);

        List<HealthData> list = healthDataMapper.selectList(wrapper);

        List<HealthDataTrendVO> result = new ArrayList<>();
        for (HealthData hd : list) {
            HealthDataTrendVO vo = new HealthDataTrendVO();
            vo.setDate(hd.getMeasureTime().toLocalDate());
            vo.setValue(hd.getValue());
            vo.setValueHigh(hd.getValueHigh());
            vo.setValueLow(hd.getValueLow());
            vo.setIsAbnormal(hd.getIsAbnormal());
            result.add(vo);
        }
        return result;
    }

    /**
     * 健康数据统计摘要
     */
    @Override
    public HealthDataStatisticsVO statistics(String dataType, Long elderlyId, LocalDateTime startTime, LocalDateTime endTime) {
        if (!StringUtils.hasText(dataType)) {
            throw new BusinessException("数据类型不能为空");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String roleCode = SecurityUtils.getCurrentUserRole();
        Long targetUserId = resolveTargetUserId(roleCode, currentUserId, elderlyId);

        LambdaQueryWrapper<HealthData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthData::getUserId, targetUserId)
                .eq(HealthData::getDataType, dataType);
        if (startTime != null) {
            wrapper.ge(HealthData::getMeasureTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(HealthData::getMeasureTime, endTime);
        }

        List<HealthData> list = healthDataMapper.selectList(wrapper);

        HealthDataStatisticsVO vo = new HealthDataStatisticsVO();
        vo.setCount(list.size());

        int abnormalCount = 0;
        List<BigDecimal> values = new ArrayList<>();
        List<BigDecimal> highValues = new ArrayList<>();
        List<BigDecimal> lowValues = new ArrayList<>();

        for (HealthData hd : list) {
            if (hd.getIsAbnormal() != null && hd.getIsAbnormal() == 1) {
                abnormalCount++;
            }
            if (hd.getValue() != null) {
                values.add(hd.getValue());
            }
            if (hd.getValueHigh() != null) {
                highValues.add(hd.getValueHigh());
            }
            if (hd.getValueLow() != null) {
                lowValues.add(hd.getValueLow());
            }
        }
        vo.setAbnormalCount(abnormalCount);

        if (!values.isEmpty()) {
            vo.setAvgValue(average(values));
            vo.setMaxValue(values.stream().max(BigDecimal::compareTo).orElse(null));
            vo.setMinValue(values.stream().min(BigDecimal::compareTo).orElse(null));
        }
        if (!highValues.isEmpty()) {
            vo.setAvgValueHigh(average(highValues));
            vo.setMaxValueHigh(highValues.stream().max(BigDecimal::compareTo).orElse(null));
            vo.setMinValueHigh(highValues.stream().min(BigDecimal::compareTo).orElse(null));
        }
        if (!lowValues.isEmpty()) {
            vo.setAvgValueLow(average(lowValues));
            vo.setMaxValueLow(lowValues.stream().max(BigDecimal::compareTo).orElse(null));
            vo.setMinValueLow(lowValues.stream().min(BigDecimal::compareTo).orElse(null));
        }
        return vo;
    }

    /**
     * 查询所有健康数据类型
     */
    @Override
    public List<HealthDataTypeVO> types() {
        List<HealthDataType> list = healthDataTypeMapper.selectList(
                new LambdaQueryWrapper<HealthDataType>()
                        .orderByAsc(HealthDataType::getSort));
        List<HealthDataTypeVO> result = new ArrayList<>();
        for (HealthDataType t : list) {
            HealthDataTypeVO vo = new HealthDataTypeVO();
            BeanUtils.copyProperties(t, vo);
            result.add(vo);
        }
        return result;
    }

    /**
     * 根据当前用户角色解析目标用户ID
     */
    private Long resolveTargetUserId(String roleCode, Long currentUserId, Long elderlyId) {
        if (roleCode == null) {
            throw new BusinessException("无法获取当前用户角色");
        }
        switch (roleCode) {
            case "ELDERLY":
                return currentUserId;
            case "FAMILY":
                if (elderlyId == null) {
                    throw new BusinessException("请指定要查询的老人ID");
                }
                // 校验关联
                Long familyCount = familyElderlyRelationMapper.selectCount(
                        new LambdaQueryWrapper<FamilyElderlyRelation>()
                                .eq(FamilyElderlyRelation::getFamilyId, currentUserId)
                                .eq(FamilyElderlyRelation::getElderlyId, elderlyId)
                                .eq(FamilyElderlyRelation::getStatus, 1));
                if (familyCount == null || familyCount == 0) {
                    throw new BusinessException("未与该老人建立已确认的关联关系");
                }
                return elderlyId;
            case "DOCTOR":
            case "ADMIN":
                // 医护/管理员不传elderlyId时返回null，表示查询辖区所有数据
                return elderlyId;
            default:
                return currentUserId;
        }
    }

    /**
     * 计算平均值
     */
    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            sum = sum.add(v);
        }
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }
}
