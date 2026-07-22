package com.elderly.health.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.dto.InterventionFeedbackAddDTO;
import com.elderly.health.dto.InterventionPlanUpdateDTO;
import com.elderly.health.vo.InterventionFeedbackVO;
import com.elderly.health.vo.InterventionPlanVO;

import java.util.List;

/**
 * 干预方案服务接口
 *
 * @author elderly-health
 */
public interface InterventionPlanService {

    /**
     * 医护人员分页查询干预方案
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param userId   老人ID筛选（可选）
     * @param status   状态筛选（可选）
     * @return 干预方案VO分页（含老人姓名 + 建议标题 + 反馈列表）
     */
    IPage<InterventionPlanVO> page(Integer pageNum, Integer pageSize, Long userId, Integer status);

    /**
     * 更新干预方案内容
     *
     * @param dto 干预方案更新DTO
     */
    void update(InterventionPlanUpdateDTO dto);

    /**
     * 终止干预方案（status=0已终止）
     *
     * @param id 方案ID
     */
    void terminate(Long id);

    /**
     * 查询指定方案的所有执行反馈列表
     *
     * @param planId 方案ID
     * @return 反馈列表
     */
    List<InterventionFeedbackVO> listFeedbacks(Long planId);

    /**
     * 老年用户/家属查看进行中的干预方案
     * 老年用户：查询自己的进行中方案
     * 家属：校验关联后查询该老人的方案
     *
     * @param elderlyId 老人ID（家属查看时需传）
     * @return 干预方案VO列表
     */
    List<InterventionPlanVO> listMine(Long elderlyId);

    /**
     * 新增干预方案执行反馈
     * 校验方案存在且进行中，创建反馈记录，并通知相关医护（简化为记录日志）
     *
     * @param dto 反馈DTO
     */
    void addFeedback(InterventionFeedbackAddDTO dto);
}
