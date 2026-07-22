package com.elderly.health.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 健康数据趋势 VO
 *
 * @author elderly-health
 */
@Data
public class HealthDataTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate date;

    /**
     * 数值（单值指标）
     */
    private BigDecimal value;

    /**
     * 高压值（血压专用）
     */
    private BigDecimal valueHigh;

    /**
     * 低压值（血压专用）
     */
    private BigDecimal valueLow;

    /**
     * 是否异常：0正常1异常
     */
    private Integer isAbnormal;
}
