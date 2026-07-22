package com.elderly.health.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色 VO
 *
 * @author elderly-health
 */
@Data
public class RoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否内置：0否1是
     */
    private Integer isBuiltIn;
}
