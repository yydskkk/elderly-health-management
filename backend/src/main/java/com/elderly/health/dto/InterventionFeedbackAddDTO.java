package com.elderly.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 干预方案执行反馈新增 DTO
 *
 * @author elderly-health
 */
@Data
public class InterventionFeedbackAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 干预方案ID
     */
    @NotNull(message = "干预方案ID不能为空")
    private Long planId;

    /**
     * 是否执行：0未执行1已执行
     */
    private Integer isExecuted;

    /**
     * 执行感受
     */
    private String executionFeeling;

    /**
     * 异常情况
     */
    private String abnormalSituation;
}
