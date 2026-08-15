package com.stockmonitor.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stock_daily_data")
@IdClass(StockDailyData.StockDailyDataId.class)
public class StockDailyData {
    @Id
    @Column(name = "stock_id", length = 6)
    private String stockId;
    @Id
    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @Column(name = "open_price", precision = 10, scale = 2)
    private BigDecimal openPrice;
    @Column(name = "close_price", precision = 10, scale = 2)
    private BigDecimal closePrice;
    @Column(name = "prev_close_price", precision = 10, scale = 2)
    private BigDecimal prevClosePrice;
    @Column(name = "daily_highest_price", precision = 10, scale = 2)
    private BigDecimal dailyHighestPrice;
    @Column(name = "daily_lowest_price", precision = 10, scale = 2)
    private BigDecimal dailyLowestPrice;
    @Column(name = "daily_change_percent", precision = 5, scale = 2)
    private BigDecimal dailyChangePercent;
    @Column(name = "daily_amplitude", precision = 5, scale = 2)
    private BigDecimal dailyAmplitude;
    @Column(name = "is_suspended", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSuspended;

    // @ManyToOne
    // @JoinColumn(name = "stock_id", referencedColumnName = "stock_id", insertable = false, updatable = false)
    // private StockBase stockBase; // Relationship to StockBase

    // Getters and Setters

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public BigDecimal getPrevClosePrice() {
        return prevClosePrice;
    }

    public void setPrevClosePrice(BigDecimal prevClosePrice) {
        this.prevClosePrice = prevClosePrice;
    }

    public BigDecimal getDailyHighestPrice() {
        return dailyHighestPrice;
    }

    public void setDailyHighestPrice(BigDecimal dailyHighestPrice) {
        this.dailyHighestPrice = dailyHighestPrice;
    }

    public BigDecimal getDailyLowestPrice() {
        return dailyLowestPrice;
    }

    public void setDailyLowestPrice(BigDecimal dailyLowestPrice) {
        this.dailyLowestPrice = dailyLowestPrice;
    }

    public BigDecimal getDailyChangePercent() {
        return dailyChangePercent;
    }

    public void setDailyChangePercent(BigDecimal dailyChangePercent) {
        this.dailyChangePercent = dailyChangePercent;
    }

    public BigDecimal getDailyAmplitude() {
        return dailyAmplitude;
    }

    public void setDailyAmplitude(BigDecimal dailyAmplitude) {
        this.dailyAmplitude = dailyAmplitude;
    }

    public Boolean getSuspended() {
        return isSuspended;
    }

    public void setSuspended(Boolean suspended) {
        isSuspended = suspended;
    }


    // Composite primary key for StockDailyData
    public static class StockDailyDataId implements Serializable {
        private String stockId;
        private LocalDate tradeDate;

        // hashCode and equals
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StockDailyDataId that = (StockDailyDataId) o;
            return stockId.equals(that.stockId) &&
                    tradeDate.equals(that.tradeDate);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(stockId, tradeDate);
        }
    }
}