package com.elderly.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.InterventionFeedbackAddDTO;
import com.elderly.health.dto.InterventionPlanUpdateDTO;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.InterventionPlanService;
import com.elderly.health.vo.InterventionFeedbackVO;
import com.elderly.health.vo.InterventionPlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 干预方案管理控制器
 *
 * @author elderly-health
 */
@Tag(name = "干预方案管理", description = "干预方案管理、执行反馈接口")
@RestController
@RequestMapping("/api/intervention-plan")
@RequiredArgsConstructor
public class InterventionPlanController {

    private final InterventionPlanService interventionPlanService;

    /**
     * 医护人员分页查询干预方案
     *
     * @param pageNum  当前页（默认1）
     * @param pageSize 每页大小（默认10）
     * @param userId   老人ID筛选（可选）
     * @param status   状态筛选（可选）
     * @return 干预方案VO分页（含方案信息 + 老人姓名 + 建议标题 + 反馈列表）
     */
    @Operation(summary = "分页查询干预方案")
    @GetMapping("/page")
    @RequiresRole("DOCTOR")
    public Result<IPage<InterventionPlanVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        IPage<InterventionPlanVO> page = interventionPlanService.page(pageNum, pageSize, userId, status);
        return Result.success(page);
    }

    /**
     * 更新干预方案内容
     *
     * @param dto 干预方案更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新干预方案")
    @OperationLog(value = "更新干预方案", type = 3)
    @PutMapping
    @RequiresRole("DOCTOR")
    public Result<Void> update(@Valid @RequestBody InterventionPlanUpdateDTO dto) {
        interventionPlanService.update(dto);
        return Result.success();
    }

    /**
     * 终止干预方案（status=0已终止）
     *
     * @param id 方案ID
     * @return 操作结果
     */
    @Operation(summary = "终止干预方案")
    @OperationLog(value = "终止干预方案", type = 3)
    @PutMapping("/terminate/{id}")
    @RequiresRole("DOCTOR")
    public Result<Void> terminate(@PathVariable Long id) {
        interventionPlanService.terminate(id);
        return Result.success();
    }

    /**
     * 查询指定方案的所有执行反馈列表
     *
     * @param id 方案ID
     * @return 反馈列表
     */
    @Operation(summary = "查询方案执行反馈列表")
    @GetMapping("/{id}/feedbacks")
    @RequiresRole("DOCTOR")
    public Result<List<InterventionFeedbackVO>> feedbacks(@PathVariable Long id) {
        List<InterventionFeedbackVO> list = interventionPlanService.listFeedbacks(id);
        return Result.success(list);
    }

    /**
     * 老年用户/家属查看进行中的干预方案
     * 老年用户：查询自己的进行中方案
     * 家属：参数 elderlyId，校验关联后查询该老人的方案
     *
     * @param elderlyId 老人ID（家属查看时需传）
     * @return 干预方案VO列表
     */
    @Operation(summary = "查看我的干预方案")
    @GetMapping("/mine")
    public Result<List<InterventionPlanVO>> mine(@RequestParam(required = false) Long elderlyId) {
        List<InterventionPlanVO> list = interventionPlanService.listMine(elderlyId);
        return Result.success(list);
    }

    /**
     * 新增干预方案执行反馈
     * 校验方案存在且进行中，创建反馈记录，并通知相关医护（简化为记录日志）
     *
     * @param dto 反馈DTO
     * @return 操作结果
     */
    @Operation(summary = "新增干预方案执行反馈")
    @OperationLog(value = "新增干预方案执行反馈", type = 2)
    @PostMapping("/feedback")
    public Result<Void> addFeedback(@Valid @RequestBody InterventionFeedbackAddDTO dto) {
        interventionPlanService.addFeedback(dto);
        return Result.success();
    }
}
