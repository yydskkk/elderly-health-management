package com.elderly.health.controller;

import com.elderly.health.common.Result;
import com.elderly.health.service.SysPermissionService;
import com.elderly.health.vo.PermissionTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限管理控制器
 *
 * @author elderly-health
 */
@Tag(name = "权限管理", description = "权限树与菜单查询接口")
@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final SysPermissionService sysPermissionService;

    /**
     * 查询权限树（全部权限）
     *
     * @return 权限树形列表
     */
    @Operation(summary = "权限树查询")
    @GetMapping("/tree")
    public Result<List<PermissionTreeVO>> tree() {
        List<PermissionTreeVO> tree = sysPermissionService.getPermissionTree();
        return Result.success(tree);
    }

    /**
     * 查询当前登录用户的菜单权限树
     *
     * @return 菜单权限树形列表
     */
    @Operation(summary = "当前用户菜单")
    @GetMapping("/menus")
    public Result<List<PermissionTreeVO>> menus() {
        List<PermissionTreeVO> menus = sysPermissionService.getCurrentUserMenus();
        return Result.success(menus);
    }
}
