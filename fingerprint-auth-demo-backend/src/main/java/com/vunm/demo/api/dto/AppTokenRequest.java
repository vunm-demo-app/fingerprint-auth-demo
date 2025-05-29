package com.vunm.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppTokenRequest {
    private String deviceId;
    private String fingerprint;
    private long timestamp;
} 