package main

import (
	"fmt"
	"log"

	"github.com/gin-gonic/gin"
	"github.com/vunm/fingerprint-auth-demo-backend-go/config"
	"github.com/vunm/fingerprint-auth-demo-backend-go/controllers"
	"github.com/vunm/fingerprint-auth-demo-backend-go/database"
	"github.com/vunm/fingerprint-auth-demo-backend-go/middleware"
)

func main() {
	// Load configuration
	config.LoadConfig()

	// Set Gin mode
	gin.SetMode(config.AppConfig.GinMode)

	// Initialize database
	database.InitDB()

	// Create router
	r := gin.Default()

	// Apply CORS middleware
	r.Use(middleware.CorsMiddleware())

	// Health check endpoint
	r.GET("/health", controllers.HealthCheck)

	// API routes
	api := r.Group("/api")
	{
		// Public routes
		api.GET("/time", controllers.GetTime)
		api.POST("/app-token", controllers.GetToken)

		// Protected routes
		protected := api.Group("/")
		protected.Use(middleware.AuthMiddleware())
		{
			protected.GET("/stock-prices", controllers.GetAllStocks)
			protected.GET("/stock-prices/:symbol", controllers.GetStock)
		}

		// Admin routes
		admin := api.Group("/admin")
		admin.Use(middleware.AuthMiddleware())
		{
			admin.GET("/statistics", controllers.GetStatistics)
			admin.GET("/logs", controllers.GetLogs)
			admin.GET("/correlation", controllers.GetIpFingerprintCorrelation)
		}
	}

	// Start server
	port := fmt.Sprintf(":%d", config.AppConfig.Port)
	log.Printf("Starting server on port %s", port)
	if err := r.Run(port); err != nil {
		log.Fatalf("Failed to start server: %v", err)
	}
}