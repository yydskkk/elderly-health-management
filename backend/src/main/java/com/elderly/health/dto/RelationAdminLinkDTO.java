package com.elderly.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员/医护代关联 DTO
 *
 * @author elderly-health
 */
@Data
public class RelationAdminLinkDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 家属用户ID
     */
    @NotNull(message = "家属用户ID不能为空")
    private Long familyId;

    /**
     * 老人用户ID
     */
    @NotNull(message = "老人用户ID不能为空")
    private Long elderlyId;

    /**
     * 关系类型，如父子、母女
     */
    private String relationType;
}
