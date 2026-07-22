package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.health.dto.RelationAdminLinkDTO;
import com.elderly.health.dto.RelationApplyDTO;
import com.elderly.health.dto.RelationInviteDTO;
import com.elderly.health.entity.FamilyElderlyRelation;
import com.elderly.health.entity.SysUser;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.FamilyElderlyRelationMapper;
import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.service.RelationService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.RelationVO;
import com.elderly.health.vo.SimpleUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 家属老人关联服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RelationServiceImpl implements RelationService {

    private final FamilyElderlyRelationMapper relationMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 老人邀请家属
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invite(RelationInviteDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 根据邮箱查询家属
        SysUser familyUser = sysUserMapper.selectByEmail(dto.getFamilyEmail());
        if (familyUser == null) {
            throw new BusinessException("家属用户不存在：" + dto.getFamilyEmail());
        }

        // 创建关联记录（status=0待确认、invite_way=1老人邀请）
        FamilyElderlyRelation relation = new FamilyElderlyRelation();
        relation.setFamilyId(familyUser.getId());
        relation.setElderlyId(currentUserId);
        relation.setRelationType(dto.getRelationType());
        relation.setStatus(0);
        relation.setInviteWay(1);
        relation.setInitiatorId(currentUserId);
        relationMapper.insert(relation);
    }

    /**
     * 家属申请关联老人
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(RelationApplyDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 根据邮箱查询老人
        SysUser elderlyUser = sysUserMapper.selectByEmail(dto.getElderlyEmail());
        if (elderlyUser == null) {
            throw new BusinessException("老人用户不存在：" + dto.getElderlyEmail());
        }

        // 创建关联记录（status=0待确认、invite_way=2家属申请）
        FamilyElderlyRelation relation = new FamilyElderlyRelation();
        relation.setFamilyId(currentUserId);
        relation.setElderlyId(elderlyUser.getId());
        relation.setRelationType(dto.getRelationType());
        relation.setStatus(0);
        relation.setInviteWay(2);
        relation.setInitiatorId(currentUserId);
        relationMapper.insert(relation);
    }

    /**
     * 管理员/医护代关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminLink(RelationAdminLinkDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 校验家属用户存在
        SysUser familyUser = sysUserMapper.selectById(dto.getFamilyId());
        if (familyUser == null) {
            throw new BusinessException("家属用户不存在");
        }

        // 校验老人用户存在
        SysUser elderlyUser = sysUserMapper.selectById(dto.getElderlyId());
        if (elderlyUser == null) {
            throw new BusinessException("老人用户不存在");
        }

        // 直接创建已确认关联（status=1已关联、invite_way=3代关联）
        FamilyElderlyRelation relation = new FamilyElderlyRelation();
        relation.setFamilyId(dto.getFamilyId());
        relation.setElderlyId(dto.getElderlyId());
        relation.setRelationType(dto.getRelationType());
        relation.setStatus(1);
        relation.setInviteWay(3);
        relation.setInitiatorId(currentUserId);
        relationMapper.insert(relation);
    }

    /**
     * 确认关联
     */
    @Override
    public void confirm(Long id) {
        FamilyElderlyRelation relation = relationMapper.selectById(id);
        if (relation == null) {
            throw new BusinessException("关联记录不存在");
        }
        relation.setStatus(1);
        relationMapper.updateById(relation);
    }

    /**
     * 拒绝关联
     */
    @Override
    public void reject(Long id) {
        FamilyElderlyRelation relation = relationMapper.selectById(id);
        if (relation == null) {
            throw new BusinessException("关联记录不存在");
        }
        relation.setStatus(2);
        relationMapper.updateById(relation);
    }

    /**
     * 查询关联关系列表（根据当前用户角色返回相关记录）
     */
    @Override
    public List<RelationVO> list(Integer status) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String roleCode = SecurityUtils.getCurrentUserRole();

        LambdaQueryWrapper<FamilyElderlyRelation> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(FamilyElderlyRelation::getStatus, status);
        }
        wrapper.orderByDesc(FamilyElderlyRelation::getCreateTime);

        List<FamilyElderlyRelation> relations;

        if (StringUtils.hasText(roleCode)) {
            switch (roleCode) {
                case "ELDERLY":
                    // 老年用户：返回邀请家属的记录
                    wrapper.eq(FamilyElderlyRelation::getElderlyId, currentUserId);
                    break;
                case "FAMILY":
                    // 家属：返回申请关联老人的记录
                    wrapper.eq(FamilyElderlyRelation::getFamilyId, currentUserId);
                    break;
                case "ADMIN":
                case "DOCTOR":
                    // 管理员/医护：返回所有关联记录
                    break;
                default:
                    // 其他角色默认查自己相关的
                    wrapper.and(w -> w.eq(FamilyElderlyRelation::getElderlyId, currentUserId)
                            .or().eq(FamilyElderlyRelation::getFamilyId, currentUserId));
                    break;
            }
        } else {
            wrapper.and(w -> w.eq(FamilyElderlyRelation::getElderlyId, currentUserId)
                    .or().eq(FamilyElderlyRelation::getFamilyId, currentUserId));
        }

        relations = relationMapper.selectList(wrapper);
        return convertToVOList(relations);
    }

    /**
     * 家属查询关联老人列表
     */
    @Override
    public List<SimpleUserVO> myElderly() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 查询已关联的老人ID列表
        List<FamilyElderlyRelation> relations = relationMapper.selectList(
                new LambdaQueryWrapper<FamilyElderlyRelation>()
                        .eq(FamilyElderlyRelation::getFamilyId, currentUserId)
                        .eq(FamilyElderlyRelation::getStatus, 1));

        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> elderlyIds = relations.stream()
                .map(FamilyElderlyRelation::getElderlyId)
                .collect(Collectors.toSet());

        List<SysUser> elderlyUsers = sysUserMapper.selectBatchIds(elderlyIds);
        return convertToSimpleVOList(elderlyUsers);
    }

    /**
     * 老人查询关联家属列表
     */
    @Override
    public List<SimpleUserVO> myFamily() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 查询已关联的家属ID列表
        List<FamilyElderlyRelation> relations = relationMapper.selectList(
                new LambdaQueryWrapper<FamilyElderlyRelation>()
                        .eq(FamilyElderlyRelation::getElderlyId, currentUserId)
                        .eq(FamilyElderlyRelation::getStatus, 1));

        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> familyIds = relations.stream()
                .map(FamilyElderlyRelation::getFamilyId)
                .collect(Collectors.toSet());

        List<SysUser> familyUsers = sysUserMapper.selectBatchIds(familyIds);
        return convertToSimpleVOList(familyUsers);
    }

    /**
     * 将关联记录列表转换为VO列表（附带家属和老人信息）
     */
    private List<RelationVO> convertToVOList(List<FamilyElderlyRelation> relations) {
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有用户ID
        Set<Long> userIds = new java.util.HashSet<>();
        for (FamilyElderlyRelation r : relations) {
            if (r.getFamilyId() != null) {
                userIds.add(r.getFamilyId());
            }
            if (r.getElderlyId() != null) {
                userIds.add(r.getElderlyId());
            }
        }

        // 批量查询用户信息
        Map<Long, SysUser> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            for (SysUser u : users) {
                userMap.put(u.getId(), u);
            }
        }

        List<RelationVO> result = new ArrayList<>();
        for (FamilyElderlyRelation r : relations) {
            RelationVO vo = new RelationVO();
            BeanUtils.copyProperties(r, vo);

            SysUser family = userMap.get(r.getFamilyId());
            if (family != null) {
                vo.setFamilyName(family.getName());
                vo.setFamilyEmail(family.getEmail());
            }

            SysUser elderly = userMap.get(r.getElderlyId());
            if (elderly != null) {
                vo.setElderlyName(elderly.getName());
                vo.setElderlyEmail(elderly.getEmail());
            }

            result.add(vo);
        }
        return result;
    }

    /**
     * 将用户列表转换为简单VO列表
     */
    private List<SimpleUserVO> convertToSimpleVOList(List<SysUser> users) {
        List<SimpleUserVO> result = new ArrayList<>();
        for (SysUser u : users) {
            SimpleUserVO vo = new SimpleUserVO();
            vo.setId(u.getId());
            vo.setName(u.getName());
            vo.setEmail(u.getEmail());
            vo.setPhone(u.getPhone());
            vo.setGender(u.getGender());
            vo.setAge(u.getAge());
            result.add(vo);
        }
        return result;
    }
}
