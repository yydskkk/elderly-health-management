package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康数据记录实体类
 * 对应数据库 health_data 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("health_data")
public class HealthData extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人用户ID
     */
    private Long userId;

    /**
     * 数据类型code（health_data_type的code）
     */
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime measureTime;

    /**
     * 录入人ID
     */
    private Long recorderId;

    /**
     * 是否异常：0正常1异常
     */
    private Integer isAbnormal;
}
