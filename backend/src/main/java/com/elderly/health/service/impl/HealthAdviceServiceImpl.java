package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.health.dto.HealthAdviceAddDTO;
import com.elderly.health.entity.FamilyElderlyRelation;
import com.elderly.health.entity.HealthAdvice;
import com.elderly.health.entity.InterventionPlan;
import com.elderly.health.entity.SysUser;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.FamilyElderlyRelationMapper;
import com.elderly.health.mapper.HealthAdviceMapper;
import com.elderly.health.mapper.InterventionPlanMapper;
import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.service.HealthAdviceService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.HealthAdviceManageVO;
import com.elderly.health.vo.HealthAdviceVO;
import com.elderly.health.vo.InterventionPlanVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 健康建议服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAdviceServiceImpl implements HealthAdviceService {

    private final HealthAdviceMapper healthAdviceMapper;
    private final InterventionPlanMapper interventionPlanMapper;
    private final SysUserMapper sysUserMapper;
    private final FamilyElderlyRelationMapper familyElderlyRelationMapper;

    /**
     * 发布健康建议（医护人员）
     * 创建 health_advice 记录，若有干预方案则批量创建 intervention_plan 记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(HealthAdviceAddDTO dto) {
        Long doctorId = SecurityUtils.getCurrentUserId();

        // 校验老人用户存在
        SysUser elderlyUser = sysUserMapper.selectById(dto.getUserId());
        if (elderlyUser == null) {
            throw new BusinessException("老人用户不存在");
        }

        // 创建 health_advice 记录
        HealthAdvice advice = new HealthAdvice();
        BeanUtils.copyProperties(dto, advice);
        advice.setDoctorId(doctorId);
        advice.setStatus(1);
        healthAdviceMapper.insert(advice);

        // 若有干预方案，批量创建 intervention_plan 记录
        if (dto.getInterventionPlans() != null && !dto.getInterventionPlans().isEmpty()) {
            for (HealthAdviceAddDTO.InterventionPlanItem item : dto.getInterventionPlans()) {
                InterventionPlan plan = new InterventionPlan();
                plan.setAdviceId(advice.getId());
                plan.setUserId(dto.getUserId());
                plan.setContent(item.getContent());
                plan.setStartDate(item.getStartDate());
                plan.setEndDate(item.getEndDate());
                plan.setCycle(item.getCycle());
                plan.setStatus(1);
                interventionPlanMapper.insert(plan);
            }
        }

        log.info("医护人员{}发布健康建议，建议ID：{}，干预方案数：{}",
                doctorId, advice.getId(),
                dto.getInterventionPlans() == null ? 0 : dto.getInterventionPlans().size());
    }

    /**
     * 老年用户/家属查看健康建议列表
     * 老年用户：查询发给自己的建议
     * 家属：校验关联后查询该老人的建议
     */
    @Override
    public List<HealthAdviceVO> listMine(Long elderlyId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String roleCode = SecurityUtils.getCurrentUserRole();

        Long targetUserId = resolveTargetUserId(roleCode, currentUserId, elderlyId);

        // 查询发给该老人的建议
        List<HealthAdvice> advices = healthAdviceMapper.selectList(
                new LambdaQueryWrapper<HealthAdvice>()
                        .eq(HealthAdvice::getUserId, targetUserId)
                        .orderByDesc(HealthAdvice::getCreateTime));

        if (advices.isEmpty()) {
            return new ArrayList<>();
        }

        return convertToVOList(advices, true);
    }

    /**
     * 医护人员分页查询自己发布的健康建议
     */
    @Override
    public IPage<HealthAdviceManageVO> page(Integer pageNum, Integer pageSize, Long userId, String keyword) {
        Long doctorId = SecurityUtils.getCurrentUserId();

        LambdaQueryWrapper<HealthAdvice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthAdvice::getDoctorId, doctorId);
        if (userId != null) {
            wrapper.eq(HealthAdvice::getUserId, userId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(HealthAdvice::getTitle, keyword)
                    .or().like(HealthAdvice::getContent, keyword));
        }
        wrapper.orderByDesc(HealthAdvice::getCreateTime);

        Page<HealthAdvice> page = new Page<>(pageNum, pageSize);
        IPage<HealthAdvice> advicePage = healthAdviceMapper.selectPage(page, wrapper);

        Page<HealthAdviceManageVO> resultPage = new Page<>(advicePage.getCurrent(),
                advicePage.getSize(), advicePage.getTotal());
        List<HealthAdvice> records = advicePage.getRecords();
        if (records.isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }

        // 批量查询老人姓名
        Set<Long> userIds = records.stream()
                .map(HealthAdvice::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> elderlyNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            for (SysUser u : users) {
                elderlyNameMap.put(u.getId(), u.getName());
            }
        }

        // 批量查询医生姓名
        Set<Long> doctorIds = records.stream()
                .map(HealthAdvice::getDoctorId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> doctorNameMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            List<SysUser> doctors = sysUserMapper.selectBatchIds(doctorIds);
            for (SysUser u : doctors) {
                doctorNameMap.put(u.getId(), u.getName());
            }
        }

        List<HealthAdviceManageVO> voList = new ArrayList<>();
        for (HealthAdvice advice : records) {
            HealthAdviceManageVO vo = new HealthAdviceManageVO();
            BeanUtils.copyProperties(advice, vo);
            vo.setElderlyName(elderlyNameMap.get(advice.getUserId()));
            vo.setDoctorName(doctorNameMap.get(advice.getDoctorId()));
            voList.add(vo);
        }
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 查询健康建议详情（含干预方案列表）
     */
    @Override
    public HealthAdviceVO getById(Long id) {
        HealthAdvice advice = healthAdviceMapper.selectById(id);
        if (advice == null) {
            throw new BusinessException("健康建议不存在");
        }

        HealthAdviceVO vo = new HealthAdviceVO();
        BeanUtils.copyProperties(advice, vo);

        // 查询发布医生姓名
        if (advice.getDoctorId() != null) {
            SysUser doctor = sysUserMapper.selectById(advice.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getName());
            }
        }

        // 查询关联的干预方案列表
        List<InterventionPlan> plans = interventionPlanMapper.selectList(
                new LambdaQueryWrapper<InterventionPlan>()
                        .eq(InterventionPlan::getAdviceId, id)
                        .orderByDesc(InterventionPlan::getCreateTime));
        List<InterventionPlanVO> planVOs = new ArrayList<>();
        for (InterventionPlan plan : plans) {
            InterventionPlanVO planVO = new InterventionPlanVO();
            BeanUtils.copyProperties(plan, planVO);
            planVOs.add(planVO);
        }
        vo.setInterventionPlans(planVOs);

        return vo;
    }

    /**
     * 将建议列表转换为VO列表（含干预方案列表 + 发布医生姓名）
     *
     * @param advices          建议列表
     * @param withPlans        是否加载干预方案列表
     * @return 健康建议VO列表
     */
    private List<HealthAdviceVO> convertToVOList(List<HealthAdvice> advices, boolean withPlans) {
        if (advices.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询医生姓名
        Set<Long> doctorIds = advices.stream()
                .map(HealthAdvice::getDoctorId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> doctorNameMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            List<SysUser> doctors = sysUserMapper.selectBatchIds(doctorIds);
            for (SysUser u : doctors) {
                doctorNameMap.put(u.getId(), u.getName());
            }
        }

        // 批量查询干预方案
        Map<Long, List<InterventionPlan>> planMap = new HashMap<>();
        if (withPlans) {
            Set<Long> adviceIds = advices.stream()
                    .map(HealthAdvice::getId)
                    .collect(Collectors.toSet());
            if (!adviceIds.isEmpty()) {
                List<InterventionPlan> allPlans = interventionPlanMapper.selectList(
                        new LambdaQueryWrapper<InterventionPlan>()
                                .in(InterventionPlan::getAdviceId, adviceIds)
                                .orderByDesc(InterventionPlan::getCreateTime));
                for (InterventionPlan plan : allPlans) {
                    planMap.computeIfAbsent(plan.getAdviceId(), k -> new ArrayList<>()).add(plan);
                }
            }
        }

        List<HealthAdviceVO> result = new ArrayList<>();
        for (HealthAdvice advice : advices) {
            HealthAdviceVO vo = new HealthAdviceVO();
            BeanUtils.copyProperties(advice, vo);
            vo.setDoctorName(doctorNameMap.get(advice.getDoctorId()));

            if (withPlans) {
                List<InterventionPlan> plans = planMap.getOrDefault(advice.getId(), new ArrayList<>());
                List<InterventionPlanVO> planVOs = new ArrayList<>();
                for (InterventionPlan plan : plans) {
                    InterventionPlanVO planVO = new InterventionPlanVO();
                    BeanUtils.copyProperties(plan, planVO);
                    planVOs.add(planVO);
                }
                vo.setInterventionPlans(planVOs);
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 根据当前用户角色解析目标用户ID
     * 老年用户：返回自身ID
     * 家属：校验关联后返回老人ID
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
            default:
                return currentUserId;
        }
    }
}
