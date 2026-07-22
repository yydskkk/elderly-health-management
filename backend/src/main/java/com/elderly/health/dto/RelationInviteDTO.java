package com.elderly.health.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 老人邀请家属关联 DTO
 *
 * @author elderly-health
 */
@Data
public class RelationInviteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 家属邮箱
     */
    @NotBlank(message = "家属邮箱不能为空")
    private String familyEmail;

    /**
     * 关系类型，如父子、母女
     */
    private String relationType;
}
