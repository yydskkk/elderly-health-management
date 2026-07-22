package com.elderly.health.service;

/**
 * 验证码服务接口
 *
 * @author elderly-health
 */
public interface VerificationCodeService {

    /**
     * 发送验证码
     *
     * @param email   邮箱
     * @param purpose 用途：REGISTER/FORGET_PASSWORD
     */
    void sendCode(String email, String purpose);

    /**
     * 校验验证码
     *
     * @param email   邮箱
     * @param code    验证码
     * @param purpose 用途：REGISTER/FORGET_PASSWORD
     * @return 校验通过返回 true
     */
    boolean verifyCode(String email, String code, String purpose);
}
