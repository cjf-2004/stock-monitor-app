package com.stockmonitor.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stockmonitor.entity.StockRealtimeData;

import java.time.LocalDateTime;
import java.util.List; // 添加 List 导入

@Repository
public interface StockRealtimeDataRepository extends JpaRepository<StockRealtimeData, StockRealtimeData.StockRealtimeDataId> {
    List<StockRealtimeData> findByStockIdAndTimestampBetween(String stockId, LocalDateTime startTime, LocalDateTime endTime);
    List<StockRealtimeData> findByStockIdOrderByTimestampDesc(String stockId); // 新增：按股票ID查询并按时间戳降序排序
}