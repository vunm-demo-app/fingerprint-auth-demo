package com.vunm.demo.application.port.in;

import com.vunm.demo.api.dto.AppTokenRequest;
import com.vunm.demo.api.dto.AppTokenResponse;

public interface VerifyVisitorUseCase {
    AppTokenResponse verifyVisitor(AppTokenRequest request);
} 