package com.elderly.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.health.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper 接口
 *
 * @author elderly-health
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
