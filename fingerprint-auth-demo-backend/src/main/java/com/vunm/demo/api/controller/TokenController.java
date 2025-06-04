package com.vunm.demo.api.controller;

import com.vunm.demo.api.dto.AppTokenRequest;
import com.vunm.demo.domain.model.AppToken;
import com.vunm.demo.domain.service.TokenService;
import com.vunm.demo.util.IpAddressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TokenController {
    private final TokenService tokenService;
    private final IpAddressUtil ipAddressUtil;

    @PostMapping("/app-token")
    public ResponseEntity<AppToken> getToken(
            @RequestBody AppTokenRequest request,
            HttpServletRequest servletRequest) {
        
        String clientIp = ipAddressUtil.getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader("User-Agent");

        log.debug("Processing token request - IP: {}, User-Agent: {}", clientIp, userAgent);

        return tokenService.generateTokenIfValid(
                request,
                clientIp,
                userAgent
            )
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.badRequest().build());
    }
} 