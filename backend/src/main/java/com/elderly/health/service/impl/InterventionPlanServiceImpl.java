package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.health.dto.InterventionFeedbackAddDTO;
import com.elderly.health.dto.InterventionPlanUpdateDTO;
import com.elderly.health.entity.FamilyElderlyRelation;
import com.elderly.health.entity.HealthAdvice;
import com.elderly.health.entity.InterventionFeedback;
import com.elderly.health.entity.InterventionPlan;
import com.elderly.health.entity.SysUser;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.FamilyElderlyRelationMapper;
import com.elderly.health.mapper.HealthAdviceMapper;
import com.elderly.health.mapper.InterventionFeedbackMapper;
import com.elderly.health.mapper.InterventionPlanMapper;
import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.service.InterventionPlanService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.InterventionFeedbackVO;
import com.elderly.health.vo.InterventionPlanVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 干预方案服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterventionPlanServiceImpl implements InterventionPlanService {

    /**
     * 干预方案状态：进行中
     */
    private static final int STATUS_IN_PROGRESS = 1;

    private final InterventionPlanMapper interventionPlanMapper;
    private final InterventionFeedbackMapper interventionFeedbackMapper;
    private final HealthAdviceMapper healthAdviceMapper;
    private final SysUserMapper sysUserMapper;
    private final FamilyElderlyRelationMapper familyElderlyRelationMapper;

    /**
     * 医护人员分页查询干预方案
     * 返回方案信息 + 老人姓名 + 建议标题 + 反馈列表
     */
    @Override
    public IPage<InterventionPlanVO> page(Integer pageNum, Integer pageSize, Long userId, Integer status) {
        LambdaQueryWrapper<InterventionPlan> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(InterventionPlan::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(InterventionPlan::getStatus, status);
        }
        wrapper.orderByDesc(InterventionPlan::getCreateTime);

        Page<InterventionPlan> page = new Page<>(pageNum, pageSize);
        IPage<InterventionPlan> planPage = interventionPlanMapper.selectPage(page, wrapper);

        Page<InterventionPlanVO> resultPage = new Page<>(planPage.getCurrent(),
                planPage.getSize(), planPage.getTotal());
        List<InterventionPlan> records = planPage.getRecords();
        if (records.isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }

        List<InterventionPlanVO> voList = buildPlanVOList(records, true, true);
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 更新干预方案内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(InterventionPlanUpdateDTO dto) {
        InterventionPlan plan = interventionPlanMapper.selectById(dto.getId());
        if (plan == null) {
            throw new BusinessException("干预方案不存在");
        }

        if (dto.getContent() != null) {
            plan.setContent(dto.getContent());
        }
        if (dto.getStartDate() != null) {
            plan.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            plan.setEndDate(dto.getEndDate());
        }
        if (dto.getCycle() != null) {
            plan.setCycle(dto.getCycle());
        }
        interventionPlanMapper.updateById(plan);
    }

    /**
     * 终止干预方案（status=0已终止）
     */
    @Override
    public void terminate(Long id) {
        InterventionPlan plan = interventionPlanMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException("干预方案不存在");
        }
        plan.setStatus(0);
        interventionPlanMapper.updateById(plan);
    }

    /**
     * 查询指定方案的所有执行反馈列表
     */
    @Override
    public List<InterventionFeedbackVO> listFeedbacks(Long planId) {
        InterventionPlan plan = interventionPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("干预方案不存在");
        }

        List<InterventionFeedback> feedbacks = interventionFeedbackMapper.selectList(
                new LambdaQueryWrapper<InterventionFeedback>()
                        .eq(InterventionFeedback::getPlanId, planId)
                        .orderByDesc(InterventionFeedback::getFeedbackTime));

        if (feedbacks.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询反馈人姓名
        Set<Long> userIds = feedbacks.stream()
                .map(InterventionFeedback::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            for (SysUser u : users) {
                userNameMap.put(u.getId(), u.getName());
            }
        }

        List<InterventionFeedbackVO> voList = new ArrayList<>();
        for (InterventionFeedback fb : feedbacks) {
            InterventionFeedbackVO vo = new InterventionFeedbackVO();
            BeanUtils.copyProperties(fb, vo);
            vo.setUserName(userNameMap.get(fb.getUserId()));
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 老年用户/家属查看进行中的干预方案
     * 老年用户：查询自己的进行中方案
     * 家属：校验关联后查询该老人的方案
     */
    @Override
    public List<InterventionPlanVO> listMine(Long elderlyId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String roleCode = SecurityUtils.getCurrentUserRole();

        Long targetUserId = resolveTargetUserId(roleCode, currentUserId, elderlyId);

        // 查询该老人进行中的方案
        List<InterventionPlan> plans = interventionPlanMapper.selectList(
                new LambdaQueryWrapper<InterventionPlan>()
                        .eq(InterventionPlan::getUserId, targetUserId)
                        .eq(InterventionPlan::getStatus, STATUS_IN_PROGRESS)
                        .orderByDesc(InterventionPlan::getCreateTime));

        if (plans.isEmpty()) {
            return new ArrayList<>();
        }

        return buildPlanVOList(plans, true, false);
    }

    /**
     * 新增干预方案执行反馈
     * 1. 获取当前用户ID
     * 2. 校验方案存在且进行中
     * 3. 创建 intervention_feedback 记录
     * 4. 通知相关医护（简化为记录日志）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFeedback(InterventionFeedbackAddDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 校验方案存在且进行中
        InterventionPlan plan = interventionPlanMapper.selectById(dto.getPlanId());
        if (plan == null) {
            throw new BusinessException("干预方案不存在");
        }
        if (plan.getStatus() == null || plan.getStatus() != STATUS_IN_PROGRESS) {
            throw new BusinessException("干预方案非进行中状态，无法提交反馈");
        }

        // 创建反馈记录
        InterventionFeedback feedback = new InterventionFeedback();
        BeanUtils.copyProperties(dto, feedback);
        feedback.setUserId(currentUserId);
        feedback.setFeedbackTime(LocalDateTime.now());
        interventionFeedbackMapper.insert(feedback);

        // 通知相关医护（简化为记录日志）
        Long doctorId = null;
        if (plan.getAdviceId() != null) {
            HealthAdvice advice = healthAdviceMapper.selectById(plan.getAdviceId());
            if (advice != null) {
                doctorId = advice.getDoctorId();
            }
        }
        log.info("干预方案反馈提交，方案ID：{}，反馈人：{}，关联医护：{}，是否执行：{}",
                plan.getId(), currentUserId, doctorId, dto.getIsExecuted());
    }

    /**
     * 构建干预方案VO列表（含老人姓名 + 建议标题 + 反馈列表）
     *
     * @param plans           方案列表
     * @param withUserName    是否加载老人姓名
     * @param withFeedbacks   是否加载反馈列表
     * @return 干预方案VO列表
     */
    private List<InterventionPlanVO> buildPlanVOList(List<InterventionPlan> plans,
                                                     boolean withUserName,
                                                     boolean withFeedbacks) {
        if (plans.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询老人姓名
        Map<Long, String> userNameMap = new HashMap<>();
        if (withUserName) {
            Set<Long> userIds = plans.stream()
                    .map(InterventionPlan::getUserId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!userIds.isEmpty()) {
                List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
                for (SysUser u : users) {
                    userNameMap.put(u.getId(), u.getName());
                }
            }
        }

        // 批量查询建议标题
        Map<Long, String> adviceTitleMap = new HashMap<>();
        Set<Long> adviceIds = plans.stream()
                .map(InterventionPlan::getAdviceId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (!adviceIds.isEmpty()) {
            List<HealthAdvice> advices = healthAdviceMapper.selectBatchIds(adviceIds);
            for (HealthAdvice a : advices) {
                adviceTitleMap.put(a.getId(), a.getTitle());
            }
        }

        // 批量查询反馈列表
        Map<Long, List<InterventionFeedback>> feedbackMap = new HashMap<>();
        if (withFeedbacks) {
            Set<Long> planIds = plans.stream()
                    .map(InterventionPlan::getId)
                    .collect(Collectors.toSet());
            if (!planIds.isEmpty()) {
                List<InterventionFeedback> allFeedbacks = interventionFeedbackMapper.selectList(
                        new LambdaQueryWrapper<InterventionFeedback>()
                                .in(InterventionFeedback::getPlanId, planIds)
                                .orderByDesc(InterventionFeedback::getFeedbackTime));
                for (InterventionFeedback fb : allFeedbacks) {
                    feedbackMap.computeIfAbsent(fb.getPlanId(), k -> new ArrayList<>()).add(fb);
                }
            }
        }

        List<InterventionPlanVO> result = new ArrayList<>();
        for (InterventionPlan plan : plans) {
            InterventionPlanVO vo = new InterventionPlanVO();
            BeanUtils.copyProperties(plan, vo);
            vo.setUserName(userNameMap.get(plan.getUserId()));
            vo.setAdviceTitle(adviceTitleMap.get(plan.getAdviceId()));

            if (withFeedbacks) {
                List<InterventionFeedback> feedbacks = feedbackMap.getOrDefault(plan.getId(), new ArrayList<>());
                List<InterventionFeedbackVO> feedbackVOs = new ArrayList<>();
                for (InterventionFeedback fb : feedbacks) {
                    InterventionFeedbackVO fbVO = new InterventionFeedbackVO();
                    BeanUtils.copyProperties(fb, fbVO);
                    feedbackVOs.add(fbVO);
                }
                vo.setFeedbacks(feedbackVOs);
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
