package com.elderly.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 健康档案更新 DTO
 *
 * @author elderly-health
 */
@Data
public class ElderlyProfileUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 档案ID
     */
    @NotNull(message = "档案ID不能为空")
    private Long id;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 居住地址
     */
    private String address;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    /**
     * 既往病史
     */
    private String medicalHistory;

    /**
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 家族病史
     */
    private String familyHistory;

    /**
     * 血型
     */
    private String bloodType;

    /**
     * 身高(cm)
     */
    private BigDecimal height;

    /**
     * 体重(kg)
     */
    private BigDecimal weight;
}
