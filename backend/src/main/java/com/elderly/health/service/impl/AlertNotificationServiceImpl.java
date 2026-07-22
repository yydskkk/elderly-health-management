package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.health.entity.AlertNotification;
import com.elderly.health.entity.AlertRecord;
import com.elderly.health.entity.SysUser;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.AlertNotificationMapper;
import com.elderly.health.mapper.AlertRecordMapper;
import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.service.AlertNotificationService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.AlertNotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预警通知服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotificationServiceImpl implements AlertNotificationService {

    private final AlertNotificationMapper alertNotificationMapper;
    private final AlertRecordMapper alertRecordMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 分页查询当前用户的预警通知
     * 老年用户/家属：返回发给自己的通知
     * 医护：返回辖区内老人的预警记录
     */
    @Override
    public IPage<AlertNotificationVO> page(Integer pageNum, Integer pageSize, Integer isRead) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String roleCode = SecurityUtils.getCurrentUserRole();

        Page<AlertNotification> page = new Page<>(pageNum, pageSize);

        // 医护角色：查询辖区内老人的预警记录（通过 alert_notification 中接收人为自己）
        // 老年用户/家属：返回发给自己的通知
        LambdaQueryWrapper<AlertNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertNotification::getReceiverId, currentUserId);
        if (isRead != null) {
            wrapper.eq(AlertNotification::getIsRead, isRead);
        }
        wrapper.orderByDesc(AlertNotification::getCreateTime);

        IPage<AlertNotification> notificationPage = alertNotificationMapper.selectPage(page, wrapper);

        // 转换为 VO，补充预警详情
        Page<AlertNotificationVO> resultPage = new Page<>(notificationPage.getCurrent(),
                notificationPage.getSize(), notificationPage.getTotal());
        List<AlertNotification> records = notificationPage.getRecords();
        if (records.isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }

        // 批量查询预警记录
        Set<Long> recordIds = records.stream()
                .map(AlertNotification::getAlertRecordId)
                .collect(Collectors.toSet());
        Map<Long, AlertRecord> recordMap = new java.util.HashMap<>();
        if (!recordIds.isEmpty()) {
            List<AlertRecord> alertRecords = alertRecordMapper.selectBatchIds(recordIds);
            for (AlertRecord r : alertRecords) {
                recordMap.put(r.getId(), r);
            }
        }

        // 批量查询老人姓名
        Set<Long> userIds = recordMap.values().stream()
                .map(AlertRecord::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            for (SysUser u : users) {
                userNameMap.put(u.getId(), u.getName());
            }
        }

        List<AlertNotificationVO> voList = new ArrayList<>();
        for (AlertNotification n : records) {
            AlertNotificationVO vo = new AlertNotificationVO();
            vo.setId(n.getId());
            vo.setAlertRecordId(n.getAlertRecordId());
            vo.setReceiverId(n.getReceiverId());
            vo.setIsRead(n.getIsRead());
            vo.setReadTime(n.getReadTime());
            vo.setCreateTime(n.getCreateTime());

            AlertRecord record = recordMap.get(n.getAlertRecordId());
            if (record != null) {
                vo.setUserId(record.getUserId());
                vo.setUserName(userNameMap.get(record.getUserId()));
                vo.setDataType(record.getDataType());
                vo.setValue(record.getValue());
                vo.setAlertLevel(record.getAlertLevel());
                vo.setRecordStatus(record.getStatus());
            }
            voList.add(vo);
        }
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 当前用户未读预警通知数量
     */
    @Override
    public Integer unreadCount() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Long count = alertNotificationMapper.selectCount(
                new LambdaQueryWrapper<AlertNotification>()
                        .eq(AlertNotification::getReceiverId, currentUserId)
                        .eq(AlertNotification::getIsRead, 0));
        return count == null ? 0 : count.intValue();
    }

    /**
     * 标记单条通知为已读
     */
    @Override
    public void markRead(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        AlertNotification notification = alertNotificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getReceiverId().equals(currentUserId)) {
            throw new BusinessException("无权操作该通知");
        }
        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        alertNotificationMapper.updateById(notification);
    }

    /**
     * 标记当前用户所有未读通知为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<AlertNotification> list = alertNotificationMapper.selectList(
                new LambdaQueryWrapper<AlertNotification>()
                        .eq(AlertNotification::getReceiverId, currentUserId)
                        .eq(AlertNotification::getIsRead, 0));
        LocalDateTime now = LocalDateTime.now();
        for (AlertNotification n : list) {
            n.setIsRead(1);
            n.setReadTime(now);
            alertNotificationMapper.updateById(n);
        }
    }
}
