package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.health.entity.OperationLog;
import com.elderly.health.mapper.OperationLogMapper;
import com.elderly.health.service.OperationLogService;
import com.elderly.health.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 保存操作日志（异步执行）
     *
     * @param operationLog 操作日志实体
     */
    @Async
    @Override
    public void saveLog(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 分页查询操作日志
     */
    @Override
    public IPage<OperationLogVO> page(Integer pageNum, Integer pageSize, String username, String operation,
                                      LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = buildQueryWrapper(username, operation, startTime, endTime);
        wrapper.orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        IPage<OperationLog> logPage = operationLogMapper.selectPage(page, wrapper);

        Page<OperationLogVO> resultPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        List<OperationLogVO> voList = new ArrayList<>();
        for (OperationLog log : logPage.getRecords()) {
            voList.add(convertToVO(log));
        }
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 查询操作日志列表（用于导出）
     */
    @Override
    public List<OperationLogVO> list(String username, String operation, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = buildQueryWrapper(username, operation, startTime, endTime);
        wrapper.orderByDesc(OperationLog::getCreateTime);

        List<OperationLog> logs = operationLogMapper.selectList(wrapper);
        List<OperationLogVO> result = new ArrayList<>();
        for (OperationLog log : logs) {
            result.add(convertToVO(log));
        }
        return result;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<OperationLog> buildQueryWrapper(String username, String operation,
                                                               LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (StringUtils.hasText(operation)) {
            wrapper.like(OperationLog::getOperation, operation);
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreateTime, endTime);
        }
        return wrapper;
    }

    /**
     * 实体转 VO
     */
    private OperationLogVO convertToVO(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
    }
}
