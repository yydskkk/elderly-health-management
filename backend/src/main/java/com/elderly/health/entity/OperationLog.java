package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 对应数据库 operation_log 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_log")
public class OperationLog extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作内容
     */
    private String operation;

    /**
     * 请求方法（类名#方法名）
     */
    private String method;

    /**
     * 请求参数（JSON）
     */
    private String params;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 操作地点
     */
    private String location;

    /**
     * 耗时（毫秒）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long time;
}
