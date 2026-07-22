package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 数据类型字典实体类
 * 对应数据库 health_data_type 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("health_data_type")
public class HealthDataType extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 类型编码，如BLOOD_PRESSURE
     */
    private String code;

    /**
     * 类型名称，如血压
     */
    private String name;

    /**
     * 单位，如mmHg
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
