package com.elderly.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.health.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联 Mapper 接口
 *
 * @author elderly-health
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 物理删除角色关联的所有权限（用于重新分配权限时清理旧关联）
     * 使用逻辑删除方式
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Delete("UPDATE sys_role_permission SET deleted = 1 WHERE role_id = #{roleId} AND deleted = 0")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID查询关联的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId} AND deleted = 0")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
