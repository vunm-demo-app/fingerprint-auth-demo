package com.vunm.demo.domain.service;

import com.fingerprint.sdk.WebhookValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebhookService {

    @Value("${fingerprint.webhook.secret}")
    private String webhookSecret;

    @Value("${fingerprint.webhook.enabled:false}")
    private boolean webhookEnabled;

    public boolean isValidSignature(String signature, byte[] body) {
        if (!webhookEnabled) {
            log.debug("Webhook validation skipped - webhooks are disabled");
            return true;
        }
        
        if (signature == null || body == null) {
            log.warn("Invalid webhook request - missing signature or body");
            return false;
        }

        try {
            boolean isValid = WebhookValidation.isSignatureValid(signature, body, webhookSecret);
            if (!isValid) {
                log.warn("Invalid webhook signature detected");
            }
            return isValid;
        } catch (Exception e) {
            log.error("Error validating webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean isWebhookEnabled() {
        return webhookEnabled;
    }
} 