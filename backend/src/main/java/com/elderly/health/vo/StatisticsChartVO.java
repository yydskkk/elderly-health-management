package com.elderly.health.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 统计图表 VO
 *
 * @author elderly-health
 */
@Data
public class StatisticsChartVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期（yyyy-MM-dd 或 yyyy-MM）
     */
    private String date;

    /**
     * 数值
     */
    private Long value;

    /**
     * 分类（角色编码/数据类型/预警等级等）
     */
    private String category;
}
