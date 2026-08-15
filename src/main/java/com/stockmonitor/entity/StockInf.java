package com.stockmonitor.entity; // 建议放在你的实体类包中

import com.stockmonitor.model.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 定义市场类型的枚举
// 对应数据库中的 ENUM('MAIN_BOARD', 'SCI_TECH', 'GROWTH')


/**
 * Stock 实体类，映射到 'stocks' 表。
 * 包含股票的基本信息。
 */
@Entity // 标记这是一个JPA实体
@Table(name = "stocks") // 映射到数据库中的 'stocks' 表
public class StockInf {

    @Id // 标记为实体的主键
    @Column(name = "stock_id", length = 6) // 映射到 'stock_id' 列，长度为6
    private String stockId; // 对应数据库中的 stock_id

    @Column(name = "stock_name", length = 20, nullable = false) // 映射到 'stock_name' 列，长度20，不可为空
    private String stockName; // 对应数据库中的 stock_name

    @Enumerated(EnumType.STRING) // 指定枚举以字符串形式存储到数据库
    @Column(name = "market_type", nullable = false) // 映射到 'market_type' 列，不可为空
    private MarketType marketType; // 对应数据库中的 market_type

    @Column(name = "is_suspended", columnDefinition = "BOOLEAN DEFAULT FALSE") // 映射到 'is_suspended' 列，默认值为 FALSE
    private Boolean isSuspended; // 对应数据库中的 is_suspended

    // JPA 规范要求实体类必须有一个公共的无参构造函数
    public StockInf() {
    }

    // 完整的构造函数，方便创建 Stock 对象
    public  StockInf(String stockId, String stockName, MarketType marketType, Boolean isSuspended) {
        this.stockId = stockId;
        this.stockName = stockName;
        this.marketType = marketType;
        this.isSuspended = isSuspended;
    }

    // --- Getters 和 Setters ---

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public MarketType getMarketType() {
        return marketType;
    }

    public void setMarketType(MarketType marketType) {
        this.marketType = marketType;
    }

    public Boolean getIsSuspended() {
        return isSuspended;
    }

    public void setIsSuspended(Boolean suspended) {
        isSuspended = suspended;
    }

    @Override
    public String toString() {
        return "Stock{" +
               "stockId='" + stockId + '\'' +
               ", stockName='" + stockName + '\'' +
               ", marketType=" + marketType +
               ", isSuspended=" + isSuspended +
               '}';
    }
}