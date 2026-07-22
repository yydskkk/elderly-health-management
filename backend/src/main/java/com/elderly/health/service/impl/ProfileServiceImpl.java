package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.elderly.health.dto.ChangePasswordDTO;
import com.elderly.health.dto.UpdateProfileDTO;
import com.elderly.health.entity.SysUser;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.service.ProfileService;
import com.elderly.health.service.SysUserService;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 个人信息服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserInfoVO getCurrentUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        return sysUserService.buildUserInfoVO(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UpdateProfileDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();

        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId);

        if (dto.getName() != null) {
            wrapper.set(SysUser::getName, dto.getName());
        }
        if (dto.getGender() != null) {
            wrapper.set(SysUser::getGender, dto.getGender());
        }
        if (dto.getAge() != null) {
            wrapper.set(SysUser::getAge, dto.getAge());
        }
        if (dto.getPhone() != null) {
            wrapper.set(SysUser::getPhone, dto.getPhone());
        }
        if (dto.getAvatar() != null) {
            wrapper.set(SysUser::getAvatar, dto.getAvatar());
        }

        sysUserService.update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDTO dto) {
        // 1. 获取当前用户
        Long userId = SecurityUtils.getCurrentUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. BCrypt 校验旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 3. 校验 newPassword 与 confirmPassword 一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 4. BCrypt 加密新密码
        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());

        // 5. 更新密码
        sysUserService.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, userId)
                        .set(SysUser::getPassword, encodedPassword)
        );
    }
}
