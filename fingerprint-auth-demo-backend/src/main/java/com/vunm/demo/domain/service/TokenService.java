package com.vunm.demo.domain.service;

import com.vunm.demo.domain.model.AppToken;
import com.vunm.demo.api.dto.AppTokenRequestWithComponents;
import com.vunm.demo.domain.model.RequestLog;
import com.vunm.demo.util.IpAddressUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.vunm.demo.domain.service.FingerprintVerificationService;

@Slf4j
@Service
public class TokenService {
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
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
                       IpAddressUtil ipAddressUtil) {
        this.requestLogService = requestLogService;
        this.fingerprintVerificationService = fingerprintVerificationService;
        this.ipAddressUtil = ipAddressUtil;
    }

    private boolean isRateLimited(String fingerprint) {
        long now = System.currentTimeMillis();
        long windowStart = now - (rateLimitWindowSeconds * 1000);

        // Clean up old entries
        lastRequestTimes.entrySet().removeIf(entry -> entry.getValue() < windowStart);
        requestCounts.entrySet().removeIf(entry -> !lastRequestTimes.containsKey(entry.getKey()));

        // Update request count
        int count = requestCounts.getOrDefault(fingerprint, 0) + 1;
        requestCounts.put(fingerprint, count);
        lastRequestTimes.put(fingerprint, now);

        return count > maxRequestsPerWindow;
    }

    private boolean hasTooManyFailedAttempts(String fingerprint) {
        long now = System.currentTimeMillis();
        long windowStart = now - (failedAttemptWindowSeconds * 1000);

        // Clean up old entries
        lastFailedAttemptTime.entrySet().removeIf(entry -> entry.getValue() < windowStart);
        failedAttempts.entrySet().removeIf(entry -> !lastFailedAttemptTime.containsKey(entry.getKey()));

        return failedAttempts.getOrDefault(fingerprint, 0) >= maxFailedAttempts;
    }

    private void recordFailedAttempt(String fingerprint) {
        long now = System.currentTimeMillis();
        int attempts = failedAttempts.getOrDefault(fingerprint, 0) + 1;
        failedAttempts.put(fingerprint, attempts);
        lastFailedAttemptTime.put(fingerprint, now);
        
        if (attempts >= maxFailedAttempts) {
            log.warn("Too many failed attempts for fingerprint: {} ({})", fingerprint, attempts);
        }
    }

    public Optional<AppToken> generateTokenIfValid(AppTokenRequestWithComponents request, 
                                                 String clientIp, 
                                                 String userAgent,
                                                 Map<String, Object> fingerprintComponents) {
        log.debug("Validating token request from IP: {}, User-Agent: {}", clientIp, userAgent);

        // 0. Check failed attempts
        if (hasTooManyFailedAttempts(request.getFingerprint())) {
            log.warn("Too many failed attempts for fingerprint: {}, IP: {}, User-Agent: {}", 
                request.getFingerprint(), clientIp, userAgent);
            logFailedRequest(request, clientIp, userAgent, "Too many failed attempts", false);
            return Optional.empty();
        }

        // 1. Verify fingerprint components
        if (!fingerprintVerificationService.verifyFingerprint(request.getFingerprint(), fingerprintComponents)) {
            log.warn("Invalid fingerprint detected - IP: {}, User-Agent: {}, DeviceId: {}, Components: {}", 
                clientIp, userAgent, request.getDeviceId(), fingerprintComponents);
            logFailedRequest(request, clientIp, userAgent, "Invalid fingerprint", false);
            return Optional.empty();
        }

        // 2. Check rate limiting
        if (isRateLimited(request.getFingerprint())) {
            log.warn("Rate limit exceeded - IP: {}, Fingerprint: {}, User-Agent: {}, DeviceId: {}", 
                clientIp, request.getFingerprint(), userAgent, request.getDeviceId());
            logFailedRequest(request, clientIp, userAgent, "Rate limit exceeded", true);
            return Optional.empty();
        }

        // 3. Validate timestamp
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - request.getTimestamp()) > timestampToleranceSeconds) {
            log.warn("Invalid timestamp - IP: {}, Request time: {}, Current time: {}, Difference: {} seconds", 
                clientIp, request.getTimestamp(), now, Math.abs(now - request.getTimestamp()));
            logFailedRequest(request, clientIp, userAgent, "Invalid timestamp", false);
            return Optional.empty();
        }

        log.info("Generating token - IP: {}, Fingerprint: {}, DeviceId: {}, User-Agent: {}, Timestamp: {}", 
            clientIp, request.getFingerprint(), request.getDeviceId(), userAgent, now);

        // 4. Generate token
        long expirationTime = now + tokenExpirationSeconds;
        String token = Jwts.builder()
                .subject(request.getFingerprint())
                .expiration(Date.from(Instant.ofEpochSecond(expirationTime)))
                .issuedAt(Date.from(Instant.ofEpochSecond(now)))
                .claim("deviceId", request.getDeviceId())
                .claim("ip", clientIp)
                .claim("userAgent", userAgent)
                .signWith(key)
                .compact();

        // 5. Log successful request
        RequestLog successLog = RequestLog.builder()
            .fingerprint(request.getFingerprint())
            .deviceId(request.getDeviceId())
            .ipAddress(clientIp)
            .userAgent(userAgent)
            .requestType("TOKEN_REQUEST")
            .isSuccess(true)
            .timestamp(Instant.now())
            .isSuspectedBot(false)
            .build();
        requestLogService.logRequest(successLog);

        return Optional.of(AppToken.builder()
                .token(token)
                .fingerprint(request.getFingerprint())
                .expiresAt(expirationTime)
                .build());
    }

    private void logFailedRequest(AppTokenRequestWithComponents request, String clientIp, String userAgent, String reason, boolean isSuspectedBot) {
        RequestLog failedLog = RequestLog.builder()
            .fingerprint(request.getFingerprint())
            .deviceId(request.getDeviceId())
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
        try {
            log.debug("Validating token for fingerprint: {}", fingerprint);
            
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            boolean subjectValid = claims.getSubject().equals(fingerprint);
            boolean notExpired = claims.getExpiration().after(new Date());
            
            if (!subjectValid) {
                log.warn("Token validation failed - Subject mismatch. Expected: {}, Found: {}", 
                    fingerprint, claims.getSubject());
            }
            
            if (!notExpired) {
                log.warn("Token validation failed - Token expired. Expiration: {}, Current time: {}", 
                    claims.getExpiration(), new Date());
            }

            boolean isValid = subjectValid && notExpired;

            if (!isValid) {
                recordFailedAttempt(fingerprint);
                log.warn("Token validation failed for fingerprint: {}. IP: {}, User-Agent: {}", 
                    fingerprint, claims.get("ip"), claims.get("userAgent"));
            } else {
                log.debug("Token successfully validated for fingerprint: {}", fingerprint);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Token validation error for fingerprint: {}: {}", fingerprint, e.getMessage());
            recordFailedAttempt(fingerprint);
            return false;
        }
    }
}