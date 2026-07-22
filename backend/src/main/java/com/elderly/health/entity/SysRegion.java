package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 辖区实体类
 * 对应数据库 sys_region 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_region")
public class SysRegion extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 辖区名称
     */
    private String regionName;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0禁用1启用
     */
    private Integer status;
}
