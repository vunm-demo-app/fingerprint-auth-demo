package com.vunm.demo.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fingerprint.api.FingerprintApi;
import com.fingerprint.model.EventsGetResponse;
import com.fingerprint.sdk.ApiClient;
import com.fingerprint.sdk.ApiException;
import com.fingerprint.sdk.Configuration;
import com.vunm.demo.application.port.in.GetVisitorInfoUseCase;
import com.vunm.demo.domain.model.VisitorInfo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorService implements GetVisitorInfoUseCase {
    private FingerprintApi fingerprintApi;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Set<String> usedRequestIds = new CopyOnWriteArraySet<>();

    @Value("${fingerprint.secret-key}")
    private String apiKey;

    @Value("${fingerprint.api-url}")
    private String apiUrl;

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
    public VisitorInfo getVisitorInfo(String visitorId, String ipAddress, String requestId) {
        // Kiểm tra requestId đã sử dụng chưa
        if (usedRequestIds.contains(requestId)) {
            log.warn("RequestId {} đã được sử dụng", requestId);
            throw new RuntimeException("RequestId đã được sử dụng");
        }

        EventsGetResponse eventResponse;
        try {
            log.debug("Getting visitor info for requestId: {}", requestId);
            eventResponse = fingerprintApi.getEvent(requestId);
        } catch (ApiException e) {
            log.warn("Failed to get event for requestId: {}. Error: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to verify requestId with Fingerprint Server", e);
        }

        if (eventResponse == null || eventResponse.getProducts().getBotd() == null) {
            log.warn("No event found for requestId: {}", requestId);
            throw new RuntimeException("RequestId not found, potential spoofing detected.");
        }

        // check null eventResponse.getProducts().getBotd().getData();
        if (eventResponse.getProducts().getBotd() == null ||
            eventResponse.getProducts().getBotd().getData() == null) {
            log.warn("No BotD data found for requestId: {}", requestId);
            return createEmptyVisitorInfo(visitorId, ipAddress, requestId);
        }

        var botDetection = eventResponse.getProducts().getBotd().getData();
        double botResultValue = 0.0;
        String botType = "unknown";
        String botResult = "notDetected";

        if (botDetection != null) {
            var bot = botDetection.getBot();
            botResult = bot.getResult().getValue();
            botType = bot.getType() != null ? bot.getType() : "unknown";
        }

        log.info("Bot detection results - requestId: {}, probability: {}, type: {}, result: {}", 
            requestId, botResultValue, botType, botResult);

        if ("bad".equals(botResult)) {
            log.warn("Bot detected with high probability ({}) and type {} for requestId: {}", 
                botResultValue, botType, requestId);
            throw new RuntimeException("Malicious bot detected, scraping is not allowed.");
        }

        // Thêm requestId vào danh sách đã sử dụng
        usedRequestIds.add(requestId);
        log.debug("Added requestId {} to usedRequestIds", requestId);

        // Get location and browser details from event if available
        String country = "Unknown";
        String city = "Unknown";
        String browser = "Unknown";
        String os = "Unknown";
        String device = "Unknown";

        try {
            var identification = eventResponse.getProducts().getIdentification();
            log.info("Identification data for visitorId:{} requestId {}: {}", visitorId, requestId, objectMapper.writeValueAsString(identification));
            if (identification != null && identification.getData() != null) {
                var data = identification.getData();
                var browserDetails = data.getBrowserDetails();
                if (browserDetails != null) {
                    browser = browserDetails.getBrowserName();
                    os = browserDetails.getOs();
                    device = browserDetails.getDevice();
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract location or browser details from event: {}", e.getMessage());
        }

        return VisitorInfo.builder()
                .visitorId(visitorId)
                .requestId(requestId)
                .isIncognito(eventResponse.getProducts().getIdentification() != null && 
                           eventResponse.getProducts().getIdentification().getData() != null && 
                           eventResponse.getProducts().getIdentification().getData().getIncognito())
                .ipAddress(ipAddress)
                .botProbability(botResultValue)
                .botType(botType)
                .location(VisitorInfo.Location.builder()
                        .country(country)
                        .city(city)
                        .build())
                .browserDetails(VisitorInfo.BrowserDetails.builder()
                        .browser(browser)
                        .os(os)
                        .device(device)
                        .build())
                .build();
    }

    private VisitorInfo createEmptyVisitorInfo(String visitorId, String ipAddress, String requestId) {
        return VisitorInfo.builder()
                .visitorId(visitorId)
                .requestId(requestId)
                .isIncognito(false)
                .ipAddress(ipAddress)
                .botProbability(1.0)  // Assume bot if no data
                .botType("unknown")
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