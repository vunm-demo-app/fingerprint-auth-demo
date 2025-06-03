package com.vunm.demo.api.controller;

import com.vunm.demo.application.port.in.GetVisitorInfoUseCase;
import com.vunm.demo.domain.model.VisitorInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
public class VisitorController {
    private final GetVisitorInfoUseCase getVisitorInfoUseCase;

    @GetMapping("/{visitorId}")
    public ResponseEntity<VisitorInfo> getVisitorInfo(
            @PathVariable String visitorId,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        VisitorInfo visitorInfo = getVisitorInfoUseCase.getVisitorInfo(visitorId, ipAddress);
        return ResponseEntity.ok(visitorInfo);
    }
} 