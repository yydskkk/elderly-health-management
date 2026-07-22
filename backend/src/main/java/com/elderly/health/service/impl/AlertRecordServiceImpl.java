package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.health.dto.AlertHandleDTO;
import com.elderly.health.entity.AlertNotification;
import com.elderly.health.entity.AlertRecord;
import com.elderly.health.entity.FamilyElderlyRelation;
import com.elderly.health.entity.SysUser;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.AlertNotificationMapper;
import com.elderly.health.mapper.AlertRecordMapper;
import com.elderly.health.mapper.FamilyElderlyRelationMapper;
import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.service.AlertRecordService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.AlertRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预警记录服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRecordServiceImpl implements AlertRecordService {

    private final AlertRecordMapper alertRecordMapper;
    private final AlertNotificationMapper alertNotificationMapper;
    private final SysUserMapper sysUserMapper;
    private final FamilyElderlyRelationMapper familyElderlyRelationMapper;

    /**
     * 分页查询预警记录（医护查询辖区内老人的预警记录）
     */
    @Override
    public IPage<AlertRecordVO> page(Integer pageNum, Integer pageSize, Integer status, String dataType, Long userId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(AlertRecord::getStatus, status);
        }
        if (StringUtils.hasText(dataType)) {
            wrapper.eq(AlertRecord::getDataType, dataType);
        }
        if (userId != null) {
            wrapper.eq(AlertRecord::getUserId, userId);
        }
        wrapper.orderByDesc(AlertRecord::getCreateTime);

        Page<AlertRecord> page = new Page<>(pageNum, pageSize);
        IPage<AlertRecord> recordPage = alertRecordMapper.selectPage(page, wrapper);

        Page<AlertRecordVO> resultPage = new Page<>(recordPage.getCurrent(),
                recordPage.getSize(), recordPage.getTotal());
        List<AlertRecord> records = recordPage.getRecords();
        if (records.isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }

        // 批量查询老人姓名
        Set<Long> userIds = records.stream()
                .map(AlertRecord::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            for (SysUser u : users) {
                userNameMap.put(u.getId(), u.getName());
            }
        }

        // 批量查询处理人姓名
        Set<Long> handlerIds = records.stream()
                .map(AlertRecord::getHandlerId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> handlerNameMap = new HashMap<>();
        if (!handlerIds.isEmpty()) {
            List<SysUser> handlers = sysUserMapper.selectBatchIds(handlerIds);
            for (SysUser u : handlers) {
                handlerNameMap.put(u.getId(), u.getName());
            }
        }

        List<AlertRecordVO> voList = new ArrayList<>();
        for (AlertRecord r : records) {
            AlertRecordVO vo = new AlertRecordVO();
            vo.setId(r.getId());
            vo.setUserId(r.getUserId());
            vo.setUserName(userNameMap.get(r.getUserId()));
            vo.setDataType(r.getDataType());
            vo.setDataId(r.getDataId());
            vo.setValue(r.getValue());
            vo.setAlertLevel(r.getAlertLevel());
            vo.setStatus(r.getStatus());
            vo.setHandleOpinion(r.getHandleOpinion());
            vo.setHandleTime(r.getHandleTime());
            vo.setHandlerId(r.getHandlerId());
            vo.setHandlerName(handlerNameMap.get(r.getHandlerId()));
            vo.setCreateTime(r.getCreateTime());
            voList.add(vo);
        }
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 查询预警记录详情
     */
    @Override
    public AlertRecordVO detail(Long id) {
        AlertRecord record = alertRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("预警记录不存在");
        }
        AlertRecordVO vo = new AlertRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setDataType(record.getDataType());
        vo.setDataId(record.getDataId());
        vo.setValue(record.getValue());
        vo.setAlertLevel(record.getAlertLevel());
        vo.setStatus(record.getStatus());
        vo.setHandleOpinion(record.getHandleOpinion());
        vo.setHandleTime(record.getHandleTime());
        vo.setHandlerId(record.getHandlerId());
        vo.setCreateTime(record.getCreateTime());

        // 老人姓名
        if (record.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(record.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
            }
        }
        // 处理人姓名
        if (record.getHandlerId() != null) {
            SysUser handler = sysUserMapper.selectById(record.getHandlerId());
            if (handler != null) {
                vo.setHandlerName(handler.getName());
            }
        }
        return vo;
    }

    /**
     * 处理预警记录
     * 1. 更新预警记录 status=1已处理、handle_opinion、handle_time、handler_id
     * 2. 通知老年用户及家属（创建 alert_notification）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(AlertHandleDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        AlertRecord record = alertRecordMapper.selectById(dto.getId());
        if (record == null) {
            throw new BusinessException("预警记录不存在");
        }

        // 更新预警记录
        record.setStatus(1);
        record.setHandleOpinion(dto.getHandleOpinion());
        record.setHandleTime(LocalDateTime.now());
        record.setHandlerId(currentUserId);
        alertRecordMapper.updateById(record);

        // 通知老年用户及家属
        List<Long> receiverIds = new ArrayList<>();
        receiverIds.add(record.getUserId());

        // 查询关联家属
        List<FamilyElderlyRelation> relations = familyElderlyRelationMapper.selectList(
                new LambdaQueryWrapper<FamilyElderlyRelation>()
                        .eq(FamilyElderlyRelation::getElderlyId, record.getUserId())
                        .eq(FamilyElderlyRelation::getStatus, 1));
        for (FamilyElderlyRelation relation : relations) {
            receiverIds.add(relation.getFamilyId());
        }

        // 去重后创建通知
        Set<Long> uniqueReceiverIds = new java.util.LinkedHashSet<>(receiverIds);
        for (Long receiverId : uniqueReceiverIds) {
            AlertNotification notification = new AlertNotification();
            notification.setAlertRecordId(record.getId());
            notification.setReceiverId(receiverId);
            notification.setIsRead(0);
            alertNotificationMapper.insert(notification);
        }

        log.info("预警记录处理完成，记录ID：{}，处理人：{}，通知人数：{}",
                record.getId(), currentUserId, uniqueReceiverIds.size());
    }
}
