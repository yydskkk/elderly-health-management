package com.elderly.health.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elderly.health.dto.ElderlyProfileUpdateDTO;
import com.elderly.health.entity.ElderlyProfile;
import com.elderly.health.vo.ElderlyProfileManageVO;
import com.elderly.health.vo.ElderlyProfileVO;

/**
 * 健康档案服务接口
 *
 * @author elderly-health
 */
public interface ElderlyProfileService extends IService<ElderlyProfile> {

    /**
     * 老年用户查看自身档案
     *
     * @return 当前用户的健康档案VO
     */
    ElderlyProfileVO getMine();

    /**
     * 家属查看关联老人档案
     * 校验当前家属与该老人存在已确认关联（status=1）
     *
     * @param elderlyId 老人用户ID
     * @return 老人健康档案VO
     */
    ElderlyProfileVO getByElderlyId(Long elderlyId);

    /**
     * 医护人员分页查询负责辖区内的老人档案列表
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param keyword  搜索关键词（老人姓名）
     * @param regionId 辖区ID筛选
     * @return 档案管理VO分页
     */
    IPage<ElderlyProfileManageVO> pageProfiles(Integer pageNum, Integer pageSize, String keyword, Long regionId);

    /**
     * 根据档案ID查询档案详情
     *
     * @param id 档案ID
     * @return 健康档案VO
     */
    ElderlyProfileVO getById(Long id);

    /**
     * 更新档案信息
     *
     * @param dto 档案更新DTO
     */
    void updateProfile(ElderlyProfileUpdateDTO dto);
}
