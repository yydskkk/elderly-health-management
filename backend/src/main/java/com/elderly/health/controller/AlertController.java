package com.elderly.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.AlertHandleDTO;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.AlertNotificationService;
import com.elderly.health.service.AlertRecordService;
import com.elderly.health.vo.AlertNotificationVO;
import com.elderly.health.vo.AlertRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预警检测与通知控制器
 *
 * @author elderly-health
 */
@Tag(name = "预警通知与处理", description = "预警通知查询、标记已读、预警记录处理接口")
@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertNotificationService alertNotificationService;
    private final AlertRecordService alertRecordService;

    /**
     * 分页查询预警通知
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param isRead   是否已读筛选（可选）
     * @return 通知分页
     */
    @Operation(summary = "分页查询预警通知")
    @GetMapping("/notification/page")
    public Result<IPage<AlertNotificationVO>> notificationPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer isRead) {
        IPage<AlertNotificationVO> page = alertNotificationService.page(pageNum, pageSize, isRead);
        return Result.success(page);
    }

    /**
     * 当前用户未读预警通知数量
     *
     * @return 未读数量
     */
    @Operation(summary = "未读预警通知数量")
    @GetMapping("/notification/unread-count")
    public Result<Integer> unreadCount() {
        Integer count = alertNotificationService.unreadCount();
        return Result.success(count);
    }

    /**
     * 标记单条通知为已读
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @Operation(summary = "标记单条通知为已读")
    @OperationLog(value = "标记通知已读", type = 3)
    @PutMapping("/notification/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        alertNotificationService.markRead(id);
        return Result.success();
    }

    /**
     * 标记当前用户所有未读通知为已读
     *
     * @return 操作结果
     */
    @Operation(summary = "标记所有未读通知为已读")
    @OperationLog(value = "标记所有通知已读", type = 3)
    @PutMapping("/notification/read-all")
    public Result<Void> markAllRead() {
        alertNotificationService.markAllRead();
        return Result.success();
    }

    /**
     * 分页查询预警记录（医护查询辖区内老人的预警记录）
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param status   状态筛选（0待处理1已处理）
     * @param dataType 数据类型筛选
     * @param userId   老人用户ID筛选
     * @return 预警记录分页
     */
    @Operation(summary = "分页查询预警记录")
    @GetMapping("/record/page")
    @RequiresRole("DOCTOR")
    public Result<IPage<AlertRecordVO>> recordPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) Long userId) {
        IPage<AlertRecordVO> page = alertRecordService.page(pageNum, pageSize, status, dataType, userId);
        return Result.success(page);
    }

    /**
     * 查询预警记录详情
     *
     * @param id 预警记录ID
     * @return 预警记录详情
     */
    @Operation(summary = "查询预警记录详情")
    @GetMapping("/record/{id}")
    @RequiresRole("DOCTOR")
    public Result<AlertRecordVO> recordDetail(@PathVariable Long id) {
        AlertRecordVO vo = alertRecordService.detail(id);
        return Result.success(vo);
    }

    /**
     * 处理预警记录
     *
     * @param dto 处理DTO
     * @return 操作结果
     */
    @Operation(summary = "处理预警记录")
    @OperationLog(value = "处理预警记录", type = 3)
    @PutMapping("/record/handle")
    @RequiresRole("DOCTOR")
    public Result<Void> handle(@Valid @RequestBody AlertHandleDTO dto) {
        alertRecordService.handle(dto);
        return Result.success();
    }
}
