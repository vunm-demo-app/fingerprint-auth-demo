package controllers

import (
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/vunm/fingerprint-auth-demo-backend-go/services"
)

// GetStatistics returns statistics for a time range
func GetStatistics(c *gin.Context) {
	fromStr := c.Query("from")
	toStr := c.Query("to")

	var from, to time.Time
	var err error

	if fromStr != "" {
		from, err = time.Parse(time.RFC3339, fromStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid from date format"})
			return
		}
	} else {
		// Default to 7 days ago
		from = time.Now().AddDate(0, 0, -7)
	}

	if toStr != "" {
		to, err = time.Parse(time.RFC3339, toStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid to date format"})
			return
		}
	} else {
		// Default to now
		to = time.Now()
	}

	stats, err := services.GetStatistics(from, to)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get statistics"})
		return
	}

	c.JSON(http.StatusOK, stats)
}

// GetLogs returns request logs filtered by parameters
func GetLogs(c *gin.Context) {
	fromStr := c.Query("from")
	toStr := c.Query("to")
	fingerprint := c.Query("fingerprint")
	ipAddress := c.Query("ipAddress")
	isSuspectedBotStr := c.Query("isSuspectedBot")
	pageStr := c.DefaultQuery("page", "1")
	sizeStr := c.DefaultQuery("size", "10")

	var from, to time.Time
	var err error

	if fromStr != "" {
		from, err = time.Parse(time.RFC3339, fromStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid from date format"})
			return
		}
	} else {
		// Default to 7 days ago
		from = time.Now().AddDate(0, 0, -7)
	}

	if toStr != "" {
		to, err = time.Parse(time.RFC3339, toStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid to date format"})
			return
		}
	} else {
		// Default to now
		to = time.Now()
	}

	var isSuspectedBot *bool
	if isSuspectedBotStr != "" {
		boolValue, err := strconv.ParseBool(isSuspectedBotStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid isSuspectedBot value"})
			return
		}
		isSuspectedBot = &boolValue
	}

	page, err := strconv.Atoi(pageStr)
	if err != nil || page < 1 {
		page = 1
	}

	size, err := strconv.Atoi(sizeStr)
	if err != nil || size < 1 || size > 100 {
		size = 10
	}

	logs, total, err := services.GetRequestLogs(from, to, fingerprint, ipAddress, isSuspectedBot, page, size)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get logs"})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"content": logs,
		"page":    page,
		"size":    size,
		"total":   total,
	})
}

// GetIpFingerprintCorrelation returns the correlation between IP addresses and fingerprints
func GetIpFingerprintCorrelation(c *gin.Context) {
	correlations, err := services.GetIpFingerprintCorrelation()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get correlation data"})
		return
	}

	c.JSON(http.StatusOK, correlations)
}