package com.elderly.health.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.vo.AlertNotificationVO;

/**
 * 预警通知服务接口
 *
 * @author elderly-health
 */
public interface AlertNotificationService {

    /**
     * 分页查询当前用户的预警通知
     * 老年用户/家属：返回发给自己的通知
     * 医护：返回辖区内老人的预警记录
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param isRead   是否已读筛选（可选）
     * @return 通知分页
     */
    IPage<AlertNotificationVO> page(Integer pageNum, Integer pageSize, Integer isRead);

    /**
     * 当前用户未读预警通知数量
     *
     * @return 未读数量
     */
    Integer unreadCount();

    /**
     * 标记单条通知为已读
     *
     * @param id 通知ID
     */
    void markRead(Long id);

    /**
     * 标记当前用户所有未读通知为已读
     */
    void markAllRead();
}
