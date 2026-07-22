package com.elderly.health.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.entity.OperationLog;
import com.elderly.health.vo.OperationLogVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务接口
 *
 * @author elderly-health
 */
public interface OperationLogService {

    /**
     * 保存操作日志（异步执行）
     *
     * @param log 操作日志实体
     */
    void saveLog(OperationLog log);

    /**
     * 分页查询操作日志
     *
     * @param pageNum    当前页
     * @param pageSize   每页大小
     * @param username   用户名筛选
     * @param operation  操作内容筛选
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 操作日志分页
     */
    IPage<OperationLogVO> page(Integer pageNum, Integer pageSize, String username, String operation,
                               LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询操作日志列表（用于导出）
     *
     * @param username   用户名筛选
     * @param operation  操作内容筛选
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 操作日志列表
     */
    List<OperationLogVO> list(String username, String operation, LocalDateTime startTime, LocalDateTime endTime);
}
