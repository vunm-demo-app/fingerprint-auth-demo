package com.vunm.demo.api.controller;

import com.vunm.demo.domain.model.AppToken;
import com.vunm.demo.api.dto.AppTokenRequestWithComponents;
import com.vunm.demo.domain.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TokenController {
    private final TokenService tokenService;

    @PostMapping("/app-token")
    public ResponseEntity<AppToken> getToken(
            @RequestBody AppTokenRequestWithComponents request,
            HttpServletRequest servletRequest) {
        
        String clientIp = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");

        return tokenService.generateTokenIfValid(
                request,
                clientIp,
                userAgent,
                request.getComponents()
            )
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.badRequest().build());
    }
} 