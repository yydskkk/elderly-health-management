package com.elderly.health.controller;

import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.ChangePasswordDTO;
import com.elderly.health.dto.UpdateProfileDTO;
import com.elderly.health.service.ProfileService;
import com.elderly.health.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人信息控制器
 * 处理当前用户个人信息查询、更新、修改密码
 *
 * @author elderly-health
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "个人信息管理", description = "个人信息查询、修改、密码修改等接口")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 获取个人信息
     *
     * @return 用户信息
     */
    @GetMapping
    @Operation(summary = "获取个人信息")
    public Result<UserInfoVO> getProfile() {
        UserInfoVO vo = profileService.getCurrentUserInfo();
        return Result.success(vo);
    }

    /**
     * 更新个人信息
     *
     * @param dto 更新请求
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "更新个人信息")
    @OperationLog(value = "更新个人信息", type = 3)
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        profileService.updateProfile(dto);
        return Result.success();
    }

    /**
     * 修改密码
     *
     * @param dto 修改密码请求
     * @return 操作结果
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @OperationLog(value = "修改密码", type = 3)
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        profileService.changePassword(dto);
        return Result.success();
    }
}
