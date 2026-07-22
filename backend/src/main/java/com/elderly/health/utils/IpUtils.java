package com.elderly.health.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * IP 工具类
 * 用于获取客户端真实 IP 地址（支持代理）
 *
 * @author elderly-health
 */
@Slf4j
public class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    private IpUtils() {
    }

    /**
     * 获取客户端 IP 地址
     * 支持经过代理（Nginx、Apache 等）转发的请求
     *
     * @param request HttpServletRequest
     * @return 客户端 IP 地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return UNKNOWN;
        }

        String ip = request.getHeader("X-Forwarded-For");
        ip = resolveIp(ip, request, "Proxy-Client-IP");
        ip = resolveIp(ip, request, "WL-Proxy-Client-IP");
        ip = resolveIp(ip, request, "HTTP_CLIENT_IP");
        ip = resolveIp(ip, request, "HTTP_X_FORWARDED_FOR");

        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理本地回环地址
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        // 对于通过多个代理转发的请求，取第一个非 unknown 的 IP
        if (ip != null && ip.contains(",")) {
            String[] ips = ip.split(",");
            for (String s : ips) {
                if (!isUnknown(s.trim())) {
                    ip = s.trim();
                    break;
                }
            }
        }

        return ip;
    }

    /**
     * 解析 IP 地址
     * 若当前 IP 为空或 unknown，则从指定 Header 中获取
     *
     * @param ip           当前 IP
     * @param request      HttpServletRequest
     * @param headerName   Header 名称
     * @return 解析后的 IP
     */
    private static String resolveIp(String ip, HttpServletRequest request, String headerName) {
        if (isUnknown(ip)) {
            ip = request.getHeader(headerName);
        }
        return ip;
    }

    /**
     * 判断 IP 是否为空或 unknown
     *
     * @param ip IP 地址
     * @return 是否为空或 unknown
     */
    private static boolean isUnknown(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 根据 IP 地址获取地理位置信息
     * 内网/本机IP返回"内网/本机"，外网IP调用在线API查询
     *
     * @param ip IP 地址
     * @return 地理位置（如"中国-广西-南宁"），查询失败返回"未知"
     */
    public static String getIpLocation(String ip) {
        if (isUnknown(ip) || StrUtil.isBlank(ip)) {
            return "未知";
        }

        // 本机/内网IP
        if (LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
            return "内网/本机";
        }

        // 内网网段判断
        if (isInternalIp(ip)) {
            return "内网/局域网";
        }

        // 外网IP调用在线API查询
        try {
            String url = "http://ip-api.com/json/" + ip + "?lang=zh-CN&fields=country,regionName,city,isp";
            String response = HttpUtil.get(url, 3000);
            JSONObject json = JSONUtil.parseObj(response);

            if (json.getInt("status", -1) != null && json.getInt("status") == 1) {
                // success
                String country = json.getStr("country");
                String region = json.getStr("regionName");
                String city = json.getStr("city");
                String isp = json.getStr("isp");

                StringBuilder sb = new StringBuilder();
                if (StrUtil.isNotBlank(country)) {
                    sb.append(country);
                }
                if (StrUtil.isNotBlank(region)) {
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(region);
                }
                if (StrUtil.isNotBlank(city) && !city.equals(region)) {
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(city);
                }
                if (StrUtil.isNotBlank(isp)) {
                    if (sb.length() > 0) {
                        sb.append(" (").append(isp).append(")");
                    }
                }
                return sb.length() > 0 ? sb.toString() : "未知";
            }
        } catch (Exception e) {
            log.warn("查询IP地理位置失败，IP={}，原因：{}", ip, e.getMessage());
        }
        return "未知";
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip IP 地址
     * @return 是否为内网IP
     */
    private static boolean isInternalIp(String ip) {
        if (ip == null) {
            return false;
        }
        // 10.0.0.0/8
        if (ip.startsWith("10.")) {
            return true;
        }
        // 172.16.0.0/12
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        // 192.168.0.0/16
        if (ip.startsWith("192.168.")) {
            return true;
        }
        // 169.254.0.0/16
        if (ip.startsWith("169.254.")) {
            return true;
        }
        return false;
    }
}
