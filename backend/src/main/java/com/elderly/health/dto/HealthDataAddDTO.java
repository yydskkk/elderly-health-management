package com.elderly.health.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康数据录入 DTO
 *
 * @author elderly-health
 */
@Data
public class HealthDataAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据类型code
     */
    @NotBlank(message = "数据类型不能为空")
    private String dataType;

    /**
     * 高压值（血压专用）
     */
    private BigDecimal valueHigh;

    /**
     * 低压值（血压专用）
     */
    private BigDecimal valueLow;

    /**
     * 单值指标（血糖、心率、血氧等）
     */
    private BigDecimal value;

    /**
     * 测量时间
     */
    @NotNull(message = "测量时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime measureTime;
}
