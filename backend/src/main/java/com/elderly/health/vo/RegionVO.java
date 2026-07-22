package com.elderly.health.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 辖区 VO
 *
 * @author elderly-health
 */
@Data
public class RegionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 辖区ID
     */
    private Long id;

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

    /**
     * 老人数
     */
    private Integer elderlyCount;

    /**
     * 医护数
     */
    private Integer doctorCount;
}
