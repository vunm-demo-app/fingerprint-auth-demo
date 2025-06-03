package com.vunm.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class FingerprintConfig {

    @Value("${fingerprint.secret-key:}")
    private String secretKey;

    @Value("${fingerprint.public-key:}")
    private String publicKey;

    @Value("${fingerprint.api-url:https://api.fpjs.io}")
    private String apiUrl;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .build();
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public boolean isConfigured() {
        return secretKey != null && !secretKey.isEmpty() 
            && publicKey != null && !publicKey.isEmpty();
    }
}