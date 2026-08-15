package com.stockmonitor.model;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;

public class Stock {
    private StockBase base; // The base information for the stock
    private BigDecimal prevClosePrice;
    private BigDecimal openPrice;
    private BigDecimal velocity;
    private BigDecimal amplitude;
    private BigDecimal changeRate;
    private BigDecimal lowPrice;
    private BigDecimal highPrice;
    private Deque<PricePoint> vWindow =new ArrayDeque<>(); 

    public Stock(StockBase base) {
        this.base = base;
    }

    // Getters and Setters
    public StockBase getBase() {
        return base;
    }

    public void setBase(StockBase base) {
        this.base = base;
    }

    public BigDecimal getPrevClosePrice() {
        return prevClosePrice;
    }

    public void setPrevClosePrice(BigDecimal prevClosePrice) {
        this.prevClosePrice = prevClosePrice;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public BigDecimal getVelocity() {
        return velocity;
    }

    public void setVelocity(BigDecimal velocity) {
        this.velocity = velocity;
    }

    public BigDecimal getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(BigDecimal amplitude) {
        this.amplitude = amplitude;
    }

    public BigDecimal getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(BigDecimal changeRate) {
        this.changeRate = changeRate;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }
        // Getter for vWindow 队列
    public Deque<PricePoint> getvWindow() {
        return vWindow;
    }

    public BigDecimal getCurrentPrice(){
        return base.getCurrentPrice();
    }
    public void setCurrentPrice(BigDecimal currentPrice){
        base.setCurrentPrice(currentPrice);
    }
    public Boolean getSuspended(){
        return base.getSuspended();
    }

    public String getStockId(){
        return base.getCode();
    }
}