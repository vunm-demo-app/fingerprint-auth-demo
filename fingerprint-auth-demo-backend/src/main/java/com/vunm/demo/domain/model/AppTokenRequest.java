package com.vunm.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppTokenRequest {
    private String deviceId;
    private String fingerprint;
    private long timestamp;
} 