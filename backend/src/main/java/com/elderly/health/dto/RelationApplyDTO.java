package com.elderly.health.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 家属申请关联老人 DTO
 *
 * @author elderly-health
 */
@Data
public class RelationApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人邮箱
     */
    @NotBlank(message = "老人邮箱不能为空")
    private String elderlyEmail;

    /**
     * 关系类型，如父子、母女
     */
    private String relationType;
}
