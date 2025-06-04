package com.vunm.demo.domain.service;

import com.fingerprint.api.FingerprintApi;
import com.fingerprint.model.EventsGetResponse;
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
    public VisitorInfo getVisitorInfo(String visitorId, String ipAddress, String requestId) {
        try {
            log.debug("Getting visitor info for requestId: {}", requestId);
            
            // Get event using EventsGetResponse
            EventsGetResponse eventResponse = fingerprintApi.getEvent(requestId);
            log.debug("Event response for requestId {}: {}", requestId, eventResponse);


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
                var bot = botDetection .getBot();
                botResult = bot.getResult().getValue() ;
                botType = bot.getType() != null ? bot.getType() : "unknown";
            }

            log.info("Bot detection results - requestId: {}, probability: {}, type: {}, result: {}", 
                requestId, botResultValue, botType, botResult);

            if ("bad".equals(botResult)) {
                log.warn("Bot detected with high probability ({}) and type {} for requestId: {}", 
                    botResultValue, botType, requestId);
                throw new RuntimeException("Malicious bot detected, scraping is not allowed.");
            }

            // Get location and browser details from event if available
            String country = "Unknown";
            String city = "Unknown";
            String browser = "Unknown";
            String os = "Unknown";
            String device = "Unknown";

            try {
                var identification = eventResponse.getProducts().getIdentification();
                log.info("Identification data for visitorId:{} requestId {}: {}", visitorId, requestId, identification);
                if (identification != null && identification.getData() != null) {
                    var data = identification.getData();
                    var browserDetails = data.getBrowserDetails();
                    browser = browserDetails.getBrowserName();
                    os = browserDetails.getOs();
                    device = browserDetails.getDevice();
                }
            } catch (Exception e) {
                log.warn("Could not extract location or browser details from event: {}", e.getMessage());
            }

            return VisitorInfo.builder()
                    .visitorId(visitorId)
                    .requestId(requestId)
                    .isIncognito(eventResponse.getProducts().getIdentification() != null && eventResponse.getProducts().getIdentification().getData() != null && eventResponse.getProducts().getIdentification().getData().getIncognito())
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

        } catch (ApiException e) {
            log.warn("Failed to get event for requestId: {}. Error: {}", requestId, e.getMessage());
            throw new RuntimeException(e);
        }
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