package com.elderly.health.service;

import com.elderly.health.dto.RelationAdminLinkDTO;
import com.elderly.health.dto.RelationApplyDTO;
import com.elderly.health.dto.RelationInviteDTO;
import com.elderly.health.vo.RelationVO;
import com.elderly.health.vo.SimpleUserVO;

import java.util.List;

/**
 * 家属老人关联服务接口
 *
 * @author elderly-health
 */
public interface RelationService {

    /**
     * 老人邀请家属
     *
     * @param dto 邀请DTO
     */
    void invite(RelationInviteDTO dto);

    /**
     * 家属申请关联老人
     *
     * @param dto 申请DTO
     */
    void apply(RelationApplyDTO dto);

    /**
     * 管理员/医护代关联
     *
     * @param dto 代关联DTO
     */
    void adminLink(RelationAdminLinkDTO dto);

    /**
     * 确认关联
     *
     * @param id 关联ID
     */
    void confirm(Long id);

    /**
     * 拒绝关联
     *
     * @param id 关联ID
     */
    void reject(Long id);

    /**
     * 查询关联关系列表（根据当前用户角色返回相关记录）
     *
     * @param status 状态筛选（可选）
     * @return 关联VO列表
     */
    List<RelationVO> list(Integer status);

    /**
     * 家属查询关联老人列表
     *
     * @return 老人简单信息列表
     */
    List<SimpleUserVO> myElderly();

    /**
     * 老人查询关联家属列表
     *
     * @return 家属简单信息列表
     */
    List<SimpleUserVO> myFamily();
}
