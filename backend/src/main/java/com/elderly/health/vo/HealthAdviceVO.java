package com.elderly.health.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康建议 VO（含建议信息 + 干预方案列表 + 发布医生姓名）
 *
 * @author elderly-health
 */
@Data
public class HealthAdviceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 建议ID
     */
    private Long id;

    /**
     * 老人用户ID
     */
    private Long userId;

    /**
     * 发布医生ID
     */
    private Long doctorId;

    /**
     * 发布医生姓名
     */
    private String doctorName;

    /**
     * 建议标题
     */
    private String title;

    /**
     * 建议内容
     */
    private String content;

    /**
     * 建议类型，如饮食/运动/用药
     */
    private String adviceType;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /**
     * 状态：0失效1有效
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 干预方案列表
     */
    private List<InterventionPlanVO> interventionPlans;
}
