package com.elderly.health.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预警通知 VO（含预警详情）
 *
 * @author elderly-health
 */
@Data
public class AlertNotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 老人用户ID
     */
    private Long userId;

    /**
     * 老人姓名
     */
    private String userName;

    /**
     * 数据类型code
     */
    private String dataType;

    /**
     * 异常值描述
     */
    private String value;

    /**
     * 预警等级：1低2中3高
     */
    private Integer alertLevel;

    /**
     * 预警记录状态：0待处理1已处理
     */
    private Integer recordStatus;
}
