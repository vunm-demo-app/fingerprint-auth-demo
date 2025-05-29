package com.vunm.demo.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FingerprintDetails {
    @Id
    private String fingerprint;
    
    @Column(length = 20000)
    private String components;  // JSON string of component hashes
    
    @Column(length = 1000)
    private String userAgent;
    
    @Column(length = 500)
    private String platform;
    
    @Column(length = 500)
    private String screenResolution;
    
    @Column(length = 500)
    private String timezone;
    
    @Column(length = 500)
    private String language;
    
    private Boolean webglSupported;
    
    @Column(length = 1000)
    private String webglRenderer;
    
    @Column(length = 1000)
    private String webglVendor;
    
    @Column(length = 500)
    private String cpuCores;
    
    @Column(length = 500)
    private String deviceMemory;
    
    @Column(length = 500)
    private String hardwareConcurrency;
    
    @Column(length = 500)
    private String touchSupport;
    
    @Column(length = 500)
    private String colorDepth;
    
    @Column(length = 500)
    private String pixelRatio;
    
    @Column(length = 20000)
    private String fonts;  // JSON array of available fonts
    
    @Column(length = 20000)
    private String audio;  // Audio fingerprint hash
    
    @Column(length = 20000)
    private String canvas;  // Canvas fingerprint hash
    
    private Long firstSeenAt;
    private Long lastSeenAt;
    private Integer consistencyScore;  // Score based on how consistent the fingerprint components are
} 