package com.vunm.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppTokenRequest {
    private String visitorId;
    private String requestId;
    private String fingerprint;
    private long timestamp;
} 