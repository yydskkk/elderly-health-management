package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.health.dto.AlertRuleAddDTO;
import com.elderly.health.dto.AlertRuleUpdateDTO;
import com.elderly.health.dto.PersonalAlertRuleAddDTO;
import com.elderly.health.entity.AlertRule;
import com.elderly.health.entity.AlertRulePersonal;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.AlertRuleMapper;
import com.elderly.health.mapper.AlertRulePersonalMapper;
import com.elderly.health.service.AlertRuleService;
import com.elderly.health.vo.AlertRuleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预警规则服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertRulePersonalMapper alertRulePersonalMapper;

    /**
     * 查询所有全局预警规则
     */
    @Override
    public List<AlertRuleVO> listAll() {
        List<AlertRule> list = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .orderByAsc(AlertRule::getDataType));
        List<AlertRuleVO> result = new ArrayList<>();
        for (AlertRule rule : list) {
            AlertRuleVO vo = convertToVO(rule);
            vo.setPersonal(false);
            result.add(vo);
        }
        return result;
    }

    /**
     * 创建全局预警规则
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(AlertRuleAddDTO dto) {
        AlertRule rule = new AlertRule();
        BeanUtils.copyProperties(dto, rule);
        if (rule.getStatus() == null) {
            rule.setStatus(1);
        }
        alertRuleMapper.insert(rule);
    }

    /**
     * 更新全局预警规则
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AlertRuleUpdateDTO dto) {
        AlertRule rule = alertRuleMapper.selectById(dto.getId());
        if (rule == null) {
            throw new BusinessException("预警规则不存在");
        }
        BeanUtils.copyProperties(dto, rule);
        alertRuleMapper.updateById(rule);
    }

    /**
     * 更新规则状态
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException("预警规则不存在");
        }
        rule.setStatus(status);
        alertRuleMapper.updateById(rule);
    }

    /**
     * 删除全局预警规则
     */
    @Override
    public void delete(Long id) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException("预警规则不存在");
        }
        alertRuleMapper.deleteById(id);
    }

    /**
     * 查询指定老人的个性化预警规则（如无个性化规则则返回全局规则）
     */
    @Override
    public List<AlertRuleVO> listPersonal(Long userId) {
        // 查询个性化规则
        List<AlertRulePersonal> personalList = alertRulePersonalMapper.selectList(
                new LambdaQueryWrapper<AlertRulePersonal>()
                        .eq(AlertRulePersonal::getUserId, userId)
                        .eq(AlertRulePersonal::getStatus, 1));
        Set<String> personalDataTypes = personalList.stream()
                .map(AlertRulePersonal::getDataType)
                .collect(Collectors.toSet());

        List<AlertRuleVO> result = new ArrayList<>();
        // 添加个性化规则
        for (AlertRulePersonal p : personalList) {
            AlertRuleVO vo = new AlertRuleVO();
            BeanUtils.copyProperties(p, vo);
            vo.setPersonal(true);
            vo.setUserId(p.getUserId());
            result.add(vo);
        }

        // 查询全局规则，过滤掉已有个性化规则的数据类型
        List<AlertRule> globalList = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .eq(AlertRule::getStatus, 1)
                        .orderByAsc(AlertRule::getDataType));
        for (AlertRule g : globalList) {
            if (!personalDataTypes.contains(g.getDataType())) {
                AlertRuleVO vo = convertToVO(g);
                vo.setPersonal(false);
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 创建或更新个性化预警规则（同一用户同一类型只允许一条）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePersonal(PersonalAlertRuleAddDTO dto) {
        // 查询是否已存在
        AlertRulePersonal existing = alertRulePersonalMapper.selectOne(
                new LambdaQueryWrapper<AlertRulePersonal>()
                        .eq(AlertRulePersonal::getUserId, dto.getUserId())
                        .eq(AlertRulePersonal::getDataType, dto.getDataType()));

        if (existing != null) {
            // 更新
            BeanUtils.copyProperties(dto, existing);
            existing.setStatus(1);
            alertRulePersonalMapper.updateById(existing);
        } else {
            // 新增
            AlertRulePersonal rule = new AlertRulePersonal();
            BeanUtils.copyProperties(dto, rule);
            rule.setStatus(1);
            alertRulePersonalMapper.insert(rule);
        }
    }

    /**
     * 删除个性化规则（恢复使用全局规则）
     */
    @Override
    public void deletePersonal(Long id) {
        AlertRulePersonal rule = alertRulePersonalMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException("个性化预警规则不存在");
        }
        alertRulePersonalMapper.deleteById(id);
    }

    /**
     * 转换为VO
     */
    private AlertRuleVO convertToVO(AlertRule rule) {
        AlertRuleVO vo = new AlertRuleVO();
        BeanUtils.copyProperties(rule, vo);
        return vo;
    }
}
