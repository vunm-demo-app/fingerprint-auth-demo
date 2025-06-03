package com.vunm.demo.api.controller;

import com.vunm.demo.config.FingerprintConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@Slf4j
@RestController
@RequestMapping("/api/fingerprint")
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class FingerprintProxyController {

    private final RestClient restClient;
    private final FingerprintConfig fingerprintConfig;

    public FingerprintProxyController(FingerprintConfig fingerprintConfig) {
        this.fingerprintConfig = fingerprintConfig;
        this.restClient = RestClient.builder()
                .baseUrl(fingerprintConfig.getApiUrl())
                .defaultHeader("Auth-API-Key", fingerprintConfig.getSecretKey())
                .build();
        
        log.info("FingerprintProxyController initialized with API URL: {}", fingerprintConfig.getApiUrl());
    }

    @GetMapping("/visitors/identify/{requestId}/{visitorId}/h")
    public ResponseEntity<String> getVisitorInfo(
            @PathVariable String requestId,
            @PathVariable String visitorId,
            @RequestParam("q") String q) {
        
        log.info("""
                [Fingerprint Get Visitor Info]
                Endpoint: {}/visitors/identify/{}/{}/h?q={}""",
                fingerprintConfig.getApiUrl(),
                requestId, visitorId, fingerprintConfig.getPublicKey()
        );
        
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri("/visitors/identify/{requestId}/{visitorId}/h?q={apiKey}", requestId, visitorId, fingerprintConfig.getPublicKey())
                    .retrieve()
                    .toEntity(String.class);
            
            log.info("✅ Get visitor info successful - Status: {}", response.getStatusCode());
            return response;
        } catch (Exception e) {
            log.error("❌ Get visitor info failed", e);
            throw e;
        }
    }

    @PostMapping("/visitors/identify")
    public ResponseEntity<String> identifyVisitor(
            @RequestBody String body,
            @RequestHeader("Content-Type") String contentType,
            @RequestParam("ci") String ci,
            @RequestParam("q") String q) {
        
        log.info("""
                [Fingerprint Identify Request]
                Endpoint: {}/visitors/identify?ci={}&q={}
                Content-Type: {} | Body Length: {}b""",
                fingerprintConfig.getApiUrl(),
                contentType,
                body.length(), ci,
                fingerprintConfig.getPublicKey()
        );
        return null;
//
//        try {
//            ResponseEntity<String> response = restClient.post()
//                    .uri("/visitors/identify?ci={}&q={}", ci, fingerprintConfig.getPublicKey())
//                    .header("Content-Type", contentType)
//                    .body(body)
//                    .retrieve()
//                    .toEntity(String.class);
//
//            log.info("✅ Identify request successful - Status: {}", response.getStatusCode());
//            return response;
//        } catch (Exception e) {
//            log.error("❌ Identify request failed", e);
//            throw e;
//        }
    }

    private String maskKey(String key) {
        if (key == null || key.isEmpty()) return "not_present";
        if (key.length() <= 8) return "***" + key.substring(key.length() - 4);
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
} 