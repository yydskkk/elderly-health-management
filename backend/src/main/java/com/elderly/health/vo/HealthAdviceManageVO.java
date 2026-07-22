package com.elderly.health.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康建议管理 VO（含建议信息 + 老人姓名 + 发布医生姓名）
 * 用于医护人员分页查询
 *
 * @author elderly-health
 */
@Data
public class HealthAdviceManageVO implements Serializable {

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
     * 老人姓名
     */
    private String elderlyName;

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
}
