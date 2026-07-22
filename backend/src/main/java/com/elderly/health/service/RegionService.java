package com.elderly.health.service;

import com.elderly.health.dto.AssignRegionDTO;
import com.elderly.health.dto.RegionAddDTO;
import com.elderly.health.dto.RegionUpdateDTO;
import com.elderly.health.vo.RegionVO;
import com.elderly.health.vo.SimpleUserVO;

import java.util.List;

/**
 * 辖区服务接口
 *
 * @author elderly-health
 */
public interface RegionService {

    /**
     * 查询所有辖区列表（含老人数、医护数）
     *
     * @return 辖区列表
     */
    List<RegionVO> list();

    /**
     * 新增辖区
     *
     * @param dto 辖区新增DTO
     */
    void add(RegionAddDTO dto);

    /**
     * 编辑辖区
     *
     * @param dto 辖区编辑DTO
     */
    void update(RegionUpdateDTO dto);

    /**
     * 启用/禁用辖区
     *
     * @param id     辖区ID
     * @param status 状态：0禁用1启用
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除辖区（校验辖区下无用户才可删除）
     *
     * @param id 辖区ID
     */
    void delete(Long id);

    /**
     * 分配用户至辖区
     *
     * @param dto 分配DTO
     */
    void assign(AssignRegionDTO dto);

    /**
     * 移除用户辖区关联
     *
     * @param userId   用户ID
     * @param regionId 辖区ID
     */
    void unassign(Long userId, Long regionId);

    /**
     * 查询指定辖区下的用户列表
     *
     * @param regionId 辖区ID
     * @param userType 用户类型：1老人2医护
     * @return 用户列表
     */
    List<SimpleUserVO> listUsersByRegion(Long regionId, Integer userType);

    /**
     * 查询当前医护负责的辖区列表
     *
     * @return 辖区列表
     */
    List<RegionVO> myRegions();
}
