package com.vunm.demo.api.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AppTokenResponse {
    String token;
    String visitorId;
    String requestId;
} 