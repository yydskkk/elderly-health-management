package com.elderly.health.security;

import com.elderly.health.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限校验 AOP 切面
 * 处理 {@link RequiresRole} 和 {@link RequiresPermission} 注解
 *
 * @author elderly-health
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    /**
     * 校验角色
     *
     * @param joinPoint     连接点
     * @param requiresRole  角色注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(requiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole) throws Throwable {
        String[] requiredRoles = requiresRole.value();
        String currentRole = SecurityUtils.getCurrentUserRole();

        boolean hasRole = false;
        if (currentRole != null) {
            for (String required : requiredRoles) {
                if (required.equals(currentRole)) {
                    hasRole = true;
                    break;
                }
            }
        }

        if (!hasRole) {
            log.warn("角色校验失败，需要角色：{}，当前角色：{}", Arrays.toString(requiredRoles), currentRole);
            throw new AccessDeniedException("权限不足，需要角色：" + Arrays.toString(requiredRoles));
        }

        return joinPoint.proceed();
    }

    /**
     * 校验权限
     *
     * @param joinPoint           连接点
     * @param requiresPermission  权限注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        String[] requiredPermissions = requiresPermission.value();

        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        boolean hasPermission = false;
        if (authentication != null && authentication.isAuthenticated()) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Set<String> userPermissions = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            for (String required : requiredPermissions) {
                if (userPermissions.contains(required)) {
                    hasPermission = true;
                    break;
                }
            }
        }

        if (!hasPermission) {
            log.warn("权限校验失败，需要权限：{}", Arrays.toString(requiredPermissions));
            throw new AccessDeniedException("权限不足，需要权限：" + Arrays.toString(requiredPermissions));
        }

        return joinPoint.proceed();
    }
}
