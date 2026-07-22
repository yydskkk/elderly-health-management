package com.elderly.health.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警规则 VO
 *
 * @author elderly-health
 */
@Data
public class AlertRuleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    private Long id;

    /**
     * 数据类型code
     */
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

    /**
     * 是否个性化规则（true个性化 false全局）
     */
    private Boolean personal;

    /**
     * 用户ID（个性化规则时返回）
     */
    private Long userId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
