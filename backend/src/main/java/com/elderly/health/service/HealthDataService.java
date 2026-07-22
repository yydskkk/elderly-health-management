package com.elderly.health.service;

import com.elderly.health.dto.HealthDataAddDTO;
import com.elderly.health.vo.HealthDataStatisticsVO;
import com.elderly.health.vo.HealthDataTrendVO;
import com.elderly.health.vo.HealthDataTypeVO;
import com.elderly.health.vo.HealthDataVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康数据服务接口
 *
 * @author elderly-health
 */
public interface HealthDataService {

    /**
     * 老年用户录入健康数据
     *
     * @param dto 健康数据DTO
     */
    void addByElderly(HealthDataAddDTO dto);

    /**
     * 家属代录入健康数据
     *
     * @param elderlyId 老人ID
     * @param dto       健康数据DTO
     */
    void addByFamily(Long elderlyId, HealthDataAddDTO dto);

    /**
     * 查询健康数据列表
     *
     * @param dataType  数据类型（必填）
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param elderlyId 老人ID（家属/医护查看时需传）
     * @return 健康数据VO列表
     */
    List<HealthDataVO> list(String dataType, LocalDateTime startTime, LocalDateTime endTime, Long elderlyId);

    /**
     * 查询健康数据趋势
     *
     * @param dataType 数据类型
     * @param days     天数（默认30）
     * @param elderlyId 老人ID
     * @return 趋势数据列表
     */
    List<HealthDataTrendVO> trend(String dataType, Integer days, Long elderlyId);

    /**
     * 健康数据统计摘要
     *
     * @param dataType  数据类型
     * @param elderlyId 老人ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计摘要
     */
    HealthDataStatisticsVO statistics(String dataType, Long elderlyId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询所有健康数据类型
     *
     * @return 数据类型列表
     */
    List<HealthDataTypeVO> types();
}
