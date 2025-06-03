package services

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"

	"github.com/vunm/fingerprint-auth-demo-backend-go/config"
)

// FingerprintAPIResponse represents the response from the FingerprintJS Pro API
type FingerprintAPIResponse struct {
	Products struct {
		Botd struct {
			Data struct {
				Bot struct {
					Probability float64 `json:"probability"`
					Type        string  `json:"type"`
				} `json:"bot"`
			} `json:"data"`
		} `json:"botd"`
	} `json:"products"`
}

// GetVisitorInfo gets visitor information from the FingerprintJS Pro API
func GetVisitorInfo(visitorID string) (map[string]interface{}, error) {
	result := make(map[string]interface{})

	// Check if API key is configured
	if config.AppConfig.FingerprintApiKey == "" {
		log.Println("FingerprintJS Pro API key not configured, skipping API call")
		return result, nil
	}

	// Build the API URL
	url := fmt.Sprintf("%s/visitors/%s?region=%s", 
		config.AppConfig.FingerprintApiUrl, 
		visitorID, 
		config.AppConfig.FingerprintApiRegion)

	// Create the request
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("error creating request: %w", err)
	}

	// Add headers
	req.Header.Set("Auth-API-Key", config.AppConfig.FingerprintApiKey)
	req.Header.Set("Content-Type", "application/json")

	// Send the request
	client := &http.Client{}
	log.Printf("Calling FingerprintJS Pro API for visitor: %s", visitorID)
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("error calling FingerprintJS Pro API: %w", err)
	}
	defer resp.Body.Close()

	// Check response status
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("FingerprintJS Pro API returned status: %d", resp.StatusCode)
	}

	// Read response body
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("error reading response body: %w", err)
	}

	// Parse response
	var apiResponse FingerprintAPIResponse
	err = json.Unmarshal(body, &apiResponse)
	if err != nil {
		return nil, fmt.Errorf("error parsing response: %w", err)
	}

	// Extract bot detection data
	botProbability := apiResponse.Products.Botd.Data.Bot.Probability
	botType := apiResponse.Products.Botd.Data.Bot.Type
	isBot := botProbability > 0.8 // Consider as bot if probability > 80%

	result["botProbability"] = botProbability
	result["botType"] = botType
	result["isBot"] = isBot
	result["rawResponse"] = string(body)

	log.Printf("Bot detection for visitor %s: probability=%f, type=%s", 
		visitorID, botProbability, botType)

	return result, nil
}