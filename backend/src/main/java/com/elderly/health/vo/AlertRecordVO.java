package com.elderly.health.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预警记录 VO（含老人姓名、预警详情、处理信息）
 *
 * @author elderly-health
 */
@Data
public class AlertRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预警记录ID
     */
    private Long id;

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
     * 关联health_data的ID
     */
    private Long dataId;

    /**
     * 异常值描述
     */
    private String value;

    /**
     * 预警等级：1低2中3高
     */
    private Integer alertLevel;

    /**
     * 状态：0待处理1已处理
     */
    private Integer status;

    /**
     * 处理意见
     */
    private String handleOpinion;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime handleTime;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人姓名
     */
    private String handlerName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
