package com.vunm.demo.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vunm.demo.domain.model.FingerprintDetails;
import com.vunm.demo.domain.repository.FingerprintDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FingerprintVerificationService {
    private final FingerprintDetailsRepository fingerprintDetailsRepository;
    private final FingerprintApiService fingerprintApiService;
    private final ObjectMapper objectMapper;

    public boolean verifyFingerprint(String fingerprint, Map<String, Object> components) {
        try {
            // 1. Basic validation
            if (fingerprint == null || components == null) {
                log.warn("Null fingerprint or components");
                return false;
            }

            log.info("Verifying fingerprint: {}", fingerprint);
            log.debug("Received components: {}", components);

            // 2. Check with FingerprintJS Pro API for bot detection
            Map<String, Object> visitorInfo = fingerprintApiService.getVisitorInfo(fingerprint);
            
            // 3. If API indicates this is a bot with high confidence, reject immediately
            if (visitorInfo.containsKey("isBot") && (Boolean)visitorInfo.get("isBot")) {
                log.warn("FingerprintJS Pro API identified visitor as bot: {}", fingerprint);
                log.warn("Bot type: {}, probability: {}", 
                    visitorInfo.get("botType"), visitorInfo.get("botProbability"));
                return false;
            }
            
            // 4. Merge API data with components
            if (!visitorInfo.isEmpty()) {
                components.putAll(visitorInfo);
                log.debug("Enhanced components with API data: {}", visitorInfo.keySet());
            }

            // 5. Extract components
            FingerprintDetails details = buildFingerprintDetails(fingerprint, components);
            log.debug("Built fingerprint details: {}", details);
            
            // 6. Check if fingerprint exists
            Optional<FingerprintDetails> existingDetails = fingerprintDetailsRepository.findByFingerprint(fingerprint);
            
            if (existingDetails.isPresent()) {
                log.info("Found existing fingerprint, verifying consistency");
                // 7. Verify consistency with existing fingerprint
                return verifyConsistency(existingDetails.get(), details);
            } else {
                log.info("New fingerprint detected, checking for suspicious patterns");
                // 8. Check for suspicious patterns in new fingerprint
                if (isSuspiciousFingerprint(details)) {
                    log.warn("Suspicious pattern detected for new fingerprint: {}", fingerprint);
                    return false;
                }
                
                // 9. Save new fingerprint
                log.info("Saving new fingerprint: {}", fingerprint);
                details.setFirstSeenAt(System.currentTimeMillis());
                details.setConsistencyScore(100);
                fingerprintDetailsRepository.save(details);
                return true;
            }
        } catch (Exception e) {
            log.error("Error verifying fingerprint: {}", e.getMessage(), e);
            return false;
        }
    }

    private FingerprintDetails buildFingerprintDetails(String fingerprint, Map<String, Object> components) throws JsonProcessingException {
        // Extract bot detection data from components if available
        Double botProbability = null;
        String botType = null;
        Boolean isBot = null;
        
        if (components.containsKey("botProbability")) {
            Object probObj = components.get("botProbability");
            if (probObj instanceof Number) {
                botProbability = ((Number) probObj).doubleValue();
            } else if (probObj instanceof String) {
                try {
                    botProbability = Double.parseDouble((String) probObj);
                } catch (NumberFormatException e) {
                    log.warn("Invalid botProbability format: {}", probObj);
                }
            }
        }
        
        if (components.containsKey("botType")) {
            botType = getStringValue(components, "botType");
        }
        
        if (components.containsKey("isBot")) {
            Object isBotObj = components.get("isBot");
            if (isBotObj instanceof Boolean) {
                isBot = (Boolean) isBotObj;
            } else if (isBotObj instanceof String) {
                isBot = Boolean.parseBoolean((String) isBotObj);
            }
        }
        
        return FingerprintDetails.builder()
            .fingerprint(fingerprint)
            .components(objectMapper.writeValueAsString(components))
            .userAgent(getStringValue(components, "userAgent"))
            .platform(getStringValue(components, "platform"))
            .screenResolution(getStringValue(components, "screenResolution"))
            .timezone(getStringValue(components, "timezone"))
            .language(getStringValue(components, "language"))
            .webglSupported(getBooleanValue(components, "webglSupported"))
            .webglRenderer(getStringValue(components, "webglRenderer"))
            .webglVendor(getStringValue(components, "webglVendor"))
            .cpuCores(getStringValue(components, "cpuCores"))
            .deviceMemory(getStringValue(components, "deviceMemory"))
            .hardwareConcurrency(getStringValue(components, "hardwareConcurrency"))
            .touchSupport(getStringValue(components, "touchSupport"))
            .colorDepth(getStringValue(components, "colorDepth"))
            .pixelRatio(getStringValue(components, "pixelRatio"))
            .fonts(getStringValue(components, "fonts"))
            .audio(getStringValue(components, "audio"))
            .canvas(getStringValue(components, "canvas"))
            .botProbability(botProbability)
            .botType(botType)
            .isBot(isBot)
            .lastSeenAt(System.currentTimeMillis())
            .build();
    }

    private boolean verifyConsistency(FingerprintDetails existing, FingerprintDetails current) {
        // 1. Update last seen timestamp
        existing.setLastSeenAt(System.currentTimeMillis());
        
        // 2. Update bot detection data if available
        if (current.getBotProbability() != null) {
            existing.setBotProbability(current.getBotProbability());
        }
        
        if (current.getBotType() != null) {
            existing.setBotType(current.getBotType());
        }
        
        if (current.getIsBot() != null) {
            existing.setIsBot(current.getIsBot());
        }
        
        // 3. If marked as bot by the API, reject
        if (Boolean.TRUE.equals(existing.getIsBot())) {
            log.warn("Fingerprint marked as bot: {}, type: {}, probability: {}", 
                existing.getFingerprint(), existing.getBotType(), existing.getBotProbability());
            existing.setConsistencyScore(Math.max(0, existing.getConsistencyScore() - 50));
            fingerprintDetailsRepository.save(existing);
            return false;
        }
        
        // 4. Check for critical components that should never change
        log.debug("Comparing critical components for fingerprint: {}", existing.getFingerprint());
        log.debug("Existing canvas: {}, Current canvas: {}", existing.getCanvas(), current.getCanvas());
        log.debug("Existing audio: {}, Current audio: {}", existing.getAudio(), current.getAudio());
        
        if (!existing.getCanvas().equals(current.getCanvas()) ||
            !existing.getAudio().equals(current.getAudio())) {
            log.warn("Critical component mismatch for fingerprint: {}", existing.getFingerprint());
            log.warn("Canvas match: {}, Audio match: {}", 
                existing.getCanvas().equals(current.getCanvas()),
                existing.getAudio().equals(current.getAudio()));
            existing.setConsistencyScore(Math.max(0, existing.getConsistencyScore() - 20));
            fingerprintDetailsRepository.save(existing);
            return false;
        }

        // 5. Check for components that rarely change
        log.debug("Checking rarely changing components");
        int changes = 0;
        if (!existing.getWebglRenderer().equals(current.getWebglRenderer())) {
            log.debug("WebGL renderer changed: {} -> {}", existing.getWebglRenderer(), current.getWebglRenderer());
            changes++;
        }
        if (!existing.getWebglVendor().equals(current.getWebglVendor())) {
            log.debug("WebGL vendor changed: {} -> {}", existing.getWebglVendor(), current.getWebglVendor());
            changes++;
        }
        if (!existing.getCpuCores().equals(current.getCpuCores())) {
            log.debug("CPU cores changed: {} -> {}", existing.getCpuCores(), current.getCpuCores());
            changes++;
        }
        if (!existing.getHardwareConcurrency().equals(current.getHardwareConcurrency())) {
            log.debug("Hardware concurrency changed: {} -> {}", existing.getHardwareConcurrency(), current.getHardwareConcurrency());
            changes++;
        }
        if (!existing.getColorDepth().equals(current.getColorDepth())) {
            log.debug("Color depth changed: {} -> {}", existing.getColorDepth(), current.getColorDepth());
            changes++;
        }
        if (!existing.getPixelRatio().equals(current.getPixelRatio())) {
            log.debug("Pixel ratio changed: {} -> {}", existing.getPixelRatio(), current.getPixelRatio());
            changes++;
        }

        if (changes > 2) {
            log.warn("Too many hardware components changed ({} changes) for fingerprint: {}", changes, existing.getFingerprint());
            existing.setConsistencyScore(Math.max(0, existing.getConsistencyScore() - 10));
            fingerprintDetailsRepository.save(existing);
            return false;
        }

        // 6. Update consistency score
        log.debug("Updating consistency score. Current: {}", existing.getConsistencyScore());
        existing.setConsistencyScore(Math.min(100, existing.getConsistencyScore() + 1));
        fingerprintDetailsRepository.save(existing);
        
        return existing.getConsistencyScore() >= 50;
    }

    private boolean isSuspiciousFingerprint(FingerprintDetails details) {
        // 1. If marked as bot by the API with high probability, consider suspicious
        if (details.getBotProbability() != null && details.getBotProbability() > 0.7) {
            log.warn("High bot probability detected: {}, type: {}", 
                details.getBotProbability(), details.getBotType());
            return true;
        }
        
        // 2. Check for similar fingerprints with same environment
        List<FingerprintDetails> similarFingerprints = fingerprintDetailsRepository.findSimilarFingerprints(
            details.getFingerprint(),
            details.getUserAgent(),
            details.getPlatform(),
            details.getScreenResolution(),
            details.getTimezone(),
            details.getLanguage()
        );

        if (!similarFingerprints.isEmpty()) {
            log.warn("Found similar fingerprints with same environment: {}", similarFingerprints.size());
            return true;
        }

        // 3. Check for canvas/audio fingerprint reuse
        List<FingerprintDetails> matchingFingerprints = fingerprintDetailsRepository
            .findByCanvasOrAudioFingerprint(details.getCanvas(), details.getAudio());

        if (!matchingFingerprints.isEmpty()) {
            log.warn("Found fingerprints with matching canvas/audio: {}", matchingFingerprints.size());
            return true;
        }

        // 4. Check for bot patterns
        if (isBotPattern(details)) {
            log.warn("Bot pattern detected in fingerprint components");
            return true;
        }

        return false;
    }

    private boolean isBotPattern(FingerprintDetails details) {
        String userAgent = details.getUserAgent().toLowerCase();

        // Check for common bot user agents
        if (userAgent.contains("bot") ||
            userAgent.contains("crawler") ||
            userAgent.contains("spider") ||
            userAgent.contains("slurp") ||
            userAgent.contains("googlebot") ||
            userAgent.contains("bingbot") ||
            userAgent.contains("baiduspider") ||
            userAgent.contains("yandexbot") ||
            userAgent.isEmpty()) {
            log.warn("Bot user agent detected: {}", details.getUserAgent());
            return true;
        }

        // Check for headless browser characteristics
        if (userAgent.contains("headlesschrome") ||
            userAgent.contains("phantomjs") ||
            userAgent.contains("selenium")) {
            log.warn("Headless browser detected: {}", details.getUserAgent());
            return true;
        }

        // Check for automation frameworks
        if (details.getWebglRenderer().contains("SwiftShader") ||
            details.getWebglVendor().contains("Google Inc.")) {
            log.warn("Automation framework detected: WebGL {}, {}", 
                details.getWebglRenderer(), details.getWebglVendor());
            return true;
        }

        // Check for suspicious hardware configurations
        if ("1".equals(details.getCpuCores()) ||
            "undefined".equals(details.getDeviceMemory()) ||
            "0".equals(details.getHardwareConcurrency())) {
            log.warn("Suspicious hardware configuration detected for fingerprint: {}", 
                details.getFingerprint());
            return true;
        }

        // Check for missing or suspicious canvas/audio fingerprints
        if (details.getCanvas() == null || details.getCanvas().isEmpty() ||
            details.getAudio() == null || details.getAudio().isEmpty()) {
            log.warn("Missing canvas/audio fingerprints for fingerprint: {}", 
                details.getFingerprint());
            return true;
        }

        return false;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private Boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Boolean ? (Boolean) value : false;
    }
}