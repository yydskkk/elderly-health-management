package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 验证码实体类
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("verification_code")
public class VerificationCode extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 用途：REGISTER/FORGET_PASSWORD
     */
    private String purpose;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /**
     * 是否已使用：0未使用1已使用
     */
    private Integer used;
}
