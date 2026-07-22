package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预警通知实体类
 * 对应数据库 alert_notification 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("alert_notification")
public class AlertNotification extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预警记录ID
     */
    private Long alertRecordId;

    /**
     * 接收人ID
     */
    private Long receiverId;

    /**
     * 是否已读：0未读1已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime readTime;
}
