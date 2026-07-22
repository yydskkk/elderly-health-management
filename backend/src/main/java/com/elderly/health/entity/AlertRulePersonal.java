package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 个性化预警规则实体类
 * 对应数据库 alert_rule_personal 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("alert_rule_personal")
public class AlertRulePersonal extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人用户ID
     */
    private Long userId;

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
}
