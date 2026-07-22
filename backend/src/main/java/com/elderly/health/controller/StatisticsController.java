package com.elderly.health.controller;

import com.elderly.health.common.Result;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.StatisticsService;
import com.elderly.health.vo.DistributionVO;
import com.elderly.health.vo.StatisticsChartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据统计控制器
 *
 * @author elderly-health
 */
@Tag(name = "数据统计", description = "系统数据统计相关接口")
@RestController
@RequestMapping("/api/statistics")
@RequiresRole("ADMIN")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 总览数据统计
     *
     * @return 总览数据
     */
    @Operation(summary = "总览数据统计")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = statisticsService.overview();
        return Result.success(data);
    }

    /**
     * 用户注册统计（按角色分组）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      统计类型：day/month
     * @return 统计图表数据
     */
    @Operation(summary = "用户注册统计")
    @GetMapping("/user-register")
    public Result<List<StatisticsChartVO>> userRegister(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "day") String type) {
        List<StatisticsChartVO> list = statisticsService.userRegister(startDate, endDate, type);
        return Result.success(list);
    }

    /**
     * 健康数据录入统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      统计类型：day/month
     * @param dataType  数据类型（可选）
     * @return 统计图表数据
     */
    @Operation(summary = "健康数据录入统计")
    @GetMapping("/health-data")
    public Result<List<StatisticsChartVO>> healthData(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "day") String type,
            @RequestParam(required = false) String dataType) {
        List<StatisticsChartVO> list = statisticsService.healthData(startDate, endDate, type, dataType);
        return Result.success(list);
    }

    /**
     * 预警发生统计
     *
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param type       统计类型：day/month
     * @param alertLevel 预警等级（可选）
     * @return 统计图表数据
     */
    @Operation(summary = "预警发生统计")
    @GetMapping("/alert")
    public Result<List<StatisticsChartVO>> alert(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "day") String type,
            @RequestParam(required = false) Integer alertLevel) {
        List<StatisticsChartVO> list = statisticsService.alert(startDate, endDate, type, alertLevel);
        return Result.success(list);
    }

    /**
     * 健康数据类型分布占比
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分布占比数据
     */
    @Operation(summary = "健康数据类型分布占比")
    @GetMapping("/health-data-type-distribution")
    public Result<List<DistributionVO>> healthDataTypeDistribution(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<DistributionVO> list = statisticsService.healthDataTypeDistribution(startDate, endDate);
        return Result.success(list);
    }
}
