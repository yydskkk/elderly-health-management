package com.elderly.health.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 家属老人关联 VO
 *
 * @author elderly-health
 */
@Data
public class RelationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    private Long id;

    /**
     * 家属用户ID
     */
    private Long familyId;

    /**
     * 家属姓名
     */
    private String familyName;

    /**
     * 家属邮箱
     */
    private String familyEmail;

    /**
     * 老人用户ID
     */
    private Long elderlyId;

    /**
     * 老人姓名
     */
    private String elderlyName;

    /**
     * 老人邮箱
     */
    private String elderlyEmail;

    /**
     * 关系类型，如父子、母女
     */
    private String relationType;

    /**
     * 状态：0待确认1已关联2已拒绝
     */
    private Integer status;

    /**
     * 关联方式：1老人邀请2家属申请3代关联
     */
    private Integer inviteWay;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
