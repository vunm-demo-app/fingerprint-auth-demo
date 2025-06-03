package database

import (
	"log"
	"os"
	"path/filepath"

	"github.com/glebarez/sqlite" // Thay đổi import này
	"github.com/vunm/fingerprint-auth-demo-backend-go/models"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var DB *gorm.DB

// InitDB initializes the database connection
func InitDB() {
	// Create data directory if it doesn't exist
	dataDir := "./data"
	if _, err := os.Stat(dataDir); os.IsNotExist(err) {
		err := os.MkdirAll(dataDir, 0755)
		if err != nil {
			log.Fatalf("Failed to create data directory: %v", err)
		}
	}

	dbPath := filepath.Join(dataDir, "fingerprint.db")
	db, err := gorm.Open(sqlite.Open(dbPath), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Info),
	})

	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}

	// Auto migrate the schema
	err = db.AutoMigrate(
		&models.FingerprintDetails{},
		&models.RequestLog{},
		&models.StockPrice{},
	)

	if err != nil {
		log.Fatalf("Failed to migrate database: %v", err)
	}

	// Seed stock data if empty
	var count int64
	db.Model(&models.StockPrice{}).Count(&count)
	if count == 0 {
		seedStockData(db)
	}

	DB = db
	log.Println("Database initialized successfully")
}

// seedStockData seeds initial stock data
func seedStockData(db *gorm.DB) {
	stocks := []models.StockPrice{
		{
			Symbol:        "AAPL",
			RefPrice:      180.5,
			CeilingPrice:  190.0,
			FloorPrice:    170.0,
			MatchPrice:    182.3,
			Change:        1.8,
			ChangePercent: 1.0,
			Volume:        1500000,
		},
		{
			Symbol:        "MSFT",
			RefPrice:      320.75,
			CeilingPrice:  330.0,
			FloorPrice:    310.0,
			MatchPrice:    325.5,
			Change:        4.75,
			ChangePercent: 1.48,
			Volume:        980000,
		},
		{
			Symbol:        "GOOGL",
			RefPrice:      135.2,
			CeilingPrice:  140.0,
			FloorPrice:    130.0,
			MatchPrice:    134.8,
			Change:        -0.4,
			ChangePercent: -0.3,
			Volume:        750000,
		},
		{
			Symbol:        "AMZN",
			RefPrice:      145.8,
			CeilingPrice:  150.0,
			FloorPrice:    140.0,
			MatchPrice:    147.2,
			Change:        1.4,
			ChangePercent: 0.96,
			Volume:        1200000,
		},
		{
			Symbol:        "META",
			RefPrice:      310.5,
			CeilingPrice:  320.0,
			FloorPrice:    300.0,
			MatchPrice:    308.2,
			Change:        -2.3,
			ChangePercent: -0.74,
			Volume:        850000,
		},
	}

	for _, stock := range stocks {
		db.Create(&stock)
	}

	log.Println("Stock data seeded successfully")
}