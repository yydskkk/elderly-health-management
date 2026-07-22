package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 干预方案实体类
 * 对应数据库 intervention_plan 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("intervention_plan")
public class InterventionPlan extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联health_advice的ID
     */
    private Long adviceId;

    /**
     * 老人用户ID
     */
    private Long userId;

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
}
