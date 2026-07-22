package com.elderly.health.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增角色 DTO
 *
 * @author elderly-health
 */
@Data
public class RoleAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 描述
     */
    private String description;
}
