package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 干预方案执行反馈实体类
 * 对应数据库 intervention_feedback 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("intervention_feedback")
public class InterventionFeedback extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 干预方案ID
     */
    private Long planId;

    /**
     * 反馈人ID
     */
    private Long userId;

    /**
     * 是否执行：0未执行1已执行
     */
    private Integer isExecuted;

    /**
     * 执行感受
     */
    private String executionFeeling;

    /**
     * 异常情况
     */
    private String abnormalSituation;

    /**
     * 反馈时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime feedbackTime;
}
