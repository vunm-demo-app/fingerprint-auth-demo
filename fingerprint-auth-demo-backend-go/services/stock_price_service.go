package services

import (
	"errors"

	"github.com/vunm/fingerprint-auth-demo-backend-go/database"
	"github.com/vunm/fingerprint-auth-demo-backend-go/models"
	"gorm.io/gorm"
)

// GetAllStocks returns all stock prices
func GetAllStocks() ([]models.StockPrice, error) {
	var stocks []models.StockPrice
	result := database.DB.Find(&stocks)
	return stocks, result.Error
}

// GetStock returns a stock price by symbol
func GetStock(symbol string) (*models.StockPrice, error) {
	var stock models.StockPrice
	result := database.DB.Where("symbol = ?", symbol).First(&stock)
	if result.Error != nil {
		if errors.Is(result.Error, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, result.Error
	}
	return &stock, nil
}

// UpdateStock updates a stock price
func UpdateStock(stock *models.StockPrice) error {
	return database.DB.Save(stock).Error
}

// CreateStock creates a new stock price
func CreateStock(stock *models.StockPrice) error {
	return database.DB.Create(stock).Error
}

// DeleteStock deletes a stock price
func DeleteStock(symbol string) error {
	return database.DB.Where("symbol = ?", symbol).Delete(&models.StockPrice{}).Error
}