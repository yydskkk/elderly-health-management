package com.elderly.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 分配用户辖区 DTO
 *
 * @author elderly-health
 */
@Data
public class AssignRegionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 辖区ID
     */
    @NotNull(message = "辖区ID不能为空")
    private Long regionId;

    /**
     * 用户类型：1老人2医护
     */
    private Integer userType;
}
