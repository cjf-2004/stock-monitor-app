package com.stockmonitor.entity;


import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_realtime_data")
@IdClass(StockRealtimeData.StockRealtimeDataId.class)
public class StockRealtimeData {
    @Id
    @Column(name = "stock_id", length = 6)
    private String stockId;
    @Id
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    @Column(name = "highest_price", precision = 10, scale = 2)
    private BigDecimal highestPrice;
    @Column(name = "lowest_price", precision = 10, scale = 2)
    private BigDecimal lowestPrice;
    @Column(name = "change_percent", precision = 5, scale = 2)
    private BigDecimal changePercent;
    @Column(name = "amplitude", precision = 5, scale = 2)
    private BigDecimal amplitude;
    @Column(name = "velocity", precision = 5, scale = 2)
    private BigDecimal velocity;

    // private StockBase stockBase; // Relationship to StockBase

    // Getters and Setters

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(BigDecimal highestPrice) {
        this.highestPrice = highestPrice;
    }

    public BigDecimal getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(BigDecimal lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }

    public BigDecimal getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(BigDecimal amplitude) {
        this.amplitude = amplitude;
    }

    public BigDecimal getVelocity() {
        return velocity;
    }

    public void setVelocity(BigDecimal velocity) {
        this.velocity = velocity;
    }


    // Composite primary key for StockRealtimeData
    public static class StockRealtimeDataId implements Serializable {
        private String stockId;
        private LocalDateTime timestamp;

        // hashCode and equals
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StockRealtimeDataId that = (StockRealtimeDataId) o;
            return stockId.equals(that.stockId) &&
                    timestamp.equals(that.timestamp);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(stockId, timestamp);
        }
    }
}