package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.health.entity.SysPermission;
import com.elderly.health.mapper.SysPermissionMapper;
import com.elderly.health.service.SysPermissionService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.PermissionTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    /**
     * 查询权限树（全部权限）
     */
    @Override
    public List<PermissionTreeVO> getPermissionTree() {
        List<SysPermission> allPermissions = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getSort));
        return buildTree(allPermissions);
    }

    /**
     * 查询当前登录用户的菜单权限树（type=1）
     */
    @Override
    public List<PermissionTreeVO> getCurrentUserMenus() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<SysPermission> permissions = sysPermissionMapper.selectByUserId(userId);

        // 过滤出菜单类型（type=1）
        List<SysPermission> menus = permissions.stream()
                .filter(p -> p.getType() != null && p.getType() == 1)
                .collect(Collectors.toList());

        return buildTree(menus);
    }

    /**
     * 构建权限树形结构
     *
     * @param permissions 权限列表
     * @return 树形列表
     */
    private List<PermissionTreeVO> buildTree(List<SysPermission> permissions) {
        List<PermissionTreeVO> voList = new ArrayList<>();
        for (SysPermission p : permissions) {
            PermissionTreeVO vo = new PermissionTreeVO();
            BeanUtils.copyProperties(p, vo);
            voList.add(vo);
        }

        // 按 parentId 分组
        Map<Long, List<PermissionTreeVO>> parentMap = voList.stream()
                .collect(Collectors.groupingBy(vo ->
                        vo.getParentId() == null ? 0L : vo.getParentId()));

        // 设置 children
        for (PermissionTreeVO vo : voList) {
            List<PermissionTreeVO> children = parentMap.get(vo.getId());
            vo.setChildren(children);
        }

        // 返回顶级节点（parentId=0）
        return parentMap.getOrDefault(0L, new ArrayList<>());
    }
}
