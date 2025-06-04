package com.vunm.demo.domain.service;

import com.vunm.demo.api.dto.AppTokenRequest;
import com.vunm.demo.api.dto.AppTokenResponse;
import com.vunm.demo.domain.exception.BotDetectedException;
import com.vunm.demo.domain.model.AppToken;
import com.vunm.demo.domain.model.RequestLog;
import com.vunm.demo.domain.service.jwt.JwtService;
import com.vunm.demo.util.IpAddressUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TokenService {
    private final JwtService jwtService;
    private final RequestLogService requestLogService;
    private final FingerprintVerificationService fingerprintVerificationService;
    private final IpAddressUtil ipAddressUtil;
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRequestTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastFailedAttemptTime = new ConcurrentHashMap<>();
    
    @Value("${app.token.expiration:300}") // 5 minutes default
    private long tokenExpirationSeconds;

    @Value("${app.token.timestamp.tolerance:120}") // 120 seconds tolerance (2 minutes)
    private long timestampToleranceSeconds;

    @Value("${app.rate.limit.window:3600}") // 1 hour window
    private long rateLimitWindowSeconds;

    @Value("${app.rate.limit.max:100}") // max 100 requests per window
    private long maxRequestsPerWindow;

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.failed-attempt-window:3600}") // 1 hour window
    private long failedAttemptWindowSeconds;

    public TokenService(RequestLogService requestLogService,
                        FingerprintVerificationService fingerprintVerificationService,
                        IpAddressUtil ipAddressUtil,
                        JwtService jwtService) {
        this.requestLogService = requestLogService;
        this.fingerprintVerificationService = fingerprintVerificationService;
        this.ipAddressUtil = ipAddressUtil;
        this.jwtService = jwtService;
    }

    private boolean isRateLimited(String fingerprint, String clientIp) {
        if (fingerprint == null || clientIp == null) {
            log.warn("Null fingerprint or IP detected in isRateLimited");
            return false;
        }

        // Create a composite key combining fingerprint and IP
        String key = fingerprint + ":" + clientIp;
        long now = System.currentTimeMillis();
        long windowStart = now - (rateLimitWindowSeconds * 1000);

        // Clean up old entries
        lastRequestTimes.entrySet().removeIf(entry -> entry.getValue() < windowStart);
        requestCounts.entrySet().removeIf(entry -> !lastRequestTimes.containsKey(entry.getKey()));

        // Update request count for the specific fingerprint+IP combination
        int count = requestCounts.getOrDefault(key, 0) + 1;
        requestCounts.put(key, count);
        lastRequestTimes.put(key, now);

        boolean isLimited = count > maxRequestsPerWindow;
        if (isLimited) {
            log.warn("Rate limit exceeded for IP: {} with fingerprint: {} (count: {})",
                    clientIp, fingerprint, count);
        }

        return isLimited;
    }

    private boolean hasTooManyFailedAttempts(String fingerprint, String clientIp) {
        if (fingerprint == null || clientIp == null) {
            log.warn("Null fingerprint or IP detected in hasTooManyFailedAttempts");
            return false;
        }

        String key = fingerprint + ":" + clientIp;
        long now = System.currentTimeMillis();
        long windowStart = now - (failedAttemptWindowSeconds * 1000);

        lastFailedAttemptTime.entrySet().removeIf(entry -> entry.getValue() < windowStart);
        failedAttempts.entrySet().removeIf(entry -> !lastFailedAttemptTime.containsKey(entry.getKey()));

        return failedAttempts.getOrDefault(key, 0) >= maxFailedAttempts;
    }

    private void recordFailedAttempt(String fingerprint) {
        // Check for null fingerprint
        if (fingerprint == null) {
            log.warn("Null fingerprint detected in recordFailedAttempt");
            return;
        }

        long now = System.currentTimeMillis();
        int attempts = failedAttempts.getOrDefault(fingerprint, 0) + 1;
        failedAttempts.put(fingerprint, attempts);
        lastFailedAttemptTime.put(fingerprint, now);

        if (attempts >= maxFailedAttempts) {
            log.warn("Too many failed attempts for fingerprint: {} ({})", fingerprint, attempts);
        }
    }

    public Optional<AppToken> generateTokenIfValid(AppTokenRequest request,
                                                   String clientIp,
                                                   String userAgent) {
        // Handle localhost IPv6
        if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
            clientIp = "127.0.0.1";
            log.debug("Converting localhost IPv6 to IPv4: {}", clientIp);
        }

        // 0. Check failed attempts
        if (hasTooManyFailedAttempts(request.getFingerprint(), clientIp)) {
            log.warn("Too many failed attempts for fingerprint: {}, IP: {}, User-Agent: {}",
                    request.getFingerprint(), clientIp, userAgent);
            logFailedRequest(request, clientIp, userAgent, "Too many failed attempts", false);
            return Optional.empty();
        }

        try {
            // 1. Verify visitor and get token response
            AppTokenResponse tokenResponse = fingerprintVerificationService.verifyVisitor(request);
            
            // 2. Check rate limiting
            if (isRateLimited(request.getFingerprint(), clientIp)) {
                log.warn("Rate limit exceeded - IP: {}, Fingerprint: {}, User-Agent: {}, DeviceId: {}",
                        clientIp, request.getFingerprint(), userAgent, request.getVisitorId());
                logFailedRequest(request, clientIp, userAgent, "Rate limit exceeded", true);
                return Optional.empty();
            }

            // 3. Validate timestamp
            // Get current server time in Unix epoch seconds
            long now = Instant.now().getEpochSecond();

            // Check if request timestamp is within acceptable time window
            // Uses absolute difference to handle both future and past timestamps
            if (Math.abs(now - request.getTimestamp()) > timestampToleranceSeconds) {
                log.warn("Invalid timestamp - IP: {}, Request time: {}, Current time: {}, Difference: {} seconds",
                        clientIp, request.getTimestamp(), now, Math.abs(now - request.getTimestamp()));
                // Record failed attempt and mark as non-bot (false flag)
                logFailedRequest(request, clientIp, userAgent, "Invalid timestamp", false);
                // Return empty result to indicate validation failure
                return Optional.empty();
            }

            log.info("Generating token - IP: {}, Fingerprint: {}, DeviceId: {}, User-Agent: {}, Timestamp: {}",
                    clientIp, request.getFingerprint(), request.getVisitorId(), userAgent, now);

            // 4. Log successful request
            RequestLog successLog = RequestLog.builder()
                    .fingerprint(request.getFingerprint())
                    .deviceId(request.getVisitorId())
                    .ipAddress(clientIp)
                    .userAgent(userAgent)
                    .requestType("TOKEN_REQUEST")
                    .isSuccess(true)
                    .timestamp(Instant.now())
                    .isSuspectedBot(false)
                    .build();
            requestLogService.logRequest(successLog);

            // 5. Return token from response
            return Optional.of(AppToken.builder()
                    .token(tokenResponse.getToken())
                    .fingerprint(request.getFingerprint())
                    .expiresAt(now + tokenExpirationSeconds)
                    .build());

        } catch (BotDetectedException e) {
            log.warn("Bot detected for visitor {} - IP: {}, User-Agent: {}", 
                request.getVisitorId(), clientIp, userAgent);
            logFailedRequest(request, clientIp, userAgent, "Bot detected", true);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error generating token for fingerprint: {} - IP: {}, User-Agent: {}, Error: {}",
                    request.getFingerprint(), clientIp, userAgent, e.getMessage(), e);
            logFailedRequest(request, clientIp, userAgent, "Verification error", false);
            return Optional.empty();
        }
    }

    private void logFailedRequest(AppTokenRequest request, String clientIp, String userAgent, String reason, boolean isSuspectedBot) {
        RequestLog failedLog = RequestLog.builder()
                .fingerprint(request.getFingerprint())
                .deviceId(request.getVisitorId())
                .ipAddress(clientIp)
                .userAgent(userAgent)
                .requestType("TOKEN_REQUEST")
                .isSuccess(false)
                .failureReason(reason)
                .timestamp(Instant.now())
                .isSuspectedBot(isSuspectedBot)
                .build();
        requestLogService.logRequest(failedLog);
    }

    public boolean validateToken(String token, String fingerprint) {
        // Check for null fingerprint
        if (fingerprint == null) {
            log.warn("Null fingerprint detected in validateToken");
            return false;
        }

        try {
            log.debug("Validating token for fingerprint: {}", fingerprint);
            return jwtService.validateToken(token) && 
                   jwtService.getVisitorIdFromToken(token).equals(fingerprint);
        } catch (Exception e) {
            log.error("Token validation error for fingerprint: {}: {}", fingerprint, token, e);
            recordFailedAttempt(fingerprint);
            return false;
        }
    }
} 