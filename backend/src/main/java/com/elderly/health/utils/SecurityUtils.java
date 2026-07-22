package com.elderly.health.utils;

import com.elderly.health.common.ResultCode;
import com.elderly.health.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Map;

/**
 * 安全工具类，用于获取当前登录用户信息
 *
 * @author elderly-health
 */
@Slf4j
public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前 Authentication
     *
     * @return Authentication
     */
    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Number) {
            return ((Number) principal).longValue();
        }
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                log.warn("无法解析用户ID：{}", principal);
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }
        }
        log.warn("无法识别的 principal 类型：{}", principal);
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    /**
     * 获取当前登录用户邮箱
     *
     * @return 邮箱
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> map) {
            Object email = map.get("email");
            return email != null ? email.toString() : null;
        }
        return null;
    }

    /**
     * 获取当前登录用户角色
     *
     * @return 角色编码
     */
    public static String getCurrentUserRole() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        // 优先从 details 中获取
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> map) {
            Object roleCode = map.get("roleCode");
            if (roleCode != null) {
                return roleCode.toString();
            }
        }
        // 从 authorities 中获取 ROLE_ 前缀的角色
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            if (auth.startsWith("ROLE_")) {
                return auth.substring(5);
            }
        }
        return null;
    }

    /**
     * 判断是否已登录
     *
     * @return true-已登录 false-未登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
