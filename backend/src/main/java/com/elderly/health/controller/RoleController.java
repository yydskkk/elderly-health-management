package com.elderly.health.controller;

import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.RoleAddDTO;
import com.elderly.health.dto.RoleEditDTO;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.SysRoleService;
import com.elderly.health.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理控制器
 *
 * @author elderly-health
 */
@Tag(name = "角色管理", description = "角色权限管理相关接口")
@RestController
@RequestMapping("/api/role")
@RequiresRole("ADMIN")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleService sysRoleService;

    /**
     * 查询所有角色列表
     *
     * @return 角色列表
     */
    @Operation(summary = "角色列表")
    @GetMapping("/list")
    public Result<List<RoleVO>> list() {
        List<RoleVO> roles = sysRoleService.listRoles();
        return Result.success(roles);
    }

    /**
     * 创建角色
     *
     * @param dto 角色新增DTO
     * @return 操作结果
     */
    @Operation(summary = "创建角色")
    @OperationLog(value = "创建角色", type = 2)
    @PostMapping
    public Result<Void> add(@Valid @RequestBody RoleAddDTO dto) {
        sysRoleService.addRole(dto);
        return Result.success();
    }

    /**
     * 编辑角色
     *
     * @param dto 角色编辑DTO
     * @return 操作结果
     */
    @Operation(summary = "编辑角色")
    @OperationLog(value = "编辑角色", type = 3)
    @PutMapping
    public Result<Void> edit(@Valid @RequestBody RoleEditDTO dto) {
        sysRoleService.editRole(dto);
        return Result.success();
    }

    /**
     * 删除角色（内置角色不可删）
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色")
    @OperationLog(value = "删除角色", type = 4)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 查询角色关联的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Operation(summary = "查询角色权限")
    @GetMapping("/{roleId}/permissions")
    public Result<List<Long>> getPermissions(@PathVariable Long roleId) {
        List<Long> permissionIds = sysRoleService.getRolePermissions(roleId);
        return Result.success(permissionIds);
    }

    /**
     * 分配角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    @Operation(summary = "分配角色权限")
    @OperationLog(value = "分配角色权限", type = 3)
    @PutMapping("/{roleId}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long roleId,
                                          @RequestBody List<Long> permissionIds) {
        sysRoleService.assignRolePermissions(roleId, permissionIds);
        return Result.success();
    }
}
