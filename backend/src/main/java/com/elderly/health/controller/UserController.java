package com.elderly.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.ResetPasswordDTO;
import com.elderly.health.dto.UserAddDTO;
import com.elderly.health.dto.UserEditDTO;
import com.elderly.health.dto.UserStatusDTO;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.SysUserService;
import com.elderly.health.vo.UserManageVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器
 *
 * @author elderly-health
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/user")
@RequiresRole("ADMIN")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    /**
     * 用户列表分页查询
     *
     * @param pageNum  当前页（默认1）
     * @param pageSize 每页大小（默认10）
     * @param keyword  搜索关键词（姓名/邮箱）
     * @param roleCode 角色编码筛选
     * @param status   状态筛选
     * @return 用户分页数据
     */
    @Operation(summary = "用户列表分页查询")
    @GetMapping("/page")
    public Result<IPage<UserManageVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {
        IPage<UserManageVO> page = sysUserService.pageUsers(pageNum, pageSize, keyword, roleCode, status);
        return Result.success(page);
    }

    /**
     * 新增用户
     *
     * @param dto 用户新增DTO
     * @return 操作结果
     */
    @Operation(summary = "新增用户")
    @OperationLog(value = "新增用户", type = 2)
    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserAddDTO dto) {
        sysUserService.addUser(dto);
        return Result.success();
    }

    /**
     * 编辑用户基本信息
     *
     * @param dto 用户编辑DTO
     * @return 操作结果
     */
    @Operation(summary = "编辑用户")
    @OperationLog(value = "编辑用户", type = 3)
    @PutMapping
    public Result<Void> edit(@Valid @RequestBody UserEditDTO dto) {
        sysUserService.editUser(dto);
        return Result.success();
    }

    /**
     * 禁用/启用账号
     *
     * @param dto 状态变更DTO
     * @return 操作结果
     */
    @Operation(summary = "禁用/启用账号")
    @OperationLog(value = "禁用/启用账号", type = 3)
    @PutMapping("/status")
    public Result<Void> updateStatus(@Valid @RequestBody UserStatusDTO dto) {
        sysUserService.updateStatus(dto.getUserId(), dto.getStatus());
        return Result.success();
    }

    /**
     * 重置用户密码
     *
     * @param dto 重置密码DTO
     * @return 操作结果
     */
    @Operation(summary = "重置密码")
    @OperationLog(value = "重置用户密码", type = 3)
    @PutMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        sysUserService.resetPassword(dto.getUserId(), dto.getNewPassword());
        return Result.success();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户")
    @OperationLog(value = "删除用户", type = 4)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }
}
