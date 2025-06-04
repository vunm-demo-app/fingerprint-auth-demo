package com.vunm.demo.api.controller;

import com.vunm.demo.domain.model.StockPrice;
import com.vunm.demo.domain.service.StockPriceService;
import com.vunm.demo.domain.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stock-prices")
@RequiredArgsConstructor
public class StockPriceController {
    private final StockPriceService stockPriceService;
    private final TokenService tokenService;

    @GetMapping
    public ResponseEntity<?> getAllStockPrices(
            @RequestHeader("X-Fingerprint") String fingerprint,
            @RequestHeader("X-App-Token") String token) {
        
        log.debug("Received request for all stock prices. Fingerprint: {}", fingerprint);
        
        if (!tokenService.validateToken(token, fingerprint)) {
            log.warn("Invalid token for fingerprint: {}", fingerprint);
            return ResponseEntity.status(401).body("Invalid token");
        }

        List<StockPrice> prices = stockPriceService.getAllStockPrices();
        log.debug("Returning {} stock prices", prices.size());
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<?> getStockPrice(
            @PathVariable String symbol,
            @RequestHeader("X-Fingerprint") String fingerprint,
            @RequestHeader("X-App-Token") String token) {
        
        log.info("Received request for stock price. Symbol: {}, Fingerprint: {}", symbol, fingerprint);
        
        if (!tokenService.validateToken(token, fingerprint)) {
            log.warn("Invalid token for fingerprint: {}", fingerprint);
            return ResponseEntity.status(401).body("Invalid token");
        }

        StockPrice price = stockPriceService.getStockPrice(symbol);
        if (price == null) {
            log.warn("Stock not found: {}", symbol);
            return ResponseEntity.notFound().build();
        }
        
        log.info("Returning price for symbol: {}", symbol);
        return ResponseEntity.ok(price);
    }
} 