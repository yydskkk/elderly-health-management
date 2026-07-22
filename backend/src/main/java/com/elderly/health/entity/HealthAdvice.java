package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康建议实体类
 * 对应数据库 health_advice 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("health_advice")
public class HealthAdvice extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人用户ID
     */
    private Long userId;

    /**
     * 发布医生ID
     */
    private Long doctorId;

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
}
