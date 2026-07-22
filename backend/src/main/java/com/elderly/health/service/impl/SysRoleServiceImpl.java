package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.health.dto.RoleAddDTO;
import com.elderly.health.dto.RoleEditDTO;
import com.elderly.health.entity.SysRole;
import com.elderly.health.entity.SysRolePermission;
import com.elderly.health.entity.SysUserRole;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.SysRoleMapper;
import com.elderly.health.mapper.SysRolePermissionMapper;
import com.elderly.health.mapper.SysUserRoleMapper;
import com.elderly.health.service.SysRoleService;
import com.elderly.health.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 查询所有角色列表
     */
    @Override
    public List<RoleVO> listRoles() {
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
        List<RoleVO> result = new ArrayList<>();
        for (SysRole role : roles) {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            result.add(vo);
        }
        return result;
    }

    /**
     * 创建角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(RoleAddDTO dto) {
        // 校验 roleCode 唯一
        Long count = sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode()));
        if (count > 0) {
            throw new BusinessException("角色编码已存在：" + dto.getRoleCode());
        }

        SysRole role = new SysRole();
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        role.setIsBuiltin(0);
        sysRoleMapper.insert(role);
    }

    /**
     * 编辑角色（内置角色仅可改 description）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editRole(RoleEditDTO dto) {
        SysRole role = sysRoleMapper.selectById(dto.getId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        if (role.getIsBuiltin() != null && role.getIsBuiltin() == 1) {
            // 内置角色仅可改 description
            role.setDescription(dto.getDescription());
        } else {
            role.setRoleName(dto.getRoleName());
            role.setDescription(dto.getDescription());
        }
        sysRoleMapper.updateById(role);
    }

    /**
     * 删除角色（内置角色不可删）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if (role.getIsBuiltin() != null && role.getIsBuiltin() == 1) {
            throw new BusinessException("内置角色不可删除");
        }

        // 删除角色关联的用户关系
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        // 删除角色关联的权限关系
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        // 删除角色
        sysRoleMapper.deleteById(id);
    }

    /**
     * 查询角色关联的权限ID列表
     */
    @Override
    public List<Long> getRolePermissions(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return sysRolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    /**
     * 分配角色权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除旧关联
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));

        // 插入新关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                sysRolePermissionMapper.insert(rp);
            }
        }
    }
}
