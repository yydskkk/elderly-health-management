package com.elderly.health.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 预警处理 DTO
 *
 * @author elderly-health
 */
@Data
public class AlertHandleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预警记录ID
     */
    @NotNull(message = "预警记录ID不能为空")
    private Long id;

    /**
     * 处理意见
     */
    @NotBlank(message = "处理意见不能为空")
    private String handleOpinion;
}
