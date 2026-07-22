package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderly.health.dto.UserAddDTO;
import com.elderly.health.dto.UserEditDTO;
import com.elderly.health.entity.SysRole;
import com.elderly.health.entity.SysUser;
import com.elderly.health.entity.SysUserRole;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.SysRoleMapper;
import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.mapper.SysUserRoleMapper;
import com.elderly.health.service.SysUserService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.UserInfoVO;
import com.elderly.health.vo.UserManageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据邮箱查询用户
     */
    @Override
    public SysUser selectByEmail(String email) {
        return sysUserMapper.selectByEmail(email);
    }

    /**
     * 根据用户ID查询角色列表
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        return sysUserMapper.selectRolesByUserId(userId);
    }

    /**
     * 构建用户信息VO（含角色信息）
     */
    @Override
    public UserInfoVO buildUserInfoVO(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setName(user.getName());
        vo.setGender(user.getGender());
        vo.setAge(user.getAge());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());

        // 查询角色信息
        List<SysRole> roles = sysUserMapper.selectRolesByUserId(userId);
        if (roles != null && !roles.isEmpty()) {
            SysRole role = roles.get(0);
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
        }
        return vo;
    }

    /**
     * 分页查询用户管理列表
     */
    @Override
    public IPage<UserManageVO> pageUsers(Integer pageNum, Integer pageSize, String keyword, String roleCode, Integer status) {
        Page<UserManageVO> page = new Page<>(pageNum, pageSize);
        return sysUserMapper.selectUserPage(page, keyword, roleCode, status);
    }

    /**
     * 新增用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserAddDTO dto) {
        // 校验邮箱唯一
        SysUser existing = sysUserMapper.selectByEmail(dto.getEmail());
        if (existing != null) {
            throw new BusinessException("邮箱已被注册：" + dto.getEmail());
        }

        // 校验角色是否存在
        SysRole role = selectRoleByCode(dto.getRoleCode());

        // 创建用户
        SysUser user = new SysUser();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setGender(dto.getGender());
        user.setAge(dto.getAge());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        sysUserMapper.insert(user);

        // 关联角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        sysUserRoleMapper.insert(userRole);
    }

    /**
     * 编辑用户基本信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editUser(UserEditDTO dto) {
        SysUser user = sysUserMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setName(dto.getName());
        user.setGender(dto.getGender());
        user.setAge(dto.getAge());
        user.setPhone(dto.getPhone());
        sysUserMapper.updateById(user);

        // 更新角色关联
        if (StringUtils.hasText(dto.getRoleCode())) {
            SysRole newRole = selectRoleByCode(dto.getRoleCode());
            // 查询当前角色关联
            List<SysUserRole> existingRoles = sysUserRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, dto.getId()));
            if (existingRoles.isEmpty()) {
                // 无角色则新增
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(dto.getId());
                userRole.setRoleId(newRole.getId());
                sysUserRoleMapper.insert(userRole);
            } else {
                // 角色变更则更新
                SysUserRole current = existingRoles.get(0);
                if (!current.getRoleId().equals(newRole.getId())) {
                    current.setRoleId(newRole.getId());
                    sysUserRoleMapper.updateById(current);
                }
            }
        }
    }

    /**
     * 更新用户状态
     */
    @Override
    public void updateStatus(Long userId, Integer status) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 禁止用户禁用自己的账号，避免账号自锁无法登录
        if (Integer.valueOf(0).equals(status) && userId.equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException("不能禁用自己的账号");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    /**
     * 重置用户密码
     */
    @Override
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 禁止删除自己
        if (userId.equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException("不能删除自己的账号");
        }
        // 逻辑删除用户
        sysUserMapper.deleteById(userId);
        // 逻辑删除用户角色关联
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
    }

    /**
     * 根据角色编码查询角色
     */
    private SysRole selectRoleByCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            throw new BusinessException("角色编码不能为空");
        }
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
        if (roles.isEmpty()) {
            throw new BusinessException("角色不存在：" + roleCode);
        }
        return roles.get(0);
    }
}
