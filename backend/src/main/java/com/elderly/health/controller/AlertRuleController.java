package com.elderly.health.controller;

import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.AlertRuleAddDTO;
import com.elderly.health.dto.AlertRuleUpdateDTO;
import com.elderly.health.dto.PersonalAlertRuleAddDTO;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.AlertRuleService;
import com.elderly.health.vo.AlertRuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * 预警规则管理控制器
 *
 * @author elderly-health
 */
@Tag(name = "预警规则管理", description = "全局预警规则与个性化预警阈值管理接口")
@RestController
@RequestMapping("/api/alert-rule")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    /**
     * 查询所有全局预警规则
     *
     * @return 规则列表
     */
    @Operation(summary = "查询所有全局预警规则")
    @GetMapping("/list")
    @RequiresRole("ADMIN")
    public Result<List<AlertRuleVO>> list() {
        List<AlertRuleVO> list = alertRuleService.listAll();
        return Result.success(list);
    }

    /**
     * 创建全局预警规则
     *
     * @param dto 规则新增DTO
     * @return 操作结果
     */
    @Operation(summary = "创建全局预警规则")
    @OperationLog(value = "创建全局预警规则", type = 2)
    @PostMapping
    @RequiresRole("ADMIN")
    public Result<Void> create(@Valid @RequestBody AlertRuleAddDTO dto) {
        alertRuleService.create(dto);
        return Result.success();
    }

    /**
     * 更新全局预警规则
     *
     * @param dto 规则更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新全局预警规则")
    @OperationLog(value = "更新全局预警规则", type = 3)
    @PutMapping
    @RequiresRole("ADMIN")
    public Result<Void> update(@Valid @RequestBody AlertRuleUpdateDTO dto) {
        alertRuleService.update(dto);
        return Result.success();
    }

    /**
     * 启用/禁用规则
     *
     * @param id     规则ID
     * @param status 状态：0禁用1启用
     * @return 操作结果
     */
    @Operation(summary = "启用/禁用规则")
    @OperationLog(value = "启用/禁用预警规则", type = 3)
    @PutMapping("/status/{id}")
    @RequiresRole("ADMIN")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        alertRuleService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除规则
     *
     * @param id 规则ID
     * @return 操作结果
     */
    @Operation(summary = "删除全局预警规则")
    @OperationLog(value = "删除全局预警规则", type = 4)
    @DeleteMapping("/{id}")
    @RequiresRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        alertRuleService.delete(id);
        return Result.success();
    }

    /**
     * 查询指定老人的个性化预警规则（如无个性化规则则返回全局规则）
     *
     * @param userId 老人用户ID
     * @return 规则列表
     */
    @Operation(summary = "查询老人个性化预警规则")
    @GetMapping("/personal/{userId}")
    @RequiresRole("DOCTOR")
    public Result<List<AlertRuleVO>> listPersonal(@PathVariable Long userId) {
        List<AlertRuleVO> list = alertRuleService.listPersonal(userId);
        return Result.success(list);
    }

    /**
     * 创建或更新个性化预警规则（同一用户同一类型只允许一条）
     *
     * @param dto 个性化规则DTO
     * @return 操作结果
     */
    @Operation(summary = "创建或更新个性化预警规则")
    @OperationLog(value = "保存个性化预警规则", type = 2)
    @PostMapping("/personal")
    @RequiresRole("DOCTOR")
    public Result<Void> savePersonal(@Valid @RequestBody PersonalAlertRuleAddDTO dto) {
        alertRuleService.savePersonal(dto);
        return Result.success();
    }

    /**
     * 删除个性化规则（恢复使用全局规则）
     *
     * @param id 个性化规则ID
     * @return 操作结果
     */
    @Operation(summary = "删除个性化预警规则")
    @OperationLog(value = "删除个性化预警规则", type = 4)
    @DeleteMapping("/personal/{id}")
    @RequiresRole("DOCTOR")
    public Result<Void> deletePersonal(@PathVariable Long id) {
        alertRuleService.deletePersonal(id);
        return Result.success();
    }
}
