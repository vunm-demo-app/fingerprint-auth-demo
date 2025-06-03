package middleware

import (
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/vunm/fingerprint-auth-demo-backend-go/config"
)

// CorsMiddleware returns a CORS middleware
func CorsMiddleware() gin.HandlerFunc {
	return cors.New(cors.Config{
		AllowOrigins:     config.AppConfig.CorsAllowedOrigins,
		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Type", "Accept", "X-Fingerprint", "X-App-Token"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
	})
}