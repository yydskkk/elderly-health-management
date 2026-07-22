package com.elderly.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.ElderlyProfileUpdateDTO;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.ElderlyProfileService;
import com.elderly.health.vo.ElderlyProfileManageVO;
import com.elderly.health.vo.ElderlyProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康档案管理控制器
 *
 * @author elderly-health
 */
@Slf4j
@Tag(name = "健康档案管理", description = "老年用户、家属、医护人员健康档案管理接口")
@RestController
@RequestMapping("/api/elderly-profile")
@RequiredArgsConstructor
public class ElderlyProfileController {

    private final ElderlyProfileService elderlyProfileService;

    /**
     * 老年用户查看自身档案
     *
     * @return 当前用户的健康档案
     */
    @Operation(summary = "老年用户查看自身档案")
    @GetMapping("/mine")
    @RequiresRole("ELDERLY")
    public Result<ElderlyProfileVO> mine() {
        ElderlyProfileVO vo = elderlyProfileService.getMine();
        return Result.success(vo);
    }

    /**
     * 家属查看关联老人档案
     *
     * @param elderlyId 老人用户ID
     * @return 老人健康档案
     */
    @Operation(summary = "家属查看关联老人档案")
    @GetMapping("/elderly/{elderlyId}")
    @RequiresRole("FAMILY")
    public Result<ElderlyProfileVO> getByElderlyId(@PathVariable Long elderlyId) {
        ElderlyProfileVO vo = elderlyProfileService.getByElderlyId(elderlyId);
        return Result.success(vo);
    }

    /**
     * 医护人员档案列表分页查询
     *
     * @param pageNum  当前页（默认1）
     * @param pageSize 每页大小（默认10）
     * @param keyword  搜索关键词（老人姓名）
     * @param regionId 辖区ID筛选
     * @return 档案管理VO分页
     */
    @Operation(summary = "医护人员档案列表分页查询")
    @GetMapping("/page")
    @RequiresRole("DOCTOR")
    public Result<IPage<ElderlyProfileManageVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long regionId) {
        IPage<ElderlyProfileManageVO> page = elderlyProfileService.pageProfiles(pageNum, pageSize, keyword, regionId);
        return Result.success(page);
    }

    /**
     * 根据档案ID查询档案详情
     *
     * @param id 档案ID
     * @return 健康档案详情
     */
    @Operation(summary = "根据档案ID查询档案详情")
    @GetMapping("/{id}")
    @RequiresRole("DOCTOR")
    public Result<ElderlyProfileVO> getById(@PathVariable Long id) {
        ElderlyProfileVO vo = elderlyProfileService.getById(id);
        return Result.success(vo);
    }

    /**
     * 更新档案信息
     *
     * @param dto 档案更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新档案信息")
    @OperationLog(value = "更新健康档案", type = 3)
    @PutMapping
    @RequiresRole("DOCTOR")
    public Result<Void> update(@Valid @RequestBody ElderlyProfileUpdateDTO dto) {
        elderlyProfileService.updateProfile(dto);
        return Result.success();
    }
}
