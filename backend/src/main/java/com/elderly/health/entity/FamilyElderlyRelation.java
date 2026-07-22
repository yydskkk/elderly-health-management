package com.elderly.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 家属老人关联实体类
 *
 * @author elderly-health
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("family_elderly_relation")
public class FamilyElderlyRelation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 家属用户ID
     */
    private Long familyId;

    /**
     * 老人用户ID
     */
    private Long elderlyId;

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
}
