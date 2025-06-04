package com.vunm.demo.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VisitorInfo {
    String visitorId;
    String requestId;
    boolean isIncognito;
    String ipAddress;
    double botProbability;
    String botType;
    Location location;
    BrowserDetails browserDetails;

    @Value
    @Builder
    public static class Location {
        String country;
        String city;
    }

    @Value
    @Builder
    public static class BrowserDetails {
        String browser;
        String os;
        String device;
    }
} 