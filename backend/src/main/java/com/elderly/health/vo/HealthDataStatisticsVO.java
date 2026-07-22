package com.elderly.health.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 健康数据统计摘要 VO
 *
 * @author elderly-health
 */
@Data
public class HealthDataStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 单值指标平均值
     */
    private BigDecimal avgValue;

    /**
     * 单值指标最大值
     */
    private BigDecimal maxValue;

    /**
     * 单值指标最小值
     */
    private BigDecimal minValue;

    /**
     * 高压平均值
     */
    private BigDecimal avgValueHigh;

    /**
     * 高压最大值
     */
    private BigDecimal maxValueHigh;

    /**
     * 高压最小值
     */
    private BigDecimal minValueHigh;

    /**
     * 低压平均值
     */
    private BigDecimal avgValueLow;

    /**
     * 低压最大值
     */
    private BigDecimal maxValueLow;

    /**
     * 低压最小值
     */
    private BigDecimal minValueLow;

    /**
     * 数据总数
     */
    private Integer count;

    /**
     * 异常数
     */
    private Integer abnormalCount;
}
