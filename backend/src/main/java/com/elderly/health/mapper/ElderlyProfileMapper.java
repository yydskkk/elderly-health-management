package com.elderly.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderly.health.entity.ElderlyProfile;
import com.elderly.health.vo.ElderlyProfileManageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 健康档案 Mapper 接口
 *
 * @author elderly-health
 */
@Mapper
public interface ElderlyProfileMapper extends BaseMapper<ElderlyProfile> {

    /**
     * 医护人员分页查询负责辖区内的老人档案列表
     * 关联 sys_user（老人姓名）、sys_user_region（老人辖区）、sys_region（辖区名称）
     * 仅返回当前医护负责辖区（sys_user_region user_type=2）内的老人档案
     *
     * @param page     分页参数
     * @param doctorId 当前医护用户ID
     * @param keyword  搜索关键词（老人姓名）
     * @param regionId 辖区ID筛选
     * @return 档案管理VO分页
     */
    @Select("<script>" +
            "SELECT p.id, p.user_id, p.id_card, p.address, p.emergency_contact, p.emergency_phone, " +
            "p.medical_history, p.allergy_history, p.family_history, p.blood_type, p.height, p.weight, " +
            "p.create_time, p.update_time, " +
            "u.name AS elderly_name, " +
            "r.id AS region_id, " +
            "r.region_name " +
            "FROM elderly_profile p " +
            "INNER JOIN sys_user u ON p.user_id = u.id AND u.deleted = 0 " +
            "INNER JOIN sys_user_region ur ON ur.user_id = u.id AND ur.user_type = 1 AND ur.deleted = 0 " +
            "INNER JOIN sys_region r ON ur.region_id = r.id AND r.deleted = 0 " +
            "WHERE p.deleted = 0 " +
            "AND ur.region_id IN (" +
            "SELECT region_id FROM sys_user_region WHERE user_id = #{doctorId} AND user_type = 2 AND deleted = 0" +
            ") " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND u.name LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "<if test='regionId != null'>" +
            "AND ur.region_id = #{regionId} " +
            "</if>" +
            "ORDER BY p.create_time DESC" +
            "</script>")
    IPage<ElderlyProfileManageVO> selectProfilePage(IPage<ElderlyProfileManageVO> page,
                                                     @Param("doctorId") Long doctorId,
                                                     @Param("keyword") String keyword,
                                                     @Param("regionId") Long regionId);
}
