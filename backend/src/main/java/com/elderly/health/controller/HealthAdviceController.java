package com.elderly.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.HealthAdviceAddDTO;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.HealthAdviceService;
import com.elderly.health.vo.HealthAdviceManageVO;
import com.elderly.health.vo.HealthAdviceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 健康建议控制器
 *
 * @author elderly-health
 */
@Tag(name = "健康建议管理", description = "健康建议发布、查询接口")
@RestController
@RequestMapping("/api/health-advice")
@RequiredArgsConstructor
public class HealthAdviceController {

    private final HealthAdviceService healthAdviceService;

    /**
     * 发布健康建议（医护人员）
     * 创建 health_advice 记录，若有干预方案则批量创建 intervention_plan 记录
     *
     * @param dto 健康建议DTO
     * @return 操作结果
     */
    @Operation(summary = "发布健康建议")
    @OperationLog(value = "发布健康建议", type = 2)
    @PostMapping
    @RequiresRole("DOCTOR")
    public Result<Void> add(@Valid @RequestBody HealthAdviceAddDTO dto) {
        healthAdviceService.add(dto);
        return Result.success();
    }

    /**
     * 老年用户/家属查看健康建议列表
     * 老年用户：查询发给自己的建议
     * 家属：参数 elderlyId，校验关联后查询该老人的建议
     *
     * @param elderlyId 老人ID（家属查看时需传）
     * @return 健康建议VO列表（含建议信息 + 干预方案列表 + 发布医生姓名）
     */
    @Operation(summary = "查看我的健康建议")
    @GetMapping("/mine")
    public Result<List<HealthAdviceVO>> mine(@RequestParam(required = false) Long elderlyId) {
        List<HealthAdviceVO> list = healthAdviceService.listMine(elderlyId);
        return Result.success(list);
    }

    /**
     * 医护人员分页查询自己发布的健康建议
     *
     * @param pageNum  当前页（默认1）
     * @param pageSize 每页大小（默认10）
     * @param userId   老人ID筛选（可选）
     * @param keyword  关键词筛选（可选）
     * @return 健康建议管理VO分页
     */
    @Operation(summary = "分页查询健康建议")
    @GetMapping("/page")
    @RequiresRole("DOCTOR")
    public Result<IPage<HealthAdviceManageVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword) {
        IPage<HealthAdviceManageVO> page = healthAdviceService.page(pageNum, pageSize, userId, keyword);
        return Result.success(page);
    }

    /**
     * 查询健康建议详情（含干预方案列表）
     *
     * @param id 建议ID
     * @return 健康建议详情
     */
    @Operation(summary = "查询健康建议详情")
    @GetMapping("/{id}")
    public Result<HealthAdviceVO> getById(@PathVariable Long id) {
        HealthAdviceVO vo = healthAdviceService.getById(id);
        return Result.success(vo);
    }
}
