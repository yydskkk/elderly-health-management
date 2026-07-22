package com.elderly.health.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 简单用户信息 VO（用于关联列表展示）
 *
 * @author elderly-health
 */
@Data
public class SimpleUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别：0女1男
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;
}
