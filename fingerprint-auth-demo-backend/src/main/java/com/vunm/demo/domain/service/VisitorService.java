package com.vunm.demo.domain.service;

import com.fingerprint.api.FingerprintApi;
import com.fingerprint.model.VisitorsGetResponse;
import com.fingerprint.sdk.ApiClient;
import com.fingerprint.sdk.ApiException;
import com.fingerprint.sdk.Configuration;
import com.vunm.demo.application.port.in.GetVisitorInfoUseCase;
import com.vunm.demo.domain.model.VisitorInfo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class VisitorService implements GetVisitorInfoUseCase {
    private FingerprintApi fingerprintApi;
    private final RestClient restClient;

    @Value("${fingerprint.secret-key}")
    private String apiKey;

    @Value("${fingerprint.api-url}")
    private String apiUrl;

    public VisitorService(RestClient restClient) {
        this.restClient = restClient;
    }

    @PostConstruct
    public void init() {
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║            Initializing Fingerprint API Client                 ║");
        log.info("╚════════════════════════════════════════════════════════════════╝");
        
        log.info("API URL: {}", apiUrl);
        log.info("API Key: {}", apiKey != null ? (apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4)) : "null");
        
        // Initialize Fingerprint API client
        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setApiKey(apiKey);
        apiClient.setBasePath(apiUrl);
        this.fingerprintApi = new FingerprintApi(apiClient);
        
        log.info("✅ Fingerprint API client initialized successfully");
    }

    @Override
    public VisitorInfo getVisitorInfo(String visitorId, String ipAddress) {
        try {
            log.debug("Getting visitor info for ID: {} and IP: {}", visitorId, ipAddress);
            // Get visits using the SDK with required parameters
            VisitorsGetResponse response = fingerprintApi.getVisits(
                visitorId,      // visitorId
                null,          // requestId
                null,          // linkedId
                1,            // limit to 1 result
                null,         // paginationKey
                null          // before
            );
            
            if (response.getVisits() == null || response.getVisits().isEmpty()) {
                log.warn("No visits found for visitor ID: {}", visitorId);
                return createEmptyVisitorInfo(visitorId, ipAddress);
            }

            var visit = response.getVisits().get(0);
            log.info("Found visit data for visitor ID: {}", visitorId);

            return VisitorInfo.builder()
                    .visitorId(visitorId)
                    .isIncognito(false)
                    .ipAddress(ipAddress)
                    .location(VisitorInfo.Location.builder()
                            .country("Unknown")
                            .city("Unknown")
                            .build())
                    .browserDetails(VisitorInfo.BrowserDetails.builder()
                            .browser("Unknown")
                            .os("Unknown")
                            .device("Unknown")
                            .build())
                    .build();

        } catch (ApiException e) {
            log.warn("Failed to get visitor information for visitorId: {}. Error: {}", visitorId, e.getMessage());
            return createEmptyVisitorInfo(visitorId, ipAddress);
        }
    }

    private VisitorInfo createEmptyVisitorInfo(String visitorId, String ipAddress) {
        return VisitorInfo.builder()
                .visitorId(visitorId)
                .isIncognito(false)
                .ipAddress(ipAddress)
                .location(VisitorInfo.Location.builder()
                        .country("Unknown")
                        .city("Unknown")
                        .build())
                .browserDetails(VisitorInfo.BrowserDetails.builder()
                        .browser("Unknown")
                        .os("Unknown")
                        .device("Unknown")
                        .build())
                .build();
    }
} 