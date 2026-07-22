package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户辖区关联实体类
 * 对应数据库 sys_user_region 表
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_region")
public class SysUserRegion extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 辖区ID
     */
    private Long regionId;

    /**
     * 用户类型：1老人2医护
     */
    private Integer userType;
}
