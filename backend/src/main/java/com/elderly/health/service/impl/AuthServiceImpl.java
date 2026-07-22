package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.elderly.health.common.Constants;
import com.elderly.health.dto.ForgotPasswordDTO;
import com.elderly.health.dto.LoginDTO;
import com.elderly.health.dto.RegisterDTO;
import com.elderly.health.dto.SendCodeDTO;
import com.elderly.health.entity.ElderlyProfile;
import com.elderly.health.entity.OperationLog;
import com.elderly.health.entity.SysRole;
import com.elderly.health.entity.SysUser;
import com.elderly.health.entity.SysUserRegion;
import com.elderly.health.entity.SysUserRole;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.ElderlyProfileMapper;
import com.elderly.health.mapper.SysRoleMapper;
import com.elderly.health.mapper.SysUserRegionMapper;
import com.elderly.health.mapper.SysUserRoleMapper;
import com.elderly.health.service.AuthService;
import com.elderly.health.service.OperationLogService;
import com.elderly.health.service.SysUserService;
import com.elderly.health.service.VerificationCodeService;
import com.elderly.health.utils.JwtUtils;
import com.elderly.health.utils.SecurityUtils;
import com.elderly.health.vo.LoginVO;
import com.elderly.health.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserService sysUserService;
    private final VerificationCodeService verificationCodeService;
    private final OperationLogService operationLogService;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final ElderlyProfileMapper elderlyProfileMapper;
    private final SysUserRegionMapper sysUserRegionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * ELDERLY 角色编码
     */
    private static final String ROLE_CODE_ELDERLY = "ELDERLY";

    /**
     * 默认辖区ID
     */
    private static final Long DEFAULT_REGION_ID = 1L;

    /**
     * 老人用户类型
     */
    private static final Integer USER_TYPE_ELDERLY = 1;

    /**
     * 用户启用状态
     */
    private static final Integer USER_STATUS_ENABLED = 1;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 1. 校验两次密码一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 2. 校验邮箱是否已注册
        SysUser existUser = sysUserService.selectByEmail(dto.getEmail());
        if (existUser != null) {
            throw new BusinessException("该邮箱已注册");
        }

        // 3. 校验验证码（purpose=REGISTER）
        verificationCodeService.verifyCode(dto.getEmail(), dto.getCode(),
                Constants.VERIFICATION_PURPOSE_REGISTER);

        // 4. BCrypt 加密密码
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // 5. 创建 SysUser 记录
        SysUser user = new SysUser();
        user.setEmail(dto.getEmail());
        user.setPassword(encodedPassword);
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setStatus(USER_STATUS_ENABLED);
        sysUserService.save(user);

        // 6. 根据角色码查询角色ID，插入 sys_user_role 关联
        SysRole role = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode())
        );
        if (role == null) {
            throw new BusinessException("角色不存在：" + dto.getRoleCode());
        }

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        sysUserRoleMapper.insert(userRole);

        // 7. 若角色为 ELDERLY：创建健康档案 + 分配默认辖区
        if (ROLE_CODE_ELDERLY.equals(dto.getRoleCode())) {
            // 创建 elderly_profile 基础档案
            ElderlyProfile profile = new ElderlyProfile();
            profile.setUserId(user.getId());
            elderlyProfileMapper.insert(profile);

            // 分配至默认辖区（region_id=1）
            SysUserRegion userRegion = new SysUserRegion();
            userRegion.setUserId(user.getId());
            userRegion.setRegionId(DEFAULT_REGION_ID);
            userRegion.setUserType(USER_TYPE_ELDERLY);
            sysUserRegionMapper.insert(userRegion);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO dto, String ip) {
        // 1. 根据邮箱查询用户
        SysUser user = sysUserService.selectByEmail(dto.getEmail());
        if (user == null) {
            throw new BusinessException("邮箱或密码错误");
        }

        // 2. 校验用户状态
        if (!USER_STATUS_ENABLED.equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 3. BCrypt 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("邮箱或密码错误");
        }

        // 4. 查询用户角色
        List<SysRole> roles = sysUserService.selectRolesByUserId(user.getId());
        String roleCode = null;
        if (roles != null && !roles.isEmpty()) {
            roleCode = roles.get(0).getRoleCode();
        }

        // 5. 生成 JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), roleCode);

        // 6. 更新 last_login_time 和 last_login_ip
        sysUserService.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, user.getId())
                        .set(SysUser::getLastLoginTime, LocalDateTime.now())
                        .set(SysUser::getLastLoginIp, ip)
        );

        // 7. 记录登录日志到 operation_log
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(user.getId());
            operationLog.setUsername(user.getName());
            operationLog.setOperation("用户登录");
            operationLog.setMethod("AuthController#login");
            operationLog.setIp(ip);
            operationLogService.saveLog(operationLog);
        } catch (Exception e) {
            log.warn("记录登录日志失败：{}", e.getMessage());
        }

        // 8. 构建 LoginVO 返回
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setName(user.getName());
        vo.setRoleCode(roleCode);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(ForgotPasswordDTO dto) {
        // 1. 校验两次密码一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 2. 校验邮箱是否已注册
        SysUser user = sysUserService.selectByEmail(dto.getEmail());
        if (user == null) {
            throw new BusinessException("该邮箱未注册");
        }

        // 3. 校验验证码（purpose=FORGET_PASSWORD）
        verificationCodeService.verifyCode(dto.getEmail(), dto.getCode(),
                Constants.VERIFICATION_PURPOSE_FORGET_PASSWORD);

        // 4. BCrypt 加密新密码
        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());

        // 5. 更新用户密码
        sysUserService.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, user.getId())
                        .set(SysUser::getPassword, encodedPassword)
        );
    }

    @Override
    public void sendCode(SendCodeDTO dto) {
        verificationCodeService.sendCode(dto.getEmail(), dto.getPurpose());
    }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        return sysUserService.buildUserInfoVO(userId);
    }
}
