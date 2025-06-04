//package com.vunm.demo;
//
//import com.vunm.demo.domain.service.TokenService;
//import com.vunm.demo.domain.repository.FingerprintDetailsRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//public class BotDetectionTest {
//
//    @Autowired
//    private TokenService tokenService;
//
//    @Autowired
//    private FingerprintDetailsRepository fingerprintDetailsRepository;
//
//    @BeforeEach
//    void setUp() {
//        fingerprintDetailsRepository.deleteAll();
//    }
//
//    private Map<String, Object> createValidComponents(String userAgent) {
//        Map<String, Object> components = new HashMap<>();
//        components.put("userAgent", userAgent);
//        components.put("platform", "Win32");
//        components.put("screenResolution", "1920x1080");
//        components.put("timezone", "UTC+7");
//        components.put("language", "en-US");
//        components.put("webglSupported", true);
//        components.put("webglRenderer", "ANGLE (AMD Radeon RX 6800 XT Direct3D11 vs_5_0)");
//        components.put("webglVendor", "AMD");
//        components.put("cpuCores", "16");
//        components.put("deviceMemory", "32");
//        components.put("hardwareConcurrency", "32");
//        components.put("touchSupport", "false");
//        components.put("colorDepth", "24");
//        components.put("pixelRatio", "1");
//        components.put("fonts", "Arial,Helvetica,Times New Roman");
//        components.put("audio", "124.04347527516074");
//        components.put("canvas", "canvas1:4b2c16cf3c804f50ba3fd6c0e1e6cd83");
//        return components;
//    }
//
//    @Test
//    public void testBotUserAgentDetection() {
//        // Test known bot user agents
//        String[] botUserAgents = {
//            "Googlebot/2.1",
//            "Mozilla/5.0 (compatible; Bingbot/2.0)",
//            "Mozilla/5.0 (compatible; Crawler/1.0)",
//            "HeadlessChrome/88.0.4324.150",
//            ""  // Empty user agent
//        };
//
//        for (String botAgent : botUserAgents) {
//            Map<String, Object> components = createValidComponents(botAgent);
//
//            AppTokenRequestWithComponents request = new AppTokenRequestWithComponents(
//                "test-device-001",
//                "test-fp-001",
//                Instant.now().getEpochSecond(),
//                components
//            );
//
//            var result = tokenService.generateTokenIfValid(request, "127.0.0.1", botAgent, components);
//            assertFalse(result.isPresent(), "Should reject bot user agent: " + botAgent);
//        }
//
//        // Test legitimate user agent
//        String legitimateAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
//        Map<String, Object> components = createValidComponents(legitimateAgent);
//
//        AppTokenRequestWithComponents request = new AppTokenRequestWithComponents(
//            "test-device-001",
//            "test-fp-001",
//            Instant.now().getEpochSecond(),
//            components
//        );
//
//        var result = tokenService.generateTokenIfValid(
//            request,
//            "127.0.0.1",
//            legitimateAgent,
//            components
//        );
//        assertTrue(result.isPresent(), "Should accept legitimate user agent");
//    }
//
//    @Test
//    public void testRateLimiting() throws InterruptedException {
//        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0.4472.124";
//        Map<String, Object> components = createValidComponents(userAgent);
//
//        AppTokenRequestWithComponents request = new AppTokenRequestWithComponents(
//            "test-device-002",
//            "test-fp-002",
//            Instant.now().getEpochSecond(),
//            components
//        );
//
//        // Make rapid requests
//        ExecutorService executor = Executors.newFixedThreadPool(10);
//        for (int i = 0; i < 150; i++) {
//            executor.submit(() -> {
//                tokenService.generateTokenIfValid(request, "127.0.0.1", userAgent, components);
//            });
//        }
//
//        executor.shutdown();
//        executor.awaitTermination(5, TimeUnit.SECONDS);
//
//        // Verify rate limiting
//        var result = tokenService.generateTokenIfValid(request, "127.0.0.1", userAgent, components);
//        assertFalse(result.isPresent(), "Should be rate limited after too many requests");
//    }
//
//    @Test
//    public void testAbnormalPatternDetection() {
//        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0.4472.124";
//        Map<String, Object> components = createValidComponents(userAgent);
//
//        AppTokenRequestWithComponents request = new AppTokenRequestWithComponents(
//            "test-device-003",
//            "test-fp-003",
//            Instant.now().getEpochSecond(),
//            components
//        );
//
//        // Simulate requests from multiple IPs (bot behavior)
//        String[] ips = {"1.1.1.1", "2.2.2.2", "3.3.3.3", "4.4.4.4", "5.5.5.5"};
//        for (String ip : ips) {
//            tokenService.generateTokenIfValid(request, ip, userAgent, components);
//        }
//
//        // Verify pattern detection
//        var result = tokenService.generateTokenIfValid(request, "6.6.6.6", userAgent, components);
//        assertFalse(result.isPresent(), "Should detect abnormal IP pattern");
//    }
//
//    @Test
//    public void testFailedAttemptTracking() {
//        String fingerprint = "test-fp-004";
//        String invalidToken = "invalid-token";
//
//        // Simulate multiple failed attempts
//        for (int i = 0; i < 6; i++) {
//            tokenService.validateToken(invalidToken, fingerprint);
//        }
//
//        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0.4472.124";
//        Map<String, Object> components = createValidComponents(userAgent);
//
//        AppTokenRequestWithComponents request = new AppTokenRequestWithComponents(
//            "test-device-004",
//            fingerprint,
//            Instant.now().getEpochSecond(),
//            components
//        );
//
//        var result = tokenService.generateTokenIfValid(
//            request,
//            "127.0.0.1",
//            userAgent,
//            components
//        );
//        assertFalse(result.isPresent(), "Should reject requests from suspicious fingerprint");
//    }
//}