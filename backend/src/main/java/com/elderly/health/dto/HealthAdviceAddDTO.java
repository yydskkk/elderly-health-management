package com.elderly.health.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康建议发布 DTO
 *
 * @author elderly-health
 */
@Data
public class HealthAdviceAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人用户ID
     */
    @NotNull(message = "老人用户ID不能为空")
    private Long userId;

    /**
     * 建议标题
     */
    @NotBlank(message = "建议标题不能为空")
    private String title;

    /**
     * 建议内容
     */
    @NotBlank(message = "建议内容不能为空")
    private String content;

    /**
     * 建议类型，如饮食/运动/用药
     */
    private String adviceType;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /**
     * 干预方案列表（可选）
     */
    private List<InterventionPlanItem> interventionPlans;

    /**
     * 干预方案项（内部类）
     *
     * @author elderly-health
     */
    @Data
    public static class InterventionPlanItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 干预内容
         */
        @NotBlank(message = "干预内容不能为空")
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
}
