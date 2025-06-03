package controllers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/vunm/fingerprint-auth-demo-backend-go/models"
	"github.com/vunm/fingerprint-auth-demo-backend-go/services"
	"github.com/vunm/fingerprint-auth-demo-backend-go/utils"
)

// GetToken handles the token generation request
func GetToken(c *gin.Context) {
	var request models.AppTokenRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request"})
		return
	}

	clientIP := utils.GetClientIP(c)
	userAgent := c.GetHeader("User-Agent")

	token, err := services.GenerateTokenIfValid(request, clientIP, userAgent)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate token"})
		return
	}

	if token == nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request"})
		return
	}

	c.JSON(http.StatusOK, token)
}