package com.elderly.health.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分布占比 VO
 *
 * @author elderly-health
 */
@Data
public class DistributionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 数值
     */
    private Long value;

    /**
     * 百分比
     */
    private BigDecimal percentage;
}
