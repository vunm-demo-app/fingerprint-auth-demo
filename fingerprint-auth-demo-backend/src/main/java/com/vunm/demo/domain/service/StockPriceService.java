package com.vunm.demo.domain.service;

import com.vunm.demo.domain.model.StockPrice;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StockPriceService {
    private final Map<String, StockPrice> stockPrices = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final List<String> symbols = Arrays.asList("VCB", "VNM", "VIC", "HPG", "MSN", "VHM", "CTG", "TCB", "BID", "FPT");

    public StockPriceService() {
        initializeStockPrices();
    }

    private void initializeStockPrices() {
        for (String symbol : symbols) {
            double refPrice = 20 + random.nextDouble() * 80;
            double ceilingPrice = refPrice * 1.07;
            double floorPrice = refPrice * 0.93;
            double matchPrice = refPrice + (random.nextDouble() - 0.5) * (ceilingPrice - floorPrice);
            double change = matchPrice - refPrice;
            double changePercent = (change / refPrice) * 100;
            long volume = 100000 + random.nextInt(900000);

            stockPrices.put(symbol, StockPrice.builder()
                    .symbol(symbol)
                    .refPrice(Math.round(refPrice * 100.0) / 100.0)
                    .ceilingPrice(Math.round(ceilingPrice * 100.0) / 100.0)
                    .floorPrice(Math.round(floorPrice * 100.0) / 100.0)
                    .matchPrice(Math.round(matchPrice * 100.0) / 100.0)
                    .change(Math.round(change * 100.0) / 100.0)
                    .changePercent(Math.round(changePercent * 100.0) / 100.0)
                    .volume(volume)
                    .build());
        }
    }

    public List<StockPrice> getAllStockPrices() {
        updatePrices();
        return new ArrayList<>(stockPrices.values());
    }

    public StockPrice getStockPrice(String symbol) {
        updatePrice(symbol);
        return stockPrices.get(symbol.toUpperCase());
    }

    private void updatePrices() {
        for (String symbol : symbols) {
            updatePrice(symbol);
        }
    }

    private void updatePrice(String symbol) {
        StockPrice currentPrice = stockPrices.get(symbol);
        if (currentPrice == null) return;

        double refPrice = currentPrice.getRefPrice();
        double ceilingPrice = currentPrice.getCeilingPrice();
        double floorPrice = currentPrice.getFloorPrice();
        
        // Generate new match price within ceiling and floor limits
        double currentMatch = currentPrice.getMatchPrice();
        double priceChange = (random.nextDouble() - 0.5) * 0.5; // Max 0.5% change
        double newMatch = Math.min(ceilingPrice, Math.max(floorPrice, currentMatch * (1 + priceChange)));
        
        double change = newMatch - refPrice;
        double changePercent = (change / refPrice) * 100;
        long volumeChange = random.nextInt(10000) - 5000;

        stockPrices.put(symbol, StockPrice.builder()
                .symbol(symbol)
                .refPrice(refPrice)
                .ceilingPrice(ceilingPrice)
                .floorPrice(floorPrice)
                .matchPrice(Math.round(newMatch * 100.0) / 100.0)
                .change(Math.round(change * 100.0) / 100.0)
                .changePercent(Math.round(changePercent * 100.0) / 100.0)
                .volume(Math.max(0, currentPrice.getVolume() + volumeChange))
                .build());
    }
} 