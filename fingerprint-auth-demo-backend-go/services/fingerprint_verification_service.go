package services

import (
	"encoding/json"
	"log"
	"strings"
	"time"

	"github.com/vunm/fingerprint-auth-demo-backend-go/database"
	"github.com/vunm/fingerprint-auth-demo-backend-go/models"
)

// VerifyFingerprint verifies a fingerprint and its components
func VerifyFingerprint(fingerprint string, components map[string]interface{}) bool {
	// 1. Basic validation
	if fingerprint == "" || components == nil {
		log.Println("Null fingerprint or components")
		return false
	}

	log.Printf("Verifying fingerprint: %s", fingerprint)

	// 2. Check with FingerprintJS Pro API for bot detection
	visitorInfo, err := GetVisitorInfo(fingerprint)
	if err != nil {
		log.Printf("Error getting visitor info: %v", err)
	}

	// 3. If API indicates this is a bot with high confidence, reject immediately
	if isBot, ok := visitorInfo["isBot"].(bool); ok && isBot {
		log.Printf("FingerprintJS Pro API identified visitor as bot: %s", fingerprint)
		log.Printf("Bot type: %v, probability: %v", 
			visitorInfo["botType"], visitorInfo["botProbability"])
		return false
	}

	// 4. Merge API data with components
	if len(visitorInfo) > 0 {
		for k, v := range visitorInfo {
			components[k] = v
		}
		log.Printf("Enhanced components with API data: %v", visitorInfo)
	}

	// 5. Extract components
	details, err := buildFingerprintDetails(fingerprint, components)
	if err != nil {
		log.Printf("Error building fingerprint details: %v", err)
		return false
	}

	// 6. Check if fingerprint exists
	var existingDetails models.FingerprintDetails
	result := database.DB.Where("fingerprint = ?", fingerprint).First(&existingDetails)
	
	if result.RowsAffected > 0 {
		log.Printf("Found existing fingerprint, verifying consistency")
		// 7. Verify consistency with existing fingerprint
		return verifyConsistency(&existingDetails, details)
	} else {
		log.Printf("New fingerprint detected, checking for suspicious patterns")
		// 8. Check for suspicious patterns in new fingerprint
		if isSuspiciousFingerprint(details) {
			log.Printf("Suspicious pattern detected for new fingerprint: %s", fingerprint)
			return false
		}
		
		// 9. Save new fingerprint
		log.Printf("Saving new fingerprint: %s", fingerprint)
		details.FirstSeenAt = time.Now().Unix()
		details.LastSeenAt = time.Now().Unix()
		details.ConsistencyScore = 100
		database.DB.Create(details)
		return true
	}
}

// buildFingerprintDetails builds a FingerprintDetails struct from components
func buildFingerprintDetails(fingerprint string, components map[string]interface{}) (*models.FingerprintDetails, error) {
	// Convert components to JSON string
	componentsJSON, err := json.Marshal(components)
	if err != nil {
		return nil, err
	}

	// Extract bot detection data from components if available
	var botProbability float64
	var botType string
	var isBot bool

	if prob, ok := components["botProbability"]; ok {
		switch v := prob.(type) {
		case float64:
			botProbability = v
		case string:
			// Try to parse string to float
		}
	}

	if typ, ok := components["botType"].(string); ok {
		botType = typ
	}

	if bot, ok := components["isBot"].(bool); ok {
		isBot = bot
	}

	// Build the details
	details := &models.FingerprintDetails{
		Fingerprint:        fingerprint,
		Components:         string(componentsJSON),
		UserAgent:          getStringValue(components, "userAgent"),
		Platform:           getStringValue(components, "platform"),
		ScreenResolution:   getStringValue(components, "screenResolution"),
		Timezone:           getStringValue(components, "timezone"),
		Language:           getStringValue(components, "language"),
		WebglSupported:     getBoolValue(components, "webglSupported"),
		WebglRenderer:      getStringValue(components, "webglRenderer"),
		WebglVendor:        getStringValue(components, "webglVendor"),
		CpuCores:           getStringValue(components, "cpuCores"),
		DeviceMemory:       getStringValue(components, "deviceMemory"),
		HardwareConcurrency: getStringValue(components, "hardwareConcurrency"),
		TouchSupport:       getStringValue(components, "touchSupport"),
		ColorDepth:         getStringValue(components, "colorDepth"),
		PixelRatio:         getStringValue(components, "pixelRatio"),
		Fonts:              getStringValue(components, "fonts"),
		Audio:              getStringValue(components, "audio"),
		Canvas:             getStringValue(components, "canvas"),
		BotProbability:     botProbability,
		BotType:            botType,
		IsBot:              isBot,
		LastSeenAt:         time.Now().Unix(),
	}

	return details, nil
}

// verifyConsistency verifies the consistency of a fingerprint
func verifyConsistency(existing *models.FingerprintDetails, current *models.FingerprintDetails) bool {
	// 1. Update last seen timestamp
	existing.LastSeenAt = time.Now().Unix()
	
	// 2. Update bot detection data if available
	if current.BotProbability > 0 {
		existing.BotProbability = current.BotProbability
	}
	
	if current.BotType != "" {
		existing.BotType = current.BotType
	}
	
	if current.IsBot {
		existing.IsBot = current.IsBot
	}
	
	// 3. If marked as bot by the API, reject
	if existing.IsBot {
		log.Printf("Fingerprint marked as bot: %s, type: %s, probability: %f", 
			existing.Fingerprint, existing.BotType, existing.BotProbability)
		existing.ConsistencyScore = max(0, existing.ConsistencyScore-50)
		database.DB.Save(existing)
		return false
	}
	
	// 4. Check for critical components that should never change
	log.Printf("Comparing critical components for fingerprint: %s", existing.Fingerprint)
	log.Printf("Existing canvas: %s, Current canvas: %s", existing.Canvas, current.Canvas)
	log.Printf("Existing audio: %s, Current audio: %s", existing.Audio, current.Audio)
	
	if existing.Canvas != current.Canvas || existing.Audio != current.Audio {
		log.Printf("Critical component mismatch for fingerprint: %s", existing.Fingerprint)
		log.Printf("Canvas match: %t, Audio match: %t", 
			existing.Canvas == current.Canvas, existing.Audio == current.Audio)
		existing.ConsistencyScore = max(0, existing.ConsistencyScore-20)
		database.DB.Save(existing)
		return false
	}

	// 5. Check for components that rarely change
	log.Printf("Checking rarely changing components")
	changes := 0
	if existing.WebglRenderer != current.WebglRenderer {
		log.Printf("WebGL renderer changed: %s -> %s", existing.WebglRenderer, current.WebglRenderer)
		changes++
	}
	if existing.WebglVendor != current.WebglVendor {
		log.Printf("WebGL vendor changed: %s -> %s", existing.WebglVendor, current.WebglVendor)
		changes++
	}
	if existing.CpuCores != current.CpuCores {
		log.Printf("CPU cores changed: %s -> %s", existing.CpuCores, current.CpuCores)
		changes++
	}
	if existing.HardwareConcurrency != current.HardwareConcurrency {
		log.Printf("Hardware concurrency changed: %s -> %s", existing.HardwareConcurrency, current.HardwareConcurrency)
		changes++
	}
	if existing.ColorDepth != current.ColorDepth {
		log.Printf("Color depth changed: %s -> %s", existing.ColorDepth, current.ColorDepth)
		changes++
	}
	if existing.PixelRatio != current.PixelRatio {
		log.Printf("Pixel ratio changed: %s -> %s", existing.PixelRatio, current.PixelRatio)
		changes++
	}

	if changes > 2 {
		log.Printf("Too many hardware components changed (%d changes) for fingerprint: %s", changes, existing.Fingerprint)
		existing.ConsistencyScore = max(0, existing.ConsistencyScore-10)
		database.DB.Save(existing)
		return false
	}

	// 6. Update consistency score
	log.Printf("Updating consistency score. Current: %d", existing.ConsistencyScore)
	existing.ConsistencyScore = min(100, existing.ConsistencyScore+1)
	database.DB.Save(existing)
	
	return existing.ConsistencyScore >= 50
}

// isSuspiciousFingerprint checks if a fingerprint is suspicious
func isSuspiciousFingerprint(details *models.FingerprintDetails) bool {
	// 1. If marked as bot by the API with high probability, consider suspicious
	if details.BotProbability > 0.7 {
		log.Printf("High bot probability detected: %f, type: %s", 
			details.BotProbability, details.BotType)
		return true
	}
	
	// 2. Check for similar fingerprints with same environment
	var similarFingerprints []models.FingerprintDetails
	database.DB.Where("fingerprint != ? AND user_agent = ? AND platform = ? AND screen_resolution = ? AND timezone = ? AND language = ?",
		details.Fingerprint, details.UserAgent, details.Platform, details.ScreenResolution, details.Timezone, details.Language).
		Find(&similarFingerprints)

	if len(similarFingerprints) > 0 {
		log.Printf("Found similar fingerprints with same environment: %d", len(similarFingerprints))
		return true
	}

	// 3. Check for canvas/audio fingerprint reuse
	var matchingFingerprints []models.FingerprintDetails
	database.DB.Where("fingerprint != ? AND (canvas = ? OR audio = ?)",
		details.Fingerprint, details.Canvas, details.Audio).
		Find(&matchingFingerprints)

	if len(matchingFingerprints) > 0 {
		log.Printf("Found fingerprints with matching canvas/audio: %d", len(matchingFingerprints))
		return true
	}

	// 4. Check for bot patterns
	if isBotPattern(details) {
		log.Printf("Bot pattern detected in fingerprint components")
		return true
	}

	return false
}

// isBotPattern checks if a fingerprint matches known bot patterns
func isBotPattern(details *models.FingerprintDetails) bool {
	userAgent := strings.ToLower(details.UserAgent)

	// Check for common bot user agents
	if strings.Contains(userAgent, "bot") ||
		strings.Contains(userAgent, "crawler") ||
		strings.Contains(userAgent, "spider") ||
		strings.Contains(userAgent, "slurp") ||
		strings.Contains(userAgent, "googlebot") ||
		strings.Contains(userAgent, "bingbot") ||
		strings.Contains(userAgent, "baiduspider") ||
		strings.Contains(userAgent, "yandexbot") ||
		userAgent == "" {
		log.Printf("Bot user agent detected: %s", details.UserAgent)
		return true
	}

	// Check for headless browser characteristics
	if strings.Contains(userAgent, "headlesschrome") ||
		strings.Contains(userAgent, "phantomjs") ||
		strings.Contains(userAgent, "selenium") {
		log.Printf("Headless browser detected: %s", details.UserAgent)
		return true
	}

	// Check for automation frameworks
	if strings.Contains(details.WebglRenderer, "SwiftShader") ||
		strings.Contains(details.WebglVendor, "Google Inc.") {
		log.Printf("Automation framework detected: WebGL %s, %s", 
			details.WebglRenderer, details.WebglVendor)
		return true
	}

	// Check for suspicious hardware configurations
	if details.CpuCores == "1" ||
		details.DeviceMemory == "undefined" ||
		details.HardwareConcurrency == "0" {
		log.Printf("Suspicious hardware configuration detected for fingerprint: %s", 
			details.Fingerprint)
		return true
	}

	// Check for missing or suspicious canvas/audio fingerprints
	if details.Canvas == "" || details.Audio == "" {
		log.Printf("Missing canvas/audio fingerprints for fingerprint: %s", 
			details.Fingerprint)
		return true
	}

	return false
}

// Helper functions
func getStringValue(m map[string]interface{}, key string) string {
	if val, ok := m[key]; ok {
		if str, ok := val.(string); ok {
			return str
		}
		return ""
	}
	return ""
}

func getBoolValue(m map[string]interface{}, key string) bool {
	if val, ok := m[key]; ok {
		if b, ok := val.(bool); ok {
			return b
		}
		return false
	}
	return false
}

func min(a, b int) int {
	if a < b {
		return a
	}
	return b
}

func max(a, b int) int {
	if a > b {
		return a
	}
	return b
}