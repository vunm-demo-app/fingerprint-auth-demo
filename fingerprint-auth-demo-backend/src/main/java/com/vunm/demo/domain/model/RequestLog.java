package com.vunm.demo.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fingerprint;
    private String deviceId;
    private String ipAddress;
    private String userAgent;
    private String requestType; // TOKEN_REQUEST, STOCK_PRICE, etc.
    private boolean isSuccess;
    private String failureReason;
    private Instant timestamp;
    private String requestPattern;
    private boolean isSuspectedBot;
} 