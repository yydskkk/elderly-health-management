package com.elderly.health.service;

/**
 * 邮件服务接口
 *
 * @author elderly-health
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     *
     * @param email   收件人邮箱
     * @param code    验证码
     * @param purpose 用途：REGISTER/FORGET_PASSWORD
     */
    void sendVerificationCode(String email, String code, String purpose);
}
