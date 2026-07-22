package com.elderly.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 编辑用户 DTO
 *
 * @author elderly-health
 */
@Data
public class UserEditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别：0女1男
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色编码
     */
    private String roleCode;
}
