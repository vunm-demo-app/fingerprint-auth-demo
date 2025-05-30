package middleware

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/vunm/fingerprint-auth-demo-backend-go/services"
)

// AuthMiddleware is a middleware that checks if the request is authenticated
func AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		fingerprint := c.GetHeader("X-Fingerprint")
		token := c.GetHeader("X-App-Token")

		if fingerprint == "" || token == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Missing authentication"})
			c.Abort()
			return
		}

		if !services.ValidateToken(token, fingerprint) {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid token"})
			c.Abort()
			return
		}

		c.Next()
	}
}