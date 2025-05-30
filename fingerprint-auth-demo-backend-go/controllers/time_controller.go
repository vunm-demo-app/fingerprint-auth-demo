package controllers

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/vunm/fingerprint-auth-demo-backend-go/models"
)

// GetTime returns the current server time
func GetTime(c *gin.Context) {
	c.JSON(http.StatusOK, models.TimeResponse{
		ServerTime: time.Now().Unix(),
	})
}