package com.elderly.health.controller;

import com.elderly.health.annotation.OperationLog;
import com.elderly.health.common.Result;
import com.elderly.health.dto.AssignRegionDTO;
import com.elderly.health.dto.RegionAddDTO;
import com.elderly.health.dto.RegionUpdateDTO;
import com.elderly.health.security.RequiresRole;
import com.elderly.health.service.RegionService;
import com.elderly.health.vo.RegionVO;
import com.elderly.health.vo.SimpleUserVO;
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
 * 辖区管理控制器
 *
 * @author elderly-health
 */
@Tag(name = "辖区管理", description = "辖区管理、用户辖区分配相关接口")
@RestController
@RequestMapping("/api/region")
@RequiresRole("ADMIN")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    /**
     * 查询所有辖区列表（含老人数、医护数）
     *
     * @return 辖区列表
     */
    @Operation(summary = "辖区列表")
    @GetMapping("/list")
    public Result<List<RegionVO>> list() {
        List<RegionVO> list = regionService.list();
        return Result.success(list);
    }

    /**
     * 新增辖区
     *
     * @param dto 辖区新增DTO
     * @return 操作结果
     */
    @Operation(summary = "新增辖区")
    @OperationLog(value = "新增辖区", type = 2)
    @PostMapping
    public Result<Void> add(@Valid @RequestBody RegionAddDTO dto) {
        regionService.add(dto);
        return Result.success();
    }

    /**
     * 编辑辖区
     *
     * @param dto 辖区编辑DTO
     * @return 操作结果
     */
    @Operation(summary = "编辑辖区")
    @OperationLog(value = "编辑辖区", type = 3)
    @PutMapping
    public Result<Void> update(@Valid @RequestBody RegionUpdateDTO dto) {
        regionService.update(dto);
        return Result.success();
    }

    /**
     * 启用/禁用辖区
     *
     * @param id     辖区ID
     * @param status 状态：0禁用1启用
     * @return 操作结果
     */
    @Operation(summary = "启用/禁用辖区")
    @OperationLog(value = "启用/禁用辖区", type = 3)
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        regionService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除辖区（辖区下无用户才可删除）
     *
     * @param id 辖区ID
     * @return 操作结果
     */
    @Operation(summary = "删除辖区")
    @OperationLog(value = "删除辖区", type = 4)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        regionService.delete(id);
        return Result.success();
    }

    /**
     * 分配用户至辖区
     *
     * @param dto 分配DTO
     * @return 操作结果
     */
    @Operation(summary = "分配用户至辖区")
    @OperationLog(value = "分配用户至辖区", type = 2)
    @PostMapping("/assign")
    public Result<Void> assign(@Valid @RequestBody AssignRegionDTO dto) {
        regionService.assign(dto);
        return Result.success();
    }

    /**
     * 移除用户辖区关联
     *
     * @param dto 分配DTO（仅需userId和regionId）
     * @return 操作结果
     */
    @Operation(summary = "移除用户辖区关联")
    @OperationLog(value = "移除用户辖区关联", type = 4)
    @DeleteMapping("/assign")
    public Result<Void> unassign(@RequestBody AssignRegionDTO dto) {
        regionService.unassign(dto.getUserId(), dto.getRegionId());
        return Result.success();
    }

    /**
     * 查询指定辖区下的用户列表
     *
     * @param regionId 辖区ID
     * @param userType 用户类型：1老人2医护
     * @return 用户列表
     */
    @Operation(summary = "查询辖区用户列表")
    @GetMapping("/{regionId}/users")
    public Result<List<SimpleUserVO>> listUsersByRegion(@PathVariable Long regionId,
                                                        @RequestParam(required = false) Integer userType) {
        List<SimpleUserVO> list = regionService.listUsersByRegion(regionId, userType);
        return Result.success(list);
    }

    /**
     * 查询当前医护负责的辖区列表
     *
     * @return 辖区列表
     */
    @Operation(summary = "我的辖区列表")
    @GetMapping("/my-regions")
    @RequiresRole("DOCTOR")
    public Result<List<RegionVO>> myRegions() {
        List<RegionVO> list = regionService.myRegions();
        return Result.success(list);
    }
}
