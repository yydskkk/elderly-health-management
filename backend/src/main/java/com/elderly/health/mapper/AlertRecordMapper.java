package com.elderly.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.health.entity.AlertRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预警记录 Mapper 接口
 *
 * @author elderly-health
 */
@Mapper
public interface AlertRecordMapper extends BaseMapper<AlertRecord> {
}
