package com.elderly.health.service;

import com.elderly.health.dto.RoleAddDTO;
import com.elderly.health.dto.RoleEditDTO;
import com.elderly.health.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author elderly-health
 */
public interface SysRoleService {

    /**
     * 查询所有角色列表
     *
     * @return 角色VO列表
     */
    List<RoleVO> listRoles();

    /**
     * 创建角色
     *
     * @param dto 角色新增DTO
     */
    void addRole(RoleAddDTO dto);

    /**
     * 编辑角色
     *
     * @param dto 角色编辑DTO
     */
    void editRole(RoleEditDTO dto);

    /**
     * 删除角色（内置角色不可删）
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 查询角色关联的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getRolePermissions(Long roleId);

    /**
     * 分配角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    void assignRolePermissions(Long roleId, List<Long> permissionIds);
}
