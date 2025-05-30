package services

import (
	"log"
	"sync"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/vunm/fingerprint-auth-demo-backend-go/config"
	"github.com/vunm/fingerprint-auth-demo-backend-go/database"
	"github.com/vunm/fingerprint-auth-demo-backend-go/models"
)

var (
	// In-memory caches for rate limiting and failed attempts
	requestCounts       = make(map[string]int)
	lastRequestTimes    = make(map[string]int64)
	failedAttempts      = make(map[string]int)
	lastFailedAttemptTime = make(map[string]int64)
	mutex               = &sync.Mutex{}
	
	// JWT signing key
	jwtKey = []byte("your-secret-key") // In production, use a secure key from environment variables
)

// GenerateTokenIfValid generates a token if the request is valid
func GenerateTokenIfValid(request models.AppTokenRequest, clientIP, userAgent string) (*models.AppToken, error) {
	log.Printf("Validating token request from IP: %s, User-Agent: %s", clientIP, userAgent)

	// 0. Check failed attempts
	if hasTooManyFailedAttempts(request.Fingerprint) {
		log.Printf("Too many failed attempts for fingerprint: %s, IP: %s, User-Agent: %s", 
			request.Fingerprint, clientIP, userAgent)
		logFailedRequest(request, clientIP, userAgent, "Too many failed attempts", false)
		return nil, nil
	}

	// 1. Verify fingerprint components
	if !VerifyFingerprint(request.Fingerprint, request.Components) {
		log.Printf("Invalid fingerprint detected - IP: %s, User-Agent: %s, DeviceId: %s", 
			clientIP, userAgent, request.DeviceId)
		logFailedRequest(request, clientIP, userAgent, "Invalid fingerprint", false)
		return nil, nil
	}

	// 2. Check rate limiting
	if isRateLimited(request.Fingerprint) {
		log.Printf("Rate limit exceeded - IP: %s, Fingerprint: %s, User-Agent: %s, DeviceId: %s", 
			clientIP, request.Fingerprint, userAgent, request.DeviceId)
		logFailedRequest(request, clientIP, userAgent, "Rate limit exceeded", true)
		return nil, nil
	}

	// 3. Validate timestamp
	now := time.Now().Unix()
	if abs(now-request.Timestamp) > config.AppConfig.TokenTimestampTolerance {
		log.Printf("Invalid timestamp - IP: %s, Request time: %d, Current time: %d, Difference: %d seconds", 
			clientIP, request.Timestamp, now, abs(now-request.Timestamp))
		logFailedRequest(request, clientIP, userAgent, "Invalid timestamp", false)
		return nil, nil
	}

	log.Printf("Generating token - IP: %s, Fingerprint: %s, DeviceId: %s, User-Agent: %s, Timestamp: %d", 
		clientIP, request.Fingerprint, request.DeviceId, userAgent, now)

	// 4. Generate token
	expirationTime := now + config.AppConfig.TokenExpiration
	claims := jwt.MapClaims{
		"sub": request.Fingerprint,
		"exp": expirationTime,
		"iat": now,
		"deviceId": request.DeviceId,
		"ip": clientIP,
		"userAgent": userAgent,
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenString, err := token.SignedString(jwtKey)
	if err != nil {
		log.Printf("Error signing token: %v", err)
		return nil, err
	}

	// 5. Log successful request
	successLog := models.RequestLog{
		Fingerprint:    request.Fingerprint,
		DeviceId:       request.DeviceId,
		IpAddress:      clientIP,
		UserAgent:      userAgent,
		RequestType:    "TOKEN_REQUEST",
		IsSuccess:      true,
		Timestamp:      time.Now(),
		IsSuspectedBot: false,
	}
	database.DB.Create(&successLog)

	return &models.AppToken{
		Token:       tokenString,
		Fingerprint: request.Fingerprint,
		ExpiresAt:   expirationTime,
	}, nil
}

// ValidateToken validates a token
func ValidateToken(tokenString, fingerprint string) bool {
	log.Printf("Validating token for fingerprint: %s", fingerprint)
	
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		return jwtKey, nil
	})

	if err != nil {
		log.Printf("Token validation error for fingerprint: %s: %v", fingerprint, err)
		recordFailedAttempt(fingerprint)
		return false
	}

	if claims, ok := token.Claims.(jwt.MapClaims); ok && token.Valid {
		// Check subject
		if sub, ok := claims["sub"].(string); ok && sub == fingerprint {
			log.Printf("Token successfully validated for fingerprint: %s", fingerprint)
			return true
		}
		
		log.Printf("Token validation failed - Subject mismatch. Expected: %s, Found: %s", 
			fingerprint, claims["sub"])
	}

	recordFailedAttempt(fingerprint)
	return false
}

// isRateLimited checks if a fingerprint is rate limited
func isRateLimited(fingerprint string) bool {
	mutex.Lock()
	defer mutex.Unlock()

	now := time.Now().Unix()
	windowStart := now - config.AppConfig.RateLimitWindow

	// Clean up old entries
	for fp, t := range lastRequestTimes {
		if t < windowStart {
			delete(lastRequestTimes, fp)
			delete(requestCounts, fp)
		}
	}

	// Update request count
	count := requestCounts[fingerprint] + 1
	requestCounts[fingerprint] = count
	lastRequestTimes[fingerprint] = now

	return count > int(config.AppConfig.RateLimitMax)
}

// hasTooManyFailedAttempts checks if a fingerprint has too many failed attempts
func hasTooManyFailedAttempts(fingerprint string) bool {
	mutex.Lock()
	defer mutex.Unlock()

	now := time.Now().Unix()
	windowStart := now - config.AppConfig.FailedAttemptWindow

	// Clean up old entries
	for fp, t := range lastFailedAttemptTime {
		if t < windowStart {
			delete(lastFailedAttemptTime, fp)
			delete(failedAttempts, fp)
		}
	}

	return failedAttempts[fingerprint] >= config.AppConfig.MaxFailedAttempts
}

// recordFailedAttempt records a failed attempt for a fingerprint
func recordFailedAttempt(fingerprint string) {
	mutex.Lock()
	defer mutex.Unlock()

	now := time.Now().Unix()
	attempts := failedAttempts[fingerprint] + 1
	failedAttempts[fingerprint] = attempts
	lastFailedAttemptTime[fingerprint] = now
	
	if attempts >= config.AppConfig.MaxFailedAttempts {
		log.Printf("Too many failed attempts for fingerprint: %s (%d)", fingerprint, attempts)
	}
}

// logFailedRequest logs a failed request
func logFailedRequest(request models.AppTokenRequest, clientIP, userAgent, reason string, isSuspectedBot bool) {
	failedLog := models.RequestLog{
		Fingerprint:    request.Fingerprint,
		DeviceId:       request.DeviceId,
		IpAddress:      clientIP,
		UserAgent:      userAgent,
		RequestType:    "TOKEN_REQUEST",
		IsSuccess:      false,
		FailureReason:  reason,
		Timestamp:      time.Now(),
		IsSuspectedBot: isSuspectedBot,
	}
	database.DB.Create(&failedLog)
	recordFailedAttempt(request.Fingerprint)
}

// abs returns the absolute value of an int64
func abs(n int64) int64 {
	if n < 0 {
		return -n
	}
	return n
}