package com.elderly.health.service;

import com.elderly.health.vo.DistributionVO;
import com.elderly.health.vo.StatisticsChartVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务接口
 *
 * @author elderly-health
 */
public interface StatisticsService {

    /**
     * 总览数据统计
     *
     * @return 总览数据
     */
    Map<String, Object> overview();

    /**
     * 用户注册统计（按角色分组）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      统计类型：day/month
     * @return 统计图表数据
     */
    List<StatisticsChartVO> userRegister(LocalDate startDate, LocalDate endDate, String type);

    /**
     * 健康数据录入统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      统计类型：day/month
     * @param dataType  数据类型（可选）
     * @return 统计图表数据
     */
    List<StatisticsChartVO> healthData(LocalDate startDate, LocalDate endDate, String type, String dataType);

    /**
     * 预警发生统计
     *
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param type       统计类型：day/month
     * @param alertLevel 预警等级（可选）
     * @return 统计图表数据
     */
    List<StatisticsChartVO> alert(LocalDate startDate, LocalDate endDate, String type, Integer alertLevel);

    /**
     * 健康数据类型分布占比
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分布占比数据
     */
    List<DistributionVO> healthDataTypeDistribution(LocalDate startDate, LocalDate endDate);
}
