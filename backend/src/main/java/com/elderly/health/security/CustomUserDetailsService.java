package com.elderly.health.security;

import com.elderly.health.entity.SysRole;
import com.elderly.health.entity.SysUser;
import com.elderly.health.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 UserDetailsService 实现
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectByEmail(email);
        if (sysUser == null) {
            log.warn("用户不存在：{}", email);
            throw new UsernameNotFoundException("用户不存在：" + email);
        }

        // 检查用户状态
        if (sysUser.getStatus() != null && sysUser.getStatus() == 0) {
            log.warn("用户已被禁用：{}", email);
            throw new UsernameNotFoundException("用户已被禁用：" + email);
        }

        // 查询用户角色
        List<SysRole> roles = sysUserMapper.selectRolesByUserId(sysUser.getId());

        // 查询用户权限
        List<String> permissions = sysUserMapper.selectPermissionsByUserId(sysUser.getId());

        // 构建权限列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (SysRole role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
        }
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }

        return User.builder()
                .username(sysUser.getEmail())
                .password(sysUser.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
