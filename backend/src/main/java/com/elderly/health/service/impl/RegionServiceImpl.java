package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.health.dto.AssignRegionDTO;
import com.elderly.health.dto.RegionAddDTO;
import com.elderly.health.dto.RegionUpdateDTO;
import com.elderly.health.entity.SysRegion;
import com.elderly.health.entity.SysUser;
import com.elderly.health.entity.SysUserRegion;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.SysRegionMapper;
import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.mapper.SysUserRegionMapper;
import com.elderly.health.service.RegionService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.RegionVO;
import com.elderly.health.vo.SimpleUserVO;
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
 * 辖区服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final SysRegionMapper sysRegionMapper;
    private final SysUserRegionMapper sysUserRegionMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 用户类型：老人
     */
    private static final int USER_TYPE_ELDERLY = 1;
    /**
     * 用户类型：医护
     */
    private static final int USER_TYPE_DOCTOR = 2;

    /**
     * 查询所有辖区列表（含老人数、医护数）
     */
    @Override
    public List<RegionVO> list() {
        List<SysRegion> regions = sysRegionMapper.selectList(
                new LambdaQueryWrapper<SysRegion>().orderByAsc(SysRegion::getId));

        if (regions.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询所有辖区关联记录
        List<SysUserRegion> allRelations = sysUserRegionMapper.selectList(null);

        // 按辖区ID分组统计老人数和医护数
        Map<Long, Integer> elderlyCountMap = new HashMap<>();
        Map<Long, Integer> doctorCountMap = new HashMap<>();
        for (SysUserRegion relation : allRelations) {
            Long regionId = relation.getRegionId();
            if (relation.getUserType() != null && relation.getUserType() == USER_TYPE_ELDERLY) {
                elderlyCountMap.merge(regionId, 1, Integer::sum);
            } else if (relation.getUserType() != null && relation.getUserType() == USER_TYPE_DOCTOR) {
                doctorCountMap.merge(regionId, 1, Integer::sum);
            }
        }

        List<RegionVO> result = new ArrayList<>();
        for (SysRegion region : regions) {
            RegionVO vo = new RegionVO();
            BeanUtils.copyProperties(region, vo);
            vo.setElderlyCount(elderlyCountMap.getOrDefault(region.getId(), 0));
            vo.setDoctorCount(doctorCountMap.getOrDefault(region.getId(), 0));
            result.add(vo);
        }
        return result;
    }

    /**
     * 新增辖区
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RegionAddDTO dto) {
        // 校验辖区名称唯一
        Long count = sysRegionMapper.selectCount(
                new LambdaQueryWrapper<SysRegion>().eq(SysRegion::getRegionName, dto.getRegionName()));
        if (count > 0) {
            throw new BusinessException("辖区名称已存在：" + dto.getRegionName());
        }

        SysRegion region = new SysRegion();
        region.setRegionName(dto.getRegionName());
        region.setDescription(dto.getDescription());
        region.setStatus(1);
        sysRegionMapper.insert(region);
    }

    /**
     * 编辑辖区
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RegionUpdateDTO dto) {
        SysRegion region = sysRegionMapper.selectById(dto.getId());
        if (region == null) {
            throw new BusinessException("辖区不存在");
        }

        // 校验辖区名称唯一（排除自身）
        if (StringUtils.hasText(dto.getRegionName()) && !dto.getRegionName().equals(region.getRegionName())) {
            Long count = sysRegionMapper.selectCount(
                    new LambdaQueryWrapper<SysRegion>()
                            .eq(SysRegion::getRegionName, dto.getRegionName())
                            .ne(SysRegion::getId, dto.getId()));
            if (count > 0) {
                throw new BusinessException("辖区名称已存在：" + dto.getRegionName());
            }
            region.setRegionName(dto.getRegionName());
        }
        if (dto.getDescription() != null) {
            region.setDescription(dto.getDescription());
        }
        sysRegionMapper.updateById(region);
    }

    /**
     * 启用/禁用辖区
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        SysRegion region = sysRegionMapper.selectById(id);
        if (region == null) {
            throw new BusinessException("辖区不存在");
        }
        region.setStatus(status);
        sysRegionMapper.updateById(region);
    }

    /**
     * 删除辖区（校验辖区下无用户才可删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysRegion region = sysRegionMapper.selectById(id);
        if (region == null) {
            throw new BusinessException("辖区不存在");
        }

        // 校验辖区下无用户
        Long userCount = sysUserRegionMapper.selectCount(
                new LambdaQueryWrapper<SysUserRegion>().eq(SysUserRegion::getRegionId, id));
        if (userCount > 0) {
            throw new BusinessException("辖区下存在关联用户，无法删除");
        }

        sysRegionMapper.deleteById(id);
    }

    /**
     * 分配用户至辖区
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(AssignRegionDTO dto) {
        if (dto.getUserType() == null || (dto.getUserType() != USER_TYPE_ELDERLY && dto.getUserType() != USER_TYPE_DOCTOR)) {
            throw new BusinessException("用户类型无效，1老人2医护");
        }

        // 校验辖区存在
        SysRegion region = sysRegionMapper.selectById(dto.getRegionId());
        if (region == null) {
            throw new BusinessException("辖区不存在");
        }

        // 校验用户存在
        SysUser user = sysUserMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验是否已分配
        Long count = sysUserRegionMapper.selectCount(
                new LambdaQueryWrapper<SysUserRegion>()
                        .eq(SysUserRegion::getUserId, dto.getUserId())
                        .eq(SysUserRegion::getRegionId, dto.getRegionId())
                        .eq(SysUserRegion::getUserType, dto.getUserType()));
        if (count > 0) {
            throw new BusinessException("该用户已分配至此辖区");
        }

        SysUserRegion userRegion = new SysUserRegion();
        userRegion.setUserId(dto.getUserId());
        userRegion.setRegionId(dto.getRegionId());
        userRegion.setUserType(dto.getUserType());
        sysUserRegionMapper.insert(userRegion);
    }

    /**
     * 移除用户辖区关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unassign(Long userId, Long regionId) {
        sysUserRegionMapper.delete(
                new LambdaQueryWrapper<SysUserRegion>()
                        .eq(SysUserRegion::getUserId, userId)
                        .eq(SysUserRegion::getRegionId, regionId));
    }

    /**
     * 查询指定辖区下的用户列表
     */
    @Override
    public List<SimpleUserVO> listUsersByRegion(Long regionId, Integer userType) {
        // 校验辖区存在
        SysRegion region = sysRegionMapper.selectById(regionId);
        if (region == null) {
            throw new BusinessException("辖区不存在");
        }

        // 查询辖区关联记录
        LambdaQueryWrapper<SysUserRegion> wrapper = new LambdaQueryWrapper<SysUserRegion>()
                .eq(SysUserRegion::getRegionId, regionId);
        if (userType != null) {
            wrapper.eq(SysUserRegion::getUserType, userType);
        }
        List<SysUserRegion> relations = sysUserRegionMapper.selectList(wrapper);

        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询用户信息
        Set<Long> userIds = relations.stream()
                .map(SysUserRegion::getUserId)
                .collect(Collectors.toSet());
        List<SysUser> users = sysUserMapper.selectBatchIds(userIds);

        List<SimpleUserVO> result = new ArrayList<>();
        for (SysUser user : users) {
            SimpleUserVO vo = new SimpleUserVO();
            vo.setId(user.getId());
            vo.setName(user.getName());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            vo.setGender(user.getGender());
            vo.setAge(user.getAge());
            result.add(vo);
        }
        return result;
    }

    /**
     * 查询当前医护负责的辖区列表
     */
    @Override
    public List<RegionVO> myRegions() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 查询当前医护关联的辖区（user_type=2医护）
        List<SysUserRegion> relations = sysUserRegionMapper.selectList(
                new LambdaQueryWrapper<SysUserRegion>()
                        .eq(SysUserRegion::getUserId, currentUserId)
                        .eq(SysUserRegion::getUserType, USER_TYPE_DOCTOR));

        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> regionIds = relations.stream()
                .map(SysUserRegion::getRegionId)
                .collect(Collectors.toSet());
        List<SysRegion> regions = sysRegionMapper.selectBatchIds(regionIds);

        List<RegionVO> result = new ArrayList<>();
        for (SysRegion region : regions) {
            RegionVO vo = new RegionVO();
            BeanUtils.copyProperties(region, vo);
            result.add(vo);
        }
        return result;
    }
}
