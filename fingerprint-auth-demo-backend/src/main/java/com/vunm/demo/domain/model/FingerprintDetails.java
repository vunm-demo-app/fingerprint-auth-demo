package com.vunm.demo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fingerprint_details")
public class FingerprintDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String fingerprint;
    
    @Column(length = 10000)
    private String components;
    
    private String userAgent;
    private String platform;
    private String screenResolution;
    private String timezone;
    private String language;
    private Boolean webglSupported;
    private String webglRenderer;
    private String webglVendor;
    private String cpuCores;
    private String deviceMemory;
    private String hardwareConcurrency;
    private String touchSupport;
    private String colorDepth;
    private String pixelRatio;
    private String fonts;
    private String audio;
    private String canvas;
    
    // Bot detection fields from FingerprintJS Pro
    private Double botProbability;
    private String botType;
    private Boolean isBot;
    
    // Tracking fields
    private Long firstSeenAt;
    private Long lastSeenAt;
    private Integer consistencyScore;
}