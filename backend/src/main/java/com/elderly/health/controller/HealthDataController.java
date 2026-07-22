package com.elderly.health.controller;

import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.HealthDataAddDTO;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.HealthDataService;
import com.elderly.health.vo.HealthDataStatisticsVO;
import com.elderly.health.vo.HealthDataTrendVO;
import com.elderly.health.vo.HealthDataTypeVO;
import com.elderly.health.vo.HealthDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康数据录入与查询控制器
 *
 * @author elderly-health
 */
@Tag(name = "健康数据管理", description = "健康数据录入、查询、统计接口")
@RestController
@RequestMapping("/api/health-data")
@RequiredArgsConstructor
public class HealthDataController {

    private final HealthDataService healthDataService;

    /**
     * 老年用户录入健康数据
     *
     * @param dto 健康数据DTO
     * @return 操作结果
     */
    @Operation(summary = "老年用户录入健康数据")
    @OperationLog(value = "录入健康数据", type = 2)
    @PostMapping
    @RequiresRole("ELDERLY")
    public Result<Void> add(@Valid @RequestBody HealthDataAddDTO dto) {
        healthDataService.addByElderly(dto);
        return Result.success();
    }

    /**
     * 家属代录入健康数据
     *
     * @param elderlyId 老人ID
     * @param dto       健康数据DTO
     * @return 操作结果
     */
    @Operation(summary = "家属代录入健康数据")
    @OperationLog(value = "家属代录入健康数据", type = 2)
    @PostMapping("/family/{elderlyId}")
    @RequiresRole("FAMILY")
    public Result<Void> addByFamily(@PathVariable Long elderlyId, @Valid @RequestBody HealthDataAddDTO dto) {
        healthDataService.addByFamily(elderlyId, dto);
        return Result.success();
    }

    /**
     * 查询健康数据列表
     *
     * @param dataType  数据类型（必填）
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param elderlyId 老人ID（家属/医护查看时需传）
     * @return 健康数据列表
     */
    @Operation(summary = "查询健康数据列表")
    @GetMapping("/list")
    public Result<List<HealthDataVO>> list(
            @RequestParam String dataType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) Long elderlyId) {
        List<HealthDataVO> list = healthDataService.list(dataType, startTime, endTime, elderlyId);
        return Result.success(list);
    }

    /**
     * 查询健康数据趋势
     *
     * @param dataType  数据类型
     * @param days      天数（默认30）
     * @param elderlyId 老人ID
     * @return 趋势数据列表
     */
    @Operation(summary = "查询健康数据趋势")
    @GetMapping("/trend")
    public Result<List<HealthDataTrendVO>> trend(
            @RequestParam String dataType,
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(required = false) Long elderlyId) {
        List<HealthDataTrendVO> list = healthDataService.trend(dataType, days, elderlyId);
        return Result.success(list);
    }

    /**
     * 健康数据统计摘要
     *
     * @param dataType  数据类型
     * @param elderlyId 老人ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计摘要
     */
    @Operation(summary = "健康数据统计摘要")
    @GetMapping("/statistics")
    @RequiresRole("DOCTOR")
    public Result<HealthDataStatisticsVO> statistics(
            @RequestParam String dataType,
            @RequestParam Long elderlyId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        HealthDataStatisticsVO vo = healthDataService.statistics(dataType, elderlyId, startTime, endTime);
        return Result.success(vo);
    }

    /**
     * 查询所有健康数据类型
     *
     * @return 数据类型列表
     */
    @Operation(summary = "查询所有健康数据类型")
    @GetMapping("/types")
    public Result<List<HealthDataTypeVO>> types() {
        List<HealthDataTypeVO> list = healthDataService.types();
        return Result.success(list);
    }
}
