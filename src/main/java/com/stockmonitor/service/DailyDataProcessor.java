package com.stockmonitor.service;

import com.stockmonitor.model.StockBase;
import com.stockmonitor.entity.*;
import com.stockmonitor.model.Stock;
import com.stockmonitor.repository.StockDailyDataRepository;
import com.stockmonitor.repository.StockInfRepository;
import com.stockmonitor.repository.StockRealtimeDataRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator; // 导入 Comparator

@Service
public class DailyDataProcessor {

    private final StockDailyDataRepository stockDailyDataRepository;
    private final StockRealtimeDataRepository stockRealtimeDataRepository;
    private final StockCalculator stockCalculator;
    private final StockInfRepository stockInfRepository;

    public DailyDataProcessor(
                              StockDailyDataRepository stockDailyDataRepository,
                              StockRealtimeDataRepository stockRealtimeDataRepository,
                              StockCalculator stockCalculator,
                              StockInfRepository stockInfRepository) {
        this.stockDailyDataRepository = stockDailyDataRepository;
        this.stockRealtimeDataRepository = stockRealtimeDataRepository;
        this.stockCalculator = stockCalculator;
        this.stockInfRepository = stockInfRepository;
    }

    public void updatePrevCloseAndOpen(Map<String, Stock> currentStockStates) { // 参数类型保持 Stock
        System.out.println("运行 updatePrevCloseAndOpen 于: " + LocalDateTime.now());
        LocalDate today = LocalDate.now();

        // 找到前一个交易日
        LocalDate prevTradingDay = getPreviousTradingDay(today);

        List<StockInf> allStocks = stockInfRepository.findAll();
        for (StockInf stockInf : allStocks) { // 使用 stockInf 作为 StockInf 对象
            String stockId = stockInf.getStockId();

            Optional<StockDailyData> prevDayDataOptional = stockDailyDataRepository.findByStockIdAndTradeDate(stockId, prevTradingDay);
            BigDecimal prevClosePrice = prevDayDataOptional.map(StockDailyData::getClosePrice).orElse(null);

            // 创建或更新今天的日线数据
            StockDailyData currentDailyData = stockDailyDataRepository.findByStockIdAndTradeDate(stockId, today)
                    .orElse(new StockDailyData());

            currentDailyData.setStockId(stockId);
            currentDailyData.setTradeDate(today);
            currentDailyData.setPrevClosePrice(prevClosePrice);

            // --- 修改这里，安全地从 currentStockStates 获取数据 ---
            // 使用 Optional.ofNullable 来包装 Map 的返回值，避免 NullPointerException
            Optional.ofNullable(currentStockStates.get(stockId))
                    .ifPresent(stockFromMap -> {
                        // 只有当 Map 中存在该股票的 Stock 对象时，才设置开盘价和停牌状态
                        // 这里的 stockFromMap 就是从 Map 中取出的 Stock 对象
                        currentDailyData.setOpenPrice(stockFromMap.getCurrentPrice()); 
                        currentDailyData.setSuspended(stockFromMap.getSuspended()); // 同步停牌状态
                        System.out.println("完成 OpenPrice Set。");
                    });
            // 如果 currentStockStates.get(stockId) 返回 null，则 ifPresent 中的代码块不会执行
            // 这意味着 currentDailyData 的 openPrice 和 suspended 字段将保持为 null (或其默认初始化值)

            stockDailyDataRepository.save(currentDailyData);
        }
        System.out.println("完成 updatePrevCloseAndOpen。");
    }


    public void updateDailyData(Map<String, Stock> currentStockStates) { // 参数类型保持 Stock
        System.out.println("运行 updateDailyData 于: " + LocalDateTime.now());
        LocalDate today = LocalDate.now();

        List<StockInf> allStocks = stockInfRepository.findAll();
        for (StockInf stockInf : allStocks) { // 使用 stockInf 作为 StockInf 对象
            String stockId = stockInf.getStockId();

            Optional<StockDailyData> todayDataOptional = stockDailyDataRepository.findByStockIdAndTradeDate(stockId, today);

            if (todayDataOptional.isPresent()) {
                StockDailyData todayData = todayDataOptional.get();

                Optional<Stock> optionalStockFromMap = Optional.ofNullable(currentStockStates.get(stockId));

                // 从内存中的 Stock 对象获取最终的收盘价、最高价、最低价
                // 如果 optionalStockFromMap 为空，则用开盘价或前收盘价作为兜底
                BigDecimal closePrice = optionalStockFromMap.map(Stock::getCurrentPrice)
                                        .orElse(todayData.getOpenPrice() != null ? todayData.getOpenPrice() : todayData.getPrevClosePrice());
                // 这里需要确保 Stock 对象中有 getHighPrice() 和 getLowPrice() 方法
                BigDecimal dailyHighestPrice = optionalStockFromMap.map(Stock::getHighPrice) // 假设 Stock 有 getHighPrice()
                                                .orElse(closePrice); 
                BigDecimal dailyLowestPrice = optionalStockFromMap.map(Stock::getLowPrice) // 假设 Stock 有 getLowPrice()
                                               .orElse(closePrice); 

                todayData.setClosePrice(closePrice);
                todayData.setDailyHighestPrice(dailyHighestPrice);
                todayData.setDailyLowestPrice(dailyLowestPrice);
                // 更新停牌状态：优先从实时数据，否则从 StockInf
                todayData.setSuspended(optionalStockFromMap.map(Stock::getSuspended) // 假设 Stock 有 getSuspended()
                                                     .orElse(stockInf.getIsSuspended()));

                // 创建临时 Stock 对象用于计算
                // 注意：如果你的 Stock 对象没有无参构造函数，或者需要更多初始化参数，这里需要调整
                Stock tempStock = new Stock(new StockBase()); // 假设 Stock 有无参构造函数
                // 确保 tempStock 能够被 stockCalculator 正确使用
                // 如果 Stock 内部有 StockBase，你需要设置它，或者直接在 Stock 上设置 price, high/low, suspended
                // 这里我假设 Stock 自身有这些字段
                // tempStock.setCode(stockId); // 如果 Stock 有 setCode 方法
                // tempStock.setName(stockInf.getStockName()); // 如果 Stock 有 setName 方法

                tempStock.setPrevClosePrice(todayData.getPrevClosePrice());
                tempStock.setOpenPrice(todayData.getOpenPrice());
                tempStock.setCurrentPrice(closePrice); // 使用收盘价进行日线指标计算
                tempStock.setHighPrice(dailyHighestPrice);
                tempStock.setLowPrice(dailyLowestPrice);

                stockCalculator.calculateChangeRate(tempStock);
                stockCalculator.calculateAmplitude(tempStock);

                todayData.setDailyChangePercent(tempStock.getChangeRate());
                todayData.setDailyAmplitude(tempStock.getAmplitude());

                stockDailyDataRepository.save(todayData);
            }
        }
        System.out.println("完成 updateDailyData。");
    }
    // 辅助方法：获取前一个交易日
    private LocalDate getPreviousTradingDay(LocalDate currentDate) {
        LocalDate prevDay = currentDate.minusDays(1);
        while (prevDay.getDayOfWeek() == DayOfWeek.SATURDAY || prevDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            prevDay = prevDay.minusDays(1);
        }
        return prevDay;
    }
}