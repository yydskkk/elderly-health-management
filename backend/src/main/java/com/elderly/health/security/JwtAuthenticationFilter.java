package com.elderly.health.security;

import com.elderly.health.mapper.SysUserMapper;
import com.elderly.health.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 认证过滤器
 *
 * @author elderly-health
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final SysUserMapper sysUserMapper;

    /**
     * 从请求头获取 Token 并解析认证
     *
     * @param request     请求
     * @param response    响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
                Claims claims = jwtUtils.parseToken(token);

                // 从 Token 提取用户信息
                Long userId = jwtUtils.getUserIdFromToken(token);
                String email = claims.get("email", String.class);
                String roleCode = claims.get("roleCode", String.class);

                // 构建权限列表
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (StringUtils.hasText(roleCode)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
                }

                // 从数据库加载用户权限
                if (userId != null) {
                    List<String> permissions = sysUserMapper.selectPermissionsByUserId(userId);
                    for (String permission : permissions) {
                        authorities.add(new SimpleGrantedAuthority(permission));
                    }
                }

                // 构建 Authentication
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                // 将 email 和 roleCode 存入 details
                Map<String, Object> details = new HashMap<>();
                details.put("email", email);
                details.put("roleCode", roleCode);
                authentication.setDetails(details);

                // 设置 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.warn("JWT 认证失败：{}", e.getMessage());
            // Token 无效或过期时不清除 SecurityContext，让后续过滤器处理
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头解析 Token
     *
     * @param request 请求
     * @return Token 字符串
     */
    private String resolveToken(HttpServletRequest request) {
        String header = jwtUtils.getHeader();
        String tokenPrefix = jwtUtils.getTokenPrefix();
        String bearerToken = request.getHeader(header);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix + " ")) {
            return bearerToken.substring(tokenPrefix.length() + 1).trim();
        }
        return null;
    }
}
