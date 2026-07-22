package com.elderly.health.service;

import com.elderly.health.dto.ChangePasswordDTO;
import com.elderly.health.dto.UpdateProfileDTO;
import com.elderly.health.vo.UserInfoVO;

/**
 * 个人信息服务接口
 *
 * @author elderly-health
 */
public interface ProfileService {

    /**
     * 获取当前用户个人信息
     *
     * @return 用户信息
     */
    UserInfoVO getCurrentUserInfo();

    /**
     * 更新当前用户个人信息
     *
     * @param dto 更新请求
     */
    void updateProfile(UpdateProfileDTO dto);

    /**
     * 修改密码
     *
     * @param dto 修改密码请求
     */
    void changePassword(ChangePasswordDTO dto);
}
