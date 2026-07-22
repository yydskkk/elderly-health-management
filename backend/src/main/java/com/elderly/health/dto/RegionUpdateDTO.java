package com.elderly.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 编辑辖区 DTO
 *
 * @author elderly-health
 */
@Data
public class RegionUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 辖区ID
     */
    @NotNull(message = "辖区ID不能为空")
    private Long id;

    /**
     * 辖区名称
     */
    private String regionName;

    /**
     * 描述
     */
    private String description;
}
