package com.elderly.health.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.dto.HealthAdviceAddDTO;
import com.elderly.health.vo.HealthAdviceManageVO;
import com.elderly.health.vo.HealthAdviceVO;

import java.util.List;

/**
 * 健康建议服务接口
 *
 * @author elderly-health
 */
public interface HealthAdviceService {

    /**
     * 发布健康建议（医护人员）
     * 创建 health_advice 记录，若有干预方案则批量创建 intervention_plan 记录
     *
     * @param dto 健康建议DTO
     */
    void add(HealthAdviceAddDTO dto);

    /**
     * 老年用户/家属查看健康建议列表
     * 老年用户：查询发给自己的建议
     * 家属：校验关联后查询该老人的建议
     *
     * @param elderlyId 老人ID（家属查看时需传）
     * @return 健康建议VO列表（含干预方案列表 + 发布医生姓名）
     */
    List<HealthAdviceVO> listMine(Long elderlyId);

    /**
     * 医护人员分页查询自己发布的健康建议
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param userId   老人ID筛选（可选）
     * @param keyword  关键词筛选（可选）
     * @return 健康建议管理VO分页
     */
    IPage<HealthAdviceManageVO> page(Integer pageNum, Integer pageSize, Long userId, String keyword);

    /**
     * 查询健康建议详情（含干预方案列表）
     *
     * @param id 建议ID
     * @return 健康建议VO
     */
    HealthAdviceVO getById(Long id);
}
