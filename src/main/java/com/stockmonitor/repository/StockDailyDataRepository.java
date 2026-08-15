package com.stockmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stockmonitor.entity.StockDailyData;

import java.time.LocalDate;
import java.util.List; // 添加 List 导入
import java.util.Optional;

@Repository
public interface StockDailyDataRepository extends JpaRepository<StockDailyData, StockDailyData.StockDailyDataId> {
    Optional<StockDailyData> findByStockIdAndTradeDate(String stockId, LocalDate tradeDate);
    List<StockDailyData> findByStockIdOrderByTradeDateAsc(String stockId); // 新增：按股票ID查询并按日期升序排序
}