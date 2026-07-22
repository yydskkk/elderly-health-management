package com.elderly.health.service;

import com.elderly.health.dto.AlertRuleAddDTO;
import com.elderly.health.dto.AlertRuleUpdateDTO;
import com.elderly.health.dto.PersonalAlertRuleAddDTO;
import com.elderly.health.vo.AlertRuleVO;

import java.util.List;

/**
 * 预警规则服务接口
 *
 * @author elderly-health
 */
public interface AlertRuleService {

    /**
     * 查询所有全局预警规则
     *
     * @return 规则列表
     */
    List<AlertRuleVO> listAll();

    /**
     * 创建全局预警规则
     *
     * @param dto 规则新增DTO
     */
    void create(AlertRuleAddDTO dto);

    /**
     * 更新全局预警规则
     *
     * @param dto 规则更新DTO
     */
    void update(AlertRuleUpdateDTO dto);

    /**
     * 更新规则状态
     *
     * @param id     规则ID
     * @param status 状态：0禁用1启用
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除全局预警规则
     *
     * @param id 规则ID
     */
    void delete(Long id);

    /**
     * 查询指定老人的个性化预警规则（如无个性化规则则返回全局规则）
     *
     * @param userId 老人用户ID
     * @return 规则列表
     */
    List<AlertRuleVO> listPersonal(Long userId);

    /**
     * 创建或更新个性化预警规则（同一用户同一类型只允许一条）
     *
     * @param dto 个性化规则DTO
     */
    void savePersonal(PersonalAlertRuleAddDTO dto);

    /**
     * 删除个性化规则（恢复使用全局规则）
     *
     * @param id 个性化规则ID
     */
    void deletePersonal(Long id);
}
