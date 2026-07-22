package com.elderly.health.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应 VO
 *
 * @author elderly-health
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 姓名
     */
    private String name;

    /**
     * 角色编码
     */
    private String roleCode;
}
