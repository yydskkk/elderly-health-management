package com.elderly.health.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 全局预警规则新增 DTO
 *
 * @author elderly-health
 */
@Data
public class AlertRuleAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据类型code
     */
    @NotBlank(message = "数据类型不能为空")
    private String dataType;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 单值指标下限
     */
    private BigDecimal minValue;

    /**
     * 单值指标上限
     */
    private BigDecimal maxValue;

    /**
     * 血压高压下限
     */
    private BigDecimal minValueHigh;

    /**
     * 血压高压上限
     */
    private BigDecimal maxValueHigh;

    /**
     * 血压低压下限
     */
    private BigDecimal minValueLow;

    /**
     * 血压低压上限
     */
    private BigDecimal maxValueLow;

    /**
     * 预警等级：1低2中3高
     */
    private Integer alertLevel;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0禁用1启用
     */
    private Integer status;
}
