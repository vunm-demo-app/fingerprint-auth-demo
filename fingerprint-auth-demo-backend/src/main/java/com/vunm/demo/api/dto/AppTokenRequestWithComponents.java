package com.vunm.demo.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppTokenRequestWithComponents extends AppTokenRequest {
    private Map<String, Object> components;

    public AppTokenRequestWithComponents(String deviceId, String fingerprint, long timestamp, Map<String, Object> components) {
        super(deviceId, fingerprint, timestamp);
        this.components = components;
    }
} 