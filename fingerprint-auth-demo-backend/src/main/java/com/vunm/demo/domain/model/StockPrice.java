package com.vunm.demo.domain.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPrice {
    private String symbol;
    private double refPrice;
    private double ceilingPrice;
    private double floorPrice;
    private double matchPrice;
    private double change;
    private double changePercent;
    private long volume;
} 