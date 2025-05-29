package com.vunm.demo.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class IpAddressUtil {
    private static final List<String> CLOUDFLARE_IP_RANGES = Arrays.asList(
        "173.245.48.0/20",
        "103.21.244.0/22",
        "103.22.200.0/22",
        "103.31.4.0/22",
        "141.101.64.0/18",
        "108.162.192.0/18",
        "190.93.240.0/20",
        "188.114.96.0/20",
        "197.234.240.0/22",
        "198.41.128.0/17",
        "162.158.0.0/15",
        "104.16.0.0/13",
        "104.24.0.0/14",
        "172.64.0.0/13",
        "131.0.72.0/22"
    );

    public String getClientIp(HttpServletRequest request) {
        // First try Cloudflare headers
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isEmpty()) {
            return cfConnectingIp;
        }

        // Then try X-Forwarded-For
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // Finally use remote address
        return request.getRemoteAddr();
    }

    public boolean isCloudflareIp(String ip) {
        // Simple check for now - can be enhanced with proper CIDR matching
        return CLOUDFLARE_IP_RANGES.stream()
            .map(range -> range.split("/")[0])
            .map(prefix -> ip.startsWith(prefix.substring(0, prefix.lastIndexOf("."))))
            .anyMatch(matches -> matches);
    }
} 