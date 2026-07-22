package com.elderly.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.entity.SysRole;
import com.elderly.health.entity.SysUser;
import com.elderly.health.vo.UserManageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper 接口
 *
 * @author elderly-health
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND deleted = 0")
    SysUser selectByEmail(@Param("email") String email);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND ur.deleted = 0")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限编码列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0 AND rp.deleted = 0 AND ur.deleted = 0 " +
            "AND p.permission_code IS NOT NULL")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 分页查询用户管理列表（关联角色信息）
     *
     * @param page     分页参数
     * @param keyword  搜索关键词（姓名/邮箱）
     * @param roleCode 角色编码筛选
     * @param status   状态筛选
     * @return 用户管理VO分页
     */
    @Select("<script>" +
            "SELECT u.id, u.email, u.name, u.gender, u.age, u.phone, u.status, u.create_time, " +
            "r.role_code AS roleCode, r.role_name AS roleName " +
            "FROM sys_user u " +
            "LEFT JOIN sys_user_role ur ON u.id = ur.user_id AND ur.deleted = 0 " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id AND r.deleted = 0 " +
            "WHERE u.deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (u.name LIKE CONCAT('%', #{keyword}, '%') OR u.email LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='roleCode != null and roleCode != \"\"'>" +
            "AND r.role_code = #{roleCode} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND u.status = #{status} " +
            "</if>" +
            "ORDER BY u.create_time DESC" +
            "</script>")
    IPage<UserManageVO> selectUserPage(IPage<UserManageVO> page,
                                       @Param("keyword") String keyword,
                                       @Param("roleCode") String roleCode,
                                       @Param("status") Integer status);
}
