package com.elderly.health.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 健康数据类型 VO
 *
 * @author elderly-health
 */
@Data
public class HealthDataTypeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 类型编码
     */
    private String code;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 单位
     */
    private String unit;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;
}
