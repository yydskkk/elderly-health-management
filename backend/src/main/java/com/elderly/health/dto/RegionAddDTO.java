package com.elderly.health.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增辖区 DTO
 *
 * @author elderly-health
 */
@Data
public class RegionAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 辖区名称
     */
    @NotBlank(message = "辖区名称不能为空")
    private String regionName;

    /**
     * 描述
     */
    private String description;
}
