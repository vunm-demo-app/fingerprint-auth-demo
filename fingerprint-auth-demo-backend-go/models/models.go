package models

import (
	"time"

	"gorm.io/gorm"
)

// FingerprintDetails represents the fingerprint information
type FingerprintDetails struct {
	gorm.Model
	Fingerprint        string  `gorm:"uniqueIndex"`
	Components         string  `gorm:"type:text"`
	UserAgent          string
	Platform           string
	ScreenResolution   string
	Timezone           string
	Language           string
	WebglSupported     bool
	WebglRenderer      string
	WebglVendor        string
	CpuCores           string
	DeviceMemory       string
	HardwareConcurrency string
	TouchSupport       string
	ColorDepth         string
	PixelRatio         string
	Fonts              string
	Audio              string
	Canvas             string
	BotProbability     float64
	BotType            string
	IsBot              bool
	FirstSeenAt        int64
	LastSeenAt         int64
	ConsistencyScore   int
}

// RequestLog represents a log of API requests
type RequestLog struct {
	gorm.Model
	Fingerprint    string
	DeviceId       string
	IpAddress      string
	UserAgent      string
	RequestType    string
	IsSuccess      bool
	FailureReason  string
	IsSuspectedBot bool
	Timestamp      time.Time
}

// AppTokenRequest represents a request for an application token
type AppTokenRequest struct {
	DeviceId    string                 `json:"deviceId" binding:"required"`
	Fingerprint string                 `json:"fingerprint" binding:"required"`
	Timestamp   int64                  `json:"timestamp" binding:"required"`
	Components  map[string]interface{} `json:"components" binding:"required"`
}

// AppToken represents an application token
type AppToken struct {
	Token       string `json:"token"`
	Fingerprint string `json:"fingerprint"`
	ExpiresAt   int64  `json:"expiresAt"`
}

// StockPrice represents stock price information
type StockPrice struct {
	gorm.Model
	Symbol        string  `json:"symbol" gorm:"uniqueIndex"`
	RefPrice      float64 `json:"refPrice"`
	CeilingPrice  float64 `json:"ceilingPrice"`
	FloorPrice    float64 `json:"floorPrice"`
	MatchPrice    float64 `json:"matchPrice"`
	Change        float64 `json:"change"`
	ChangePercent float64 `json:"changePercent"`
	Volume        int64   `json:"volume"`
}

// TimeResponse represents the server time response
type TimeResponse struct {
	ServerTime int64 `json:"serverTime"`
}

// StatisticsResponse represents the statistics response
type StatisticsResponse struct {
	TotalRequests     int64 `json:"totalRequests"`
	UniqueIps         int   `json:"uniqueIps"`
	UniqueFingerprints int   `json:"uniqueFingerprints"`
	BotAttempts       int64 `json:"botAttempts"`
	FailedRequests    int64 `json:"failedRequests"`
}

// IpFingerprintCorrelation represents the correlation between IP addresses and fingerprints
type IpFingerprintCorrelation struct {
	Fingerprint  string   `json:"fingerprint"`
	Ips          []string `json:"ips"`
	RequestCount int      `json:"requestCount"`
	IpCount      int      `json:"ipCount"`
}