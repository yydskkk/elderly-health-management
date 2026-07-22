package com.elderly.health.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 干预方案 VO（含老人姓名、建议标题、反馈列表）
 *
 * @author elderly-health
 */
@Data
public class InterventionPlanVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 方案ID
     */
    private Long id;

    /**
     * 关联健康建议ID
     */
    private Long adviceId;

    /**
     * 老人用户ID
     */
    private Long userId;

    /**
     * 老人姓名
     */
    private String userName;

    /**
     * 干预内容
     */
    private String content;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;

    /**
     * 执行周期，如每日/每周
     */
    private String cycle;

    /**
     * 状态：0已终止1进行中2已完成
     */
    private Integer status;

    /**
     * 关联建议标题
     */
    private String adviceTitle;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 执行反馈列表
     */
    private List<InterventionFeedbackVO> feedbacks;
}
