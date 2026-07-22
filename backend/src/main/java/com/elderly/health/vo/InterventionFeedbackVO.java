package com.elderly.health.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 干预方案执行反馈 VO
 *
 * @author elderly-health
 */
@Data
public class InterventionFeedbackVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 反馈ID
     */
    private Long id;

    /**
     * 干预方案ID
     */
    private Long planId;

    /**
     * 反馈人ID
     */
    private Long userId;

    /**
     * 反馈人姓名
     */
    private String userName;

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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
