package com.elderly.health.common;

/**
 * 系统常量类
 *
 * @author elderly-health
 */
public class Constants {

    private Constants() {
    }

    /**
     * 验证码用途：注册
     */
    public static final String VERIFICATION_PURPOSE_REGISTER = "REGISTER";

    /**
     * 验证码用途：忘记密码
     */
    public static final String VERIFICATION_PURPOSE_FORGET_PASSWORD = "FORGET_PASSWORD";

    /**
     * 验证码过期时间（分钟）
     */
    public static final int VERIFICATION_CODE_EXPIRE_MINUTES = 5;

    /**
     * 验证码发送间隔时间（分钟）
     */
    public static final int VERIFICATION_CODE_SEND_INTERVAL_MINUTES = 1;
}
