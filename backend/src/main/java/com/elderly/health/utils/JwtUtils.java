package com.elderly.health.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 工具类
 *
 * @author elderly-health
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * JWT 密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT 过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * JWT Header 名称
     */
    @Value("${jwt.header}")
    private String header;

    /**
     * JWT Token 前缀
     */
    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    /**
     * 签名密钥
     */
    private SecretKey signingKey;

    /**
     * 初始化签名密钥
     */
    @PostConstruct
    public void init() {
        try {
            // 尝试将 secret 作为 Base64 解码
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            // 如果不是合法的 Base64，则直接使用 UTF-8 字节
            log.warn("JWT secret 不是合法的 Base64 字符串，使用原始字符串作为密钥");
            signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param email    邮箱
     * @param roleCode 角色编码
     * @return JWT Token
     */
    public String generateToken(Long userId, String email, String roleCode) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("roleCode", roleCode)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * 解析 Token 返回 Claims
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 提取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }

    /**
     * 从 Token 提取邮箱
     *
     * @param token JWT Token
     * @return 邮箱
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    /**
     * 从 Token 提取角色编码
     *
     * @param token JWT Token
     * @return 角色编码
     */
    public String getRoleCodeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("roleCode", String.class);
    }

    /**
     * 验证 Token 有效性
     *
     * @param token JWT Token
     * @return true-有效 false-无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT Token 验证失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断 Token 是否过期
     *
     * @param token JWT Token
     * @return true-已过期 false-未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * 获取 JWT Header 名称
     *
     * @return Header 名称
     */
    public String getHeader() {
        return header;
    }

    /**
     * 获取 JWT Token 前缀
     *
     * @return Token 前缀
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /**
     * 获取 JWT 过期时间
     *
     * @return 过期时间（毫秒）
     */
    public long getExpiration() {
        return expiration;
    }
}
