package com.vunm.demo.application.port.in;

import com.vunm.demo.domain.model.VisitorInfo;
 
public interface GetVisitorInfoUseCase {
    VisitorInfo getVisitorInfo(String visitorId, String ipAddress, String requestId);
} 