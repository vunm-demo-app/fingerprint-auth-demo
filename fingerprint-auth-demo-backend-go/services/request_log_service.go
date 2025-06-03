package services

import (
	"time"

	"github.com/vunm/fingerprint-auth-demo-backend-go/database"
	"github.com/vunm/fingerprint-auth-demo-backend-go/models"
)

// GetStatistics returns statistics for a time range
func GetStatistics(from, to time.Time) (*models.StatisticsResponse, error) {
	var totalRequests int64
	var botAttempts int64
	var failedRequests int64

	// Count total requests
	database.DB.Model(&models.RequestLog{}).
		Where("timestamp BETWEEN ? AND ?", from, to).
		Count(&totalRequests)

	// Count bot attempts
	database.DB.Model(&models.RequestLog{}).
		Where("timestamp BETWEEN ? AND ? AND is_suspected_bot = ?", from, to, true).
		Count(&botAttempts)

	// Count failed requests
	database.DB.Model(&models.RequestLog{}).
		Where("timestamp BETWEEN ? AND ? AND is_success = ?", from, to, false).
		Count(&failedRequests)

	// Get unique IPs
	var ipAddresses []string
	database.DB.Model(&models.RequestLog{}).
		Where("timestamp BETWEEN ? AND ?", from, to).
		Distinct().
		Pluck("ip_address", &ipAddresses)

	// Get unique fingerprints
	var fingerprints []string
	database.DB.Model(&models.RequestLog{}).
		Where("timestamp BETWEEN ? AND ?", from, to).
		Distinct().
		Pluck("fingerprint", &fingerprints)

	return &models.StatisticsResponse{
		TotalRequests:     totalRequests,
		UniqueIps:         len(ipAddresses),
		UniqueFingerprints: len(fingerprints),
		BotAttempts:       botAttempts,
		FailedRequests:    failedRequests,
	}, nil
}

// GetRequestLogs returns request logs filtered by parameters
func GetRequestLogs(from, to time.Time, fingerprint, ipAddress string, isSuspectedBot *bool, page, pageSize int) ([]models.RequestLog, int64, error) {
	var logs []models.RequestLog
	var total int64

	query := database.DB.Model(&models.RequestLog{}).
		Where("timestamp BETWEEN ? AND ?", from, to)

	if fingerprint != "" {
		query = query.Where("fingerprint = ?", fingerprint)
	}

	if ipAddress != "" {
		query = query.Where("ip_address = ?", ipAddress)
	}

	if isSuspectedBot != nil {
		query = query.Where("is_suspected_bot = ?", *isSuspectedBot)
	}

	// Count total matching records
	query.Count(&total)

	// Get paginated results
	offset := (page - 1) * pageSize
	err := query.Offset(offset).Limit(pageSize).Order("timestamp desc").Find(&logs).Error

	return logs, total, err
}

// GetIpFingerprintCorrelation returns the correlation between IP addresses and fingerprints
func GetIpFingerprintCorrelation() ([]models.IpFingerprintCorrelation, error) {
	var logs []models.RequestLog
	if err := database.DB.Find(&logs).Error; err != nil {
		return nil, err
	}

	// Group logs by fingerprint
	fingerprintMap := make(map[string][]models.RequestLog)
	for _, log := range logs {
		fingerprintMap[log.Fingerprint] = append(fingerprintMap[log.Fingerprint], log)
	}

	// Create correlation data
	var correlations []models.IpFingerprintCorrelation
	for fingerprint, fpLogs := range fingerprintMap {
		// Get unique IPs
		ipMap := make(map[string]bool)
		for _, log := range fpLogs {
			ipMap[log.IpAddress] = true
		}

		// Extract unique IPs
		var ips []string
		for ip := range ipMap {
			ips = append(ips, ip)
		}

		correlations = append(correlations, models.IpFingerprintCorrelation{
			Fingerprint:  fingerprint,
			Ips:          ips,
			RequestCount: len(fpLogs),
			IpCount:      len(ips),
		})
	}

	return correlations, nil
}