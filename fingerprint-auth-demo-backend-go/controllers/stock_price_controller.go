package controllers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/vunm/fingerprint-auth-demo-backend-go/services"
)

// GetAllStocks returns all stock prices
func GetAllStocks(c *gin.Context) {
	stocks, err := services.GetAllStocks()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get stocks"})
		return
	}

	c.JSON(http.StatusOK, stocks)
}

// GetStock returns a stock price by symbol
func GetStock(c *gin.Context) {
	symbol := c.Param("symbol")
	stock, err := services.GetStock(symbol)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get stock"})
		return
	}

	if stock == nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Stock not found"})
		return
	}

	c.JSON(http.StatusOK, stock)
}