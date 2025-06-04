package com.vunm.demo.domain.service;

import com.vunm.demo.api.dto.AppTokenRequest;
import com.vunm.demo.api.dto.AppTokenResponse;
import com.vunm.demo.domain.exception.BotDetectedException;
import com.vunm.demo.domain.model.VisitorInfo;
import com.vunm.demo.domain.service.jwt.JwtService;
import com.vunm.demo.application.port.in.GetVisitorInfoUseCase;
import com.vunm.demo.application.port.in.VerifyVisitorUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FingerprintVerificationService implements VerifyVisitorUseCase {
    private final GetVisitorInfoUseCase visitorService;
    private final JwtService jwtService;

    @Override
    public AppTokenResponse verifyVisitor(AppTokenRequest request) {
        log.info("Verifying visitor: {}, requestId: {}", request.getVisitorId(), request.getRequestId());
        
        // Get visitor info using VisitorService
        VisitorInfo visitorInfo = visitorService.getVisitorInfo(
            request.getVisitorId(), 
            null,  // ipAddress is not available in AppTokenRequest
            request.getRequestId()  // Pass requestId to getVisitorInfo
        );
        
        // Check bot detection results
        if (visitorInfo.getBotProbability() > 0.5) {
            log.warn("Bot detected for visitor {} with probability {} and type {}", 
                request.getVisitorId(), 
                visitorInfo.getBotProbability(), 
                visitorInfo.getBotType());
            throw new BotDetectedException("Bot detected");
        }
        
        // Generate JWT token
        String token = jwtService.generateToken(request.getVisitorId());
        
        return AppTokenResponse.builder()
                .token(token)
                .visitorId(request.getVisitorId())
                .requestId(request.getRequestId())  // Include requestId in response
                .build();
    }
}