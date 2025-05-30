package com.vunm.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FingerprintConfig {

    @Value("${app.fingerprint.api-key:}")
    private String apiKey;

    @Value("${app.fingerprint.api-url:https://api.fpjs.io}")
    private String apiUrl;

    @Value("${app.fingerprint.api-region:us}")
    private String apiRegion;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiRegion() {
        return apiRegion;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}