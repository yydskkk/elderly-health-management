package com.elderly.health.service;

import com.elderly.health.vo.PermissionTreeVO;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author elderly-health
 */
public interface SysPermissionService {

    /**
     * 查询权限树（全部权限）
     *
     * @return 权限树形列表
     */
    List<PermissionTreeVO> getPermissionTree();

    /**
     * 查询当前登录用户的菜单权限树（type=1）
     *
     * @return 菜单权限树形列表
     */
    List<PermissionTreeVO> getCurrentUserMenus();
}
