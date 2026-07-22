package com.elderly.health.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.OperationLogService;
import com.elderly.health.vo.OperationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 系统日志管理控制器
 *
 * @author elderly-health
 */
@Slf4j
@Tag(name = "系统日志管理", description = "操作日志查询与导出接口")
@RestController
@RequestMapping("/api/log")
@RequiresRole("ADMIN")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     *
     * @param pageNum    当前页（默认1）
     * @param pageSize   每页大小（默认10）
     * @param username   用户名筛选
     * @param operation  操作内容筛选
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 操作日志分页
     */
    @Operation(summary = "分页查询操作日志")
    @GetMapping("/page")
    public Result<IPage<OperationLogVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {
        LocalDateTime startDateTime = startTime != null ? startTime.atStartOfDay() : null;
        LocalDateTime endDateTime = endTime != null ? endTime.plusDays(1).atStartOfDay() : null;
        IPage<OperationLogVO> page = operationLogService.page(pageNum, pageSize, username, operation,
                startDateTime, endDateTime);
        return Result.success(page);
    }

    /**
     * 导出操作日志为 Excel
     *
     * @param username   用户名筛选
     * @param operation  操作内容筛选
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param response   HTTP 响应
     */
    @Operation(summary = "导出操作日志")
    @OperationLog(value = "导出操作日志", type = 5)
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime,
            HttpServletResponse response) throws IOException {
        LocalDateTime startDateTime = startTime != null ? startTime.atStartOfDay() : null;
        LocalDateTime endDateTime = endTime != null ? endTime.plusDays(1).atStartOfDay() : null;
        List<OperationLogVO> list = operationLogService.list(username, operation, startDateTime, endDateTime);

        // 设置响应头
        String fileName = URLEncoder.encode("操作日志.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        // 使用 Hutool ExcelWriter 写入数据
        ExcelWriter writer = ExcelUtil.getWriter(true);
        try {
            // 表头
            writer.addHeaderAlias("id", "日志ID");
            writer.addHeaderAlias("username", "用户名");
            writer.addHeaderAlias("operation", "操作内容");
            writer.addHeaderAlias("method", "请求方法");
            writer.addHeaderAlias("ip", "IP地址");
            writer.addHeaderAlias("location", "操作地点");
            writer.addHeaderAlias("time", "耗时(ms)");
            writer.addHeaderAlias("createTime", "创建时间");

            // 只输出别名的字段
            writer.setOnlyAlias(true);

            // 写入数据
            writer.write(list, true);

            // 输出到响应流
            OutputStream out = response.getOutputStream();
            writer.flush(out, true);
        } finally {
            writer.close();
        }
    }
}
