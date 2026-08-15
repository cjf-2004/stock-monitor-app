package com.stockmonitor.model; // 建议放在 model 包或者一个 utilities 包中

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PricePoint 类：用于记录股票在某个时间点上的价格。
 */
public class PricePoint {
    private LocalDateTime timestamp; // 记录时间点
    private BigDecimal price;        // 记录该时间点的价格

    public PricePoint(LocalDateTime timestamp, BigDecimal price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    // --- Getters ---
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "PricePoint{" +
               "timestamp=" + timestamp +
               ", price=" + price +
               '}';
    }
}
