package config

import (
	"log"
	"os"
	"strconv"
	"strings"

	"github.com/joho/godotenv"
)

// Config holds all configuration for the application
type Config struct {
	Port                  int
	GinMode               string
	CorsAllowedOrigins    []string
	FingerprintApiKey     string
	FingerprintApiUrl     string
	FingerprintApiRegion  string
	TokenExpiration       int64
	TokenTimestampTolerance int64
	RateLimitWindow       int64
	RateLimitMax          int64
	MaxFailedAttempts     int
	FailedAttemptWindow   int64
}

var AppConfig Config

// LoadConfig loads configuration from environment variables
func LoadConfig() {
	// Load .env file if it exists
	err := godotenv.Load()
	if err != nil {
		log.Println("Warning: .env file not found, using environment variables")
	}

	// Set default values
	AppConfig = Config{
		Port:                  8080,
		GinMode:               "debug",
		CorsAllowedOrigins:    []string{"http://localhost:3000"},
		FingerprintApiUrl:     "https://api.fpjs.io",
		FingerprintApiRegion:  "us",
		TokenExpiration:       300,
		TokenTimestampTolerance: 30,
		RateLimitWindow:       3600,
		RateLimitMax:          100,
		MaxFailedAttempts:     5,
		FailedAttemptWindow:   3600,
	}

	// Override with environment variables if they exist
	if port := os.Getenv("PORT"); port != "" {
		if portInt, err := strconv.Atoi(port); err == nil {
			AppConfig.Port = portInt
		}
	}

	if mode := os.Getenv("GIN_MODE"); mode != "" {
		AppConfig.GinMode = mode
	}

	if origins := os.Getenv("CORS_ALLOWED_ORIGINS"); origins != "" {
		AppConfig.CorsAllowedOrigins = strings.Split(origins, ",")
	}

	if apiKey := os.Getenv("FINGERPRINT_API_KEY"); apiKey != "" {
		AppConfig.FingerprintApiKey = apiKey
	}

	if apiUrl := os.Getenv("FINGERPRINT_API_URL"); apiUrl != "" {
		AppConfig.FingerprintApiUrl = apiUrl
	}

	if apiRegion := os.Getenv("FINGERPRINT_API_REGION"); apiRegion != "" {
		AppConfig.FingerprintApiRegion = apiRegion
	}

	if expiration := os.Getenv("TOKEN_EXPIRATION"); expiration != "" {
		if expirationInt, err := strconv.ParseInt(expiration, 10, 64); err == nil {
			AppConfig.TokenExpiration = expirationInt
		}
	}

	if tolerance := os.Getenv("TOKEN_TIMESTAMP_TOLERANCE"); tolerance != "" {
		if toleranceInt, err := strconv.ParseInt(tolerance, 10, 64); err == nil {
			AppConfig.TokenTimestampTolerance = toleranceInt
		}
	}

	if window := os.Getenv("RATE_LIMIT_WINDOW"); window != "" {
		if windowInt, err := strconv.ParseInt(window, 10, 64); err == nil {
			AppConfig.RateLimitWindow = windowInt
		}
	}

	if max := os.Getenv("RATE_LIMIT_MAX"); max != "" {
		if maxInt, err := strconv.ParseInt(max, 10, 64); err == nil {
			AppConfig.RateLimitMax = maxInt
		}
	}

	if attempts := os.Getenv("MAX_FAILED_ATTEMPTS"); attempts != "" {
		if attemptsInt, err := strconv.Atoi(attempts); err == nil {
			AppConfig.MaxFailedAttempts = attemptsInt
		}
	}

	if window := os.Getenv("FAILED_ATTEMPT_WINDOW"); window != "" {
		if windowInt, err := strconv.ParseInt(window, 10, 64); err == nil {
			AppConfig.FailedAttemptWindow = windowInt
		}
	}

	log.Printf("Configuration loaded: %+v", AppConfig)
}