package com.elderly.health.controller;

import com.elderly.health.common.Result;
import com.elderly.health.dto.ForgotPasswordDTO;
import com.elderly.health.dto.LoginDTO;
import com.elderly.health.dto.RegisterDTO;
import com.elderly.health.dto.SendCodeDTO;
import com.elderly.health.service.AuthService;
import com.elderly.health.utils.IpUtils;
import com.elderly.health.vo.LoginVO;
import com.elderly.health.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * 处理用户注册、登录、密码重置、发送验证码、获取当前用户信息
 *
 * @author elderly-health
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户注册、登录、密码重置等接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     *
     * @param dto 注册请求
     * @return 操作结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    /**
     * 用户登录
     *
     * @param dto     登录请求
     * @param request HTTP 请求对象
     * @return 登录响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String ip = IpUtils.getIpAddress(request);
        LoginVO vo = authService.login(dto, ip);
        return Result.success(vo);
    }

    /**
     * 忘记密码（重置密码）
     *
     * @param dto 忘记密码请求
     * @return 操作结果
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        authService.forgotPassword(dto);
        return Result.success();
    }

    /**
     * 发送验证码
     *
     * @param dto 发送验证码请求
     * @return 操作结果
     */
    @PostMapping("/send-code")
    @Operation(summary = "发送验证码")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        authService.sendCode(dto);
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfoVO> info() {
        UserInfoVO vo = authService.getCurrentUserInfo();
        return Result.success(vo);
    }
}
