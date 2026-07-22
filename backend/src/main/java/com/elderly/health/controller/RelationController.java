package com.elderly.health.controller;

import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.RelationAdminLinkDTO;
import com.elderly.health.dto.RelationApplyDTO;
import com.elderly.health.dto.RelationInviteDTO;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.RelationService;
import com.elderly.health.vo.RelationVO;
import com.elderly.health.vo.SimpleUserVO;
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
 * 家属老人关联管理控制器
 *
 * @author elderly-health
 */
@Tag(name = "家属老人关联管理", description = "家属与老人关联关系管理接口")
@RestController
@RequestMapping("/api/relation")
@RequiredArgsConstructor
public class RelationController {

    private final RelationService relationService;

    /**
     * 老人邀请家属
     *
     * @param dto 邀请DTO
     * @return 操作结果
     */
    @Operation(summary = "老人邀请家属")
    @OperationLog(value = "老人邀请家属", type = 2)
    @PostMapping("/invite")
    public Result<Void> invite(@Valid @RequestBody RelationInviteDTO dto) {
        relationService.invite(dto);
        return Result.success();
    }

    /**
     * 家属申请关联老人
     *
     * @param dto 申请DTO
     * @return 操作结果
     */
    @Operation(summary = "家属申请关联老人")
    @OperationLog(value = "家属申请关联老人", type = 2)
    @PostMapping("/apply")
    public Result<Void> apply(@Valid @RequestBody RelationApplyDTO dto) {
        relationService.apply(dto);
        return Result.success();
    }

    /**
     * 管理员/医护代关联
     *
     * @param dto 代关联DTO
     * @return 操作结果
     */
    @Operation(summary = "管理员/医护代关联")
    @OperationLog(value = "管理员/医护代关联", type = 2)
    @PostMapping("/admin-link")
    @RequiresRole({"ADMIN", "DOCTOR"})
    public Result<Void> adminLink(@Valid @RequestBody RelationAdminLinkDTO dto) {
        relationService.adminLink(dto);
        return Result.success();
    }

    /**
     * 确认关联
     *
     * @param id 关联ID
     * @return 操作结果
     */
    @Operation(summary = "确认关联")
    @OperationLog(value = "确认关联", type = 3)
    @PostMapping("/confirm/{id}")
    public Result<Void> confirm(@PathVariable Long id) {
        relationService.confirm(id);
        return Result.success();
    }

    /**
     * 拒绝关联
     *
     * @param id 关联ID
     * @return 操作结果
     */
    @Operation(summary = "拒绝关联")
    @OperationLog(value = "拒绝关联", type = 3)
    @PostMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id) {
        relationService.reject(id);
        return Result.success();
    }

    /**
     * 查询关联关系列表
     *
     * @param status 状态筛选（可选）
     * @return 关联VO列表
     */
    @Operation(summary = "查询关联关系列表")
    @GetMapping("/list")
    public Result<List<RelationVO>> list(@RequestParam(required = false) Integer status) {
        List<RelationVO> list = relationService.list(status);
        return Result.success(list);
    }

    /**
     * 家属查询关联老人列表
     *
     * @return 老人简单信息列表
     */
    @Operation(summary = "家属查询关联老人列表")
    @GetMapping("/my-elderly")
    public Result<List<SimpleUserVO>> myElderly() {
        List<SimpleUserVO> list = relationService.myElderly();
        return Result.success(list);
    }

    /**
     * 老人查询关联家属列表
     *
     * @return 家属简单信息列表
     */
    @Operation(summary = "老人查询关联家属列表")
    @GetMapping("/my-family")
    public Result<List<SimpleUserVO>> myFamily() {
        List<SimpleUserVO> list = relationService.myFamily();
        return Result.success(list);
    }
}
