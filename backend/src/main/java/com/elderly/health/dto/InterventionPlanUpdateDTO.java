package com.elderly.health.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 干预方案更新 DTO
 *
 * @author elderly-health
 */
@Data
public class InterventionPlanUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 干预方案ID
     */
    @NotNull(message = "干预方案ID不能为空")
    private Long id;

    /**
     * 干预内容
     */
    private String content;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;

    /**
     * 执行周期，如每日/每周
     */
    private String cycle;
}
