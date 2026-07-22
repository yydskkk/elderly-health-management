package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预警记录实体类
 * 对应数据库 alert_record 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("alert_record")
public class AlertRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人用户ID
     */
    private Long userId;

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
}
