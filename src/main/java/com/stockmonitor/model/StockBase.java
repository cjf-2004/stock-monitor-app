package com.stockmonitor.model;

import java.math.BigDecimal;


public class StockBase {

    private String code; // Corresponds to stock_id in DB
    private String name;
    private MarketType marketType;
    private Boolean isSuspended;
    private BigDecimal currentPrice;
    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MarketType getMarketType() {
        return marketType;
    }

    public void setMarketType(MarketType marketType) {
        this.marketType = marketType;
    }

    public Boolean getSuspended() {
        return isSuspended;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
     public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setSuspended(Boolean suspended) {
        isSuspended = suspended;
    }
}