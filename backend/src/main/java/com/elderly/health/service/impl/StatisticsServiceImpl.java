package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.health.entity.AlertRecord;
import com.elderly.health.entity.HealthData;
import com.elderly.health.entity.HealthDataType;
import com.elderly.health.entity.SysRole;
import com.elderly.health.entity.SysUser;
import com.elderly.health.entity.SysUserRole;
import com.elderly.health.mapper.AlertRecordMapper;
import com.elderly.health.mapper.HealthDataMapper;
import com.elderly.health.mapper.HealthDataTypeMapper;
import com.elderly.health.mapper.SysRoleMapper;
import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.mapper.SysUserRoleMapper;
import com.elderly.health.service.StatisticsService;
import com.elderly.health.vo.DistributionVO;
import com.elderly.health.vo.StatisticsChartVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 数据统计服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final HealthDataMapper healthDataMapper;
    private final AlertRecordMapper alertRecordMapper;
    private final HealthDataTypeMapper healthDataTypeMapper;

    /**
     * 统计类型：按日
     */
    private static final String TYPE_DAY = "day";
    /**
     * 统计类型：按月
     */
    private static final String TYPE_MONTH = "month";

    /**
     * 总览数据统计
     */
    @Override
    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 总用户数
        Long totalUsers = sysUserMapper.selectCount(null);
        result.put("totalUsers", totalUsers);

        // 按角色统计用户数
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(null);
        List<SysRole> roles = sysRoleMapper.selectList(null);
        Map<Long, String> roleIdCodeMap = roles.stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleCode, (a, b) -> a));

        long totalElderly = 0;
        long totalFamily = 0;
        long totalDoctor = 0;
        for (SysUserRole userRole : userRoles) {
            String roleCode = roleIdCodeMap.get(userRole.getRoleId());
            if (roleCode == null) {
                continue;
            }
            switch (roleCode) {
                case "ELDERLY":
                    totalElderly++;
                    break;
                case "FAMILY":
                    totalFamily++;
                    break;
                case "DOCTOR":
                    totalDoctor++;
                    break;
                default:
                    break;
            }
        }
        result.put("totalElderly", totalElderly);
        result.put("totalFamily", totalFamily);
        result.put("totalDoctor", totalDoctor);

        // 健康数据总数
        Long totalHealthData = healthDataMapper.selectCount(null);
        result.put("totalHealthData", totalHealthData);

        // 预警总数
        Long totalAlerts = alertRecordMapper.selectCount(null);
        result.put("totalAlerts", totalAlerts);

        // 待处理预警数
        Long pendingAlerts = alertRecordMapper.selectCount(
                new LambdaQueryWrapper<AlertRecord>().eq(AlertRecord::getStatus, 0));
        result.put("pendingAlerts", pendingAlerts);

        return result;
    }

    /**
     * 用户注册统计（按角色分组）
     */
    @Override
    public List<StatisticsChartVO> userRegister(LocalDate startDate, LocalDate endDate, String type) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();

        // 查询时间范围内的用户
        List<SysUser> users = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .ge(SysUser::getCreateTime, startTime)
                        .lt(SysUser::getCreateTime, endTime));

        if (users.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询用户角色
        Set<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toSet());
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
        List<SysRole> roles = sysRoleMapper.selectList(null);
        Map<Long, String> roleIdCodeMap = roles.stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleCode, (a, b) -> a));

        // 构建 userId -> roleCode 映射
        Map<Long, String> userRoleMap = new HashMap<>();
        for (SysUserRole userRole : userRoles) {
            String roleCode = roleIdCodeMap.get(userRole.getRoleId());
            if (roleCode != null) {
                userRoleMap.put(userRole.getUserId(), roleCode);
            }
        }

        // 按日期 + 角色分组统计
        DateTimeFormatter formatter = TYPE_MONTH.equalsIgnoreCase(type)
                ? DateTimeFormatter.ofPattern("yyyy-MM")
                : DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 使用 TreeMap 保证日期排序
        Map<String, Map<String, Long>> grouped = new TreeMap<>();
        for (SysUser user : users) {
            String dateKey = user.getCreateTime().format(formatter);
            String roleCode = userRoleMap.getOrDefault(user.getId(), "UNKNOWN");
            grouped.computeIfAbsent(dateKey, k -> new HashMap<>())
                    .merge(roleCode, 1L, Long::sum);
        }

        List<StatisticsChartVO> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> dateEntry : grouped.entrySet()) {
            for (Map.Entry<String, Long> roleEntry : dateEntry.getValue().entrySet()) {
                StatisticsChartVO vo = new StatisticsChartVO();
                vo.setDate(dateEntry.getKey());
                vo.setValue(roleEntry.getValue());
                vo.setCategory(roleEntry.getKey());
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 健康数据录入统计
     */
    @Override
    public List<StatisticsChartVO> healthData(LocalDate startDate, LocalDate endDate, String type, String dataType) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<HealthData> wrapper = new LambdaQueryWrapper<HealthData>()
                .ge(HealthData::getCreateTime, startTime)
                .lt(HealthData::getCreateTime, endTime);
        if (StringUtils.hasText(dataType)) {
            wrapper.eq(HealthData::getDataType, dataType);
        }

        List<HealthData> list = healthDataMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        DateTimeFormatter formatter = TYPE_MONTH.equalsIgnoreCase(type)
                ? DateTimeFormatter.ofPattern("yyyy-MM")
                : DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 按日期 + 数据类型分组统计
        Map<String, Map<String, Long>> grouped = new TreeMap<>();
        for (HealthData hd : list) {
            String dateKey = hd.getCreateTime().format(formatter);
            String category = hd.getDataType() != null ? hd.getDataType() : "UNKNOWN";
            grouped.computeIfAbsent(dateKey, k -> new HashMap<>())
                    .merge(category, 1L, Long::sum);
        }

        List<StatisticsChartVO> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> dateEntry : grouped.entrySet()) {
            for (Map.Entry<String, Long> catEntry : dateEntry.getValue().entrySet()) {
                StatisticsChartVO vo = new StatisticsChartVO();
                vo.setDate(dateEntry.getKey());
                vo.setValue(catEntry.getValue());
                vo.setCategory(catEntry.getKey());
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 预警发生统计
     */
    @Override
    public List<StatisticsChartVO> alert(LocalDate startDate, LocalDate endDate, String type, Integer alertLevel) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<AlertRecord>()
                .ge(AlertRecord::getCreateTime, startTime)
                .lt(AlertRecord::getCreateTime, endTime);
        if (alertLevel != null) {
            wrapper.eq(AlertRecord::getAlertLevel, alertLevel);
        }

        List<AlertRecord> list = alertRecordMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        DateTimeFormatter formatter = TYPE_MONTH.equalsIgnoreCase(type)
                ? DateTimeFormatter.ofPattern("yyyy-MM")
                : DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 按日期 + 预警等级分组统计
        Map<String, Map<String, Long>> grouped = new TreeMap<>();
        for (AlertRecord ar : list) {
            String dateKey = ar.getCreateTime().format(formatter);
            String category = alertLevelToString(ar.getAlertLevel());
            grouped.computeIfAbsent(dateKey, k -> new HashMap<>())
                    .merge(category, 1L, Long::sum);
        }

        List<StatisticsChartVO> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> dateEntry : grouped.entrySet()) {
            for (Map.Entry<String, Long> levelEntry : dateEntry.getValue().entrySet()) {
                StatisticsChartVO vo = new StatisticsChartVO();
                vo.setDate(dateEntry.getKey());
                vo.setValue(levelEntry.getValue());
                vo.setCategory(levelEntry.getKey());
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 健康数据类型分布占比
     */
    @Override
    public List<DistributionVO> healthDataTypeDistribution(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();

        List<HealthData> list = healthDataMapper.selectList(
                new LambdaQueryWrapper<HealthData>()
                        .ge(HealthData::getCreateTime, startTime)
                        .lt(HealthData::getCreateTime, endTime));

        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        // 按数据类型分组统计
        Map<String, Long> typeCountMap = new LinkedHashMap<>();
        for (HealthData hd : list) {
            String dt = hd.getDataType() != null ? hd.getDataType() : "UNKNOWN";
            typeCountMap.merge(dt, 1L, Long::sum);
        }

        // 查询数据类型名称
        List<HealthDataType> dataTypes = healthDataTypeMapper.selectList(null);
        Map<String, String> codeNameMap = dataTypes.stream()
                .collect(Collectors.toMap(HealthDataType::getCode, HealthDataType::getName, (a, b) -> a));

        long total = list.size();
        List<DistributionVO> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : typeCountMap.entrySet()) {
            DistributionVO vo = new DistributionVO();
            String code = entry.getKey();
            vo.setName(codeNameMap.getOrDefault(code, code));
            vo.setValue(entry.getValue());
            vo.setPercentage(BigDecimal.valueOf(entry.getValue())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
            result.add(vo);
        }

        // 按数量降序排序
        result.sort(Comparator.comparing(DistributionVO::getValue).reversed());
        return result;
    }

    /**
     * 预警等级转中文描述
     *
     * @param alertLevel 预警等级：1低2中3高
     * @return 中文描述
     */
    private String alertLevelToString(Integer alertLevel) {
        if (alertLevel == null) {
            return "未知";
        }
        switch (alertLevel) {
            case 1:
                return "低";
            case 2:
                return "中";
            case 3:
                return "高";
            default:
                return "未知";
        }
    }
}
