package com.stockmonitor.service;
import com.stockmonitor.model.StockBase;
import java.util.List;

public interface StockMarket {
    boolean isTradingTime(); // 判断当前是否处于交易时间
    List<StockBase> getStocks(); // 提供股市上全部股票的价格信息，返回 List<StockBase>
}