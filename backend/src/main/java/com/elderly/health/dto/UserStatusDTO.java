package com.elderly.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户状态变更 DTO
 *
 * @author elderly-health
 */
@Data
public class UserStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 状态：0禁用1启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
