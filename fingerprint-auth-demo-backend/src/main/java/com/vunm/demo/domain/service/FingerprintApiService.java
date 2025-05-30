package com.vunm.demo.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vunm.demo.config.FingerprintConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FingerprintApiService {
    private final RestTemplate restTemplate;
    private final FingerprintConfig fingerprintConfig;
    private final ObjectMapper objectMapper;

    /**
     * Get visitor information from FingerprintJS Pro API
     * 
     * @param visitorId The visitor ID from the client
     * @return Map containing visitor information including bot detection data
     */
    public Map<String, Object> getVisitorInfo(String visitorId) {
        if (!fingerprintConfig.isConfigured()) {
            log.warn("FingerprintJS Pro API key not configured, skipping API call");
            return new HashMap<>();
        }

        try {
            String url = UriComponentsBuilder
                .fromHttpUrl(fingerprintConfig.getApiUrl())
                .path("/visitors/" + visitorId)
                .queryParam("region", fingerprintConfig.getApiRegion())
                .build()
                .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Auth-API-Key", fingerprintConfig.getApiKey());
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Calling FingerprintJS Pro API for visitor: {}", visitorId);
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                Map<String, Object> result = new HashMap<>();
                
                // Extract bot detection data if available
                if (rootNode.has("products") && rootNode.get("products").has("botd")) {
                    JsonNode botdNode = rootNode.get("products").get("botd");
                    if (botdNode.has("data") && botdNode.get("data").has("bot")) {
                        JsonNode botNode = botdNode.get("data").get("bot");
                        
                        double probability = botNode.has("probability") ? 
                            botNode.get("probability").asDouble() : 0.0;
                        
                        String type = botNode.has("type") ? 
                            botNode.get("type").asText() : "unknown";
                        
                        result.put("botProbability", probability);
                        result.put("botType", type);
                        result.put("isBot", probability > 0.8); // Consider as bot if probability > 80%
                        
                        log.info("Bot detection for visitor {}: probability={}, type={}", 
                            visitorId, probability, type);
                    }
                }
                
                // Add raw response for debugging
                result.put("rawResponse", response.getBody());
                
                return result;
            } else {
                log.error("Failed to get visitor info from FingerprintJS Pro API: {}", response.getStatusCode());
                return new HashMap<>();
            }
        } catch (Exception e) {
            log.error("Error calling FingerprintJS Pro API: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}