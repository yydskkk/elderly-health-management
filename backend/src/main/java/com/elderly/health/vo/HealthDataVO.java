package com.elderly.health.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康数据 VO
 *
 * @author elderly-health
 */
@Data
public class HealthDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 数据类型code
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
     * 单值指标
     */
    private BigDecimal value;

    /**
     * 测量时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime measureTime;

    /**
     * 是否异常：0正常1异常
     */
    private Integer isAbnormal;

    /**
     * 录入人姓名
     */
    private String recorderName;
}
