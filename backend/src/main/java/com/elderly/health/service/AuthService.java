package com.elderly.health.service;

import com.elderly.health.dto.ForgotPasswordDTO;
import com.elderly.health.dto.LoginDTO;
import com.elderly.health.dto.RegisterDTO;
import com.elderly.health.dto.SendCodeDTO;
import com.elderly.health.vo.LoginVO;
import com.elderly.health.vo.UserInfoVO;

/**
 * 认证服务接口
 *
 * @author elderly-health
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param dto 注册请求
     */
    void register(RegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto   登录请求
     * @param ip    登录IP
     * @return 登录响应
     */
    LoginVO login(LoginDTO dto, String ip);

    /**
     * 忘记密码（重置密码）
     *
     * @param dto 忘记密码请求
     */
    void forgotPassword(ForgotPasswordDTO dto);

    /**
     * 发送验证码
     *
     * @param dto 发送验证码请求
     */
    void sendCode(SendCodeDTO dto);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    UserInfoVO getCurrentUserInfo();
}
