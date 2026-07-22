package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderly.health.dto.ElderlyProfileUpdateDTO;
import com.elderly.health.entity.ElderlyProfile;
import com.elderly.health.entity.FamilyElderlyRelation;
import com.elderly.health.entity.SysUser;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.ElderlyProfileMapper;
import com.elderly.health.mapper.FamilyElderlyRelationMapper;
import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.service.ElderlyProfileService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.ElderlyProfileManageVO;
import com.elderly.health.vo.ElderlyProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 健康档案服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElderlyProfileServiceImpl extends ServiceImpl<ElderlyProfileMapper, ElderlyProfile> implements ElderlyProfileService {

    private final ElderlyProfileMapper elderlyProfileMapper;
    private final SysUserMapper sysUserMapper;
    private final FamilyElderlyRelationMapper familyElderlyRelationMapper;

    /**
     * 老年用户查看自身档案
     */
    @Override
    public ElderlyProfileVO getMine() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 查询当前用户的健康档案
        ElderlyProfile profile = elderlyProfileMapper.selectOne(
                new LambdaQueryWrapper<ElderlyProfile>()
                        .eq(ElderlyProfile::getUserId, currentUserId));
        if (profile == null) {
            throw new BusinessException("健康档案不存在");
        }

        // 查询用户基本信息
        SysUser user = sysUserMapper.selectById(currentUserId);

        return buildProfileVO(profile, user);
    }

    /**
     * 家属查看关联老人档案
     * 校验当前家属与该老人存在已确认关联（status=1）
     */
    @Override
    public ElderlyProfileVO getByElderlyId(Long elderlyId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 校验当前家属与该老人存在已确认关联
        Long count = familyElderlyRelationMapper.selectCount(
                new LambdaQueryWrapper<FamilyElderlyRelation>()
                        .eq(FamilyElderlyRelation::getFamilyId, currentUserId)
                        .eq(FamilyElderlyRelation::getElderlyId, elderlyId)
                        .eq(FamilyElderlyRelation::getStatus, 1));
        if (count == null || count == 0) {
            throw new BusinessException("无权查看该老人档案，请先确认关联关系");
        }

        // 查询老人健康档案
        ElderlyProfile profile = elderlyProfileMapper.selectOne(
                new LambdaQueryWrapper<ElderlyProfile>()
                        .eq(ElderlyProfile::getUserId, elderlyId));
        if (profile == null) {
            throw new BusinessException("健康档案不存在");
        }

        // 查询老人用户基本信息
        SysUser user = sysUserMapper.selectById(elderlyId);

        return buildProfileVO(profile, user);
    }

    /**
     * 医护人员分页查询负责辖区内的老人档案列表
     */
    @Override
    public IPage<ElderlyProfileManageVO> pageProfiles(Integer pageNum, Integer pageSize, String keyword, Long regionId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Page<ElderlyProfileManageVO> page = new Page<>(pageNum, pageSize);
        return elderlyProfileMapper.selectProfilePage(page, currentUserId, keyword, regionId);
    }

    /**
     * 根据档案ID查询档案详情
     */
    @Override
    public ElderlyProfileVO getById(Long id) {
        ElderlyProfile profile = elderlyProfileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException("健康档案不存在");
        }

        // 查询老人用户基本信息
        SysUser user = sysUserMapper.selectById(profile.getUserId());

        return buildProfileVO(profile, user);
    }

    /**
     * 更新档案信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(ElderlyProfileUpdateDTO dto) {
        ElderlyProfile profile = elderlyProfileMapper.selectById(dto.getId());
        if (profile == null) {
            throw new BusinessException("健康档案不存在");
        }

        // 更新档案字段
        profile.setIdCard(dto.getIdCard());
        profile.setAddress(dto.getAddress());
        profile.setEmergencyContact(dto.getEmergencyContact());
        profile.setEmergencyPhone(dto.getEmergencyPhone());
        profile.setMedicalHistory(dto.getMedicalHistory());
        profile.setAllergyHistory(dto.getAllergyHistory());
        profile.setFamilyHistory(dto.getFamilyHistory());
        profile.setBloodType(dto.getBloodType());
        profile.setHeight(dto.getHeight());
        profile.setWeight(dto.getWeight());

        elderlyProfileMapper.updateById(profile);
    }

    /**
     * 构建健康档案VO（含用户基本信息）
     *
     * @param profile 档案实体
     * @param user    用户实体
     * @return 健康档案VO
     */
    private ElderlyProfileVO buildProfileVO(ElderlyProfile profile, SysUser user) {
        ElderlyProfileVO vo = new ElderlyProfileVO();
        BeanUtils.copyProperties(profile, vo);
        if (user != null) {
            vo.setName(user.getName());
            vo.setGender(user.getGender());
            vo.setAge(user.getAge());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
        }
        return vo;
    }
}
