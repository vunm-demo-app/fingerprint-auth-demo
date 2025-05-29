package com.vunm.demo.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/time")
public class TimeController {

    @GetMapping
    public Map<String, Long> getCurrentTime() {
        return Map.of(
            "serverTime", Instant.now().getEpochSecond(),
            "timestamp", System.currentTimeMillis() / 1000
        );
    }
} 