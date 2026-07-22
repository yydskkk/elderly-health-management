package com.elderly.health.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elderly.health.dto.UserAddDTO;
import com.elderly.health.dto.UserEditDTO;
import com.elderly.health.entity.SysRole;
import com.elderly.health.entity.SysUser;
import com.elderly.health.vo.UserInfoVO;
import com.elderly.health.vo.UserManageVO;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author elderly-health
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    SysUser selectByEmail(String email);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectRolesByUserId(Long userId);

    /**
     * 构建用户信息VO（含角色信息）
     *
     * @param userId 用户ID
     * @return 用户信息VO
     */
    UserInfoVO buildUserInfoVO(Long userId);

    /**
     * 分页查询用户管理列表
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param keyword  搜索关键词（姓名/邮箱）
     * @param roleCode 角色编码筛选
     * @param status   状态筛选
     * @return 用户管理VO分页
     */
    IPage<UserManageVO> pageUsers(Integer pageNum, Integer pageSize, String keyword, String roleCode, Integer status);

    /**
     * 新增用户
     *
     * @param dto 用户新增DTO
     */
    void addUser(UserAddDTO dto);

    /**
     * 编辑用户基本信息
     *
     * @param dto 用户编辑DTO
     */
    void editUser(UserEditDTO dto);

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param userId 用户ID
     * @param status 状态：0禁用1启用
     */
    void updateStatus(Long userId, Integer status);

    /**
     * 重置用户密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 删除用户（逻辑删除）
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);
}
