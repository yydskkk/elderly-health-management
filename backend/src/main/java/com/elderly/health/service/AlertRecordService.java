package com.elderly.health.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.dto.AlertHandleDTO;
import com.elderly.health.vo.AlertRecordVO;

/**
 * 预警记录服务接口
 *
 * @author elderly-health
 */
public interface AlertRecordService {

    /**
     * 分页查询预警记录（医护查询辖区内老人的预警记录）
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param status   状态筛选（0待处理1已处理）
     * @param dataType 数据类型筛选
     * @param userId   老人用户ID筛选
     * @return 预警记录分页
     */
    IPage<AlertRecordVO> page(Integer pageNum, Integer pageSize, Integer status, String dataType, Long userId);

    /**
     * 查询预警记录详情
     *
     * @param id 预警记录ID
     * @return 预警记录VO
     */
    AlertRecordVO detail(Long id);

    /**
     * 处理预警记录
     * 1. 更新预警记录 status=1已处理、handle_opinion、handle_time、handler_id
     * 2. 通知老年用户及家属（创建 alert_notification）
     *
     * @param dto 处理DTO
     */
    void handle(AlertHandleDTO dto);
}
