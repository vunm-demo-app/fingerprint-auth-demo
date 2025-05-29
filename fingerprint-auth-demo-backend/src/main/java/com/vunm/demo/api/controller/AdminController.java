package com.vunm.demo.api.controller;

import com.vunm.demo.domain.model.RequestLog;
import com.vunm.demo.domain.service.RequestLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final RequestLogService requestLogService;

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(requestLogService.getStatistics(from, to));
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<RequestLog>> getLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String fingerprint,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) Boolean isSuspectedBot,
            Pageable pageable) {
        return ResponseEntity.ok(requestLogService.getRequestLogs(
            from, to, fingerprint, ipAddress, isSuspectedBot, pageable
        ));
    }

    @GetMapping("/correlation")
    public ResponseEntity<List<Map<String, Object>>> getIpFingerprintCorrelation() {
        return ResponseEntity.ok(requestLogService.getIpFingerprintCorrelation());
    }
} 