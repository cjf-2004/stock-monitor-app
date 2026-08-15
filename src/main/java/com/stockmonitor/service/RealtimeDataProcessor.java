package com.stockmonitor.service;

import com.stockmonitor.entity.*;
import com.stockmonitor.model.CronTimeParser;
import com.stockmonitor.model.CronTimeParser.SimpleTime;
import com.stockmonitor.model.Stock;
import com.stockmonitor.model.StockBase;
import com.stockmonitor.repository.StockDailyDataRepository;
import com.stockmonitor.repository.StockInfRepository;
import com.stockmonitor.repository.StockRealtimeDataRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek; // 导入 DayOfWeek
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RealtimeDataProcessor {

    private final StockMarket stockMarket;
    private final StockRealtimeDataRepository stockRealtimeDataRepository;
    private final StockInfRepository stockInfRepository;
    private final AppConfigService appConfigService;
    private final StockCalculator stockCalculator;
    private final StockDailyDataRepository stockDailyDataRepository;
    private final DailyDataProcessor dailyDataProcessor;

    private final Map<String, Stock> currentStockStates = new ConcurrentHashMap<>();

    @Value("${app.pollingIntervalSeconds:5}")
    private Integer pollingIntervalSeconds;

    @Value("${app.dailyProcessor.updatePrevOpenCloseCron}")
    private String updatePrevOpenCloseCronString; // 将字段名修改为 String 结尾，避免和解析后的对象混淆

    @Value("${app.dailyProcessor.updateDailyDataCron}")
    private String updateDailyDataCronString;

    // 存储解析后的时间对象
    private SimpleTime updatePrevOpenCloseTime;
    private SimpleTime updateDailyDataTime;

    private LocalDate lastProcessedDateForDailyTasks = null;
    private boolean isOpenedProcessedToday = false;
    private boolean isClosedProcessedToday = false;

    private ScheduledExecutorService scheduler;

    public RealtimeDataProcessor(StockMarket stockMarket,
                                 StockRealtimeDataRepository stockRealtimeDataRepository,
                                 StockCalculator stockCalculator,
                                 StockDailyDataRepository stockDailyDataRepository,
                                 DailyDataProcessor dailyDataProcessor,
                                 StockInfRepository stockInfRepository,
                                 AppConfigService appConfigService) {
        this.stockMarket = stockMarket;
        this.stockRealtimeDataRepository = stockRealtimeDataRepository;
        this.stockCalculator = stockCalculator;
        this.stockDailyDataRepository = stockDailyDataRepository;
        this.dailyDataProcessor = dailyDataProcessor;
        this.stockInfRepository = stockInfRepository;
        this.appConfigService =appConfigService;
    }

    @PostConstruct
    @Transactional // 确保初始化操作在事务中执行
    public void init() {
        pollingIntervalSeconds = appConfigService.getPollingInterval();

        // 使用 CronTimeParser 解析第一个 cron 表达式
        updatePrevOpenCloseTime = CronTimeParser.parseSimpleCronTime(updatePrevOpenCloseCronString);
        if (updatePrevOpenCloseTime != null) {
            System.out.println("解析 'updatePrevOpenClose' Cron 表达式时间: " + updatePrevOpenCloseTime);
        } else {
            System.err.println("无法解析 'updatePrevOpenCloseCron' 表达式: " + updatePrevOpenCloseCronString);
            // 这里可以添加错误处理，例如设置默认时间或抛出异常
        }

        // 使用 CronTimeParser 解析第二个 cron 表达式
        updateDailyDataTime = CronTimeParser.parseSimpleCronTime(updateDailyDataCronString);
        if (updateDailyDataTime != null) {
            System.out.println("解析 'updateDailyDataCron' 表达式时间: " + updateDailyDataTime);
        } else {
            System.err.println("无法解析 'updateDailyDataCron' 表达式: " + updateDailyDataCronString);
            // 这里可以添加错误处理
        }

        // **在调度器启动之前，先初始化股票基础数据**
        System.out.println("[RDP-INIT] 正在检查并初始化股票基础数据...");
        List<StockBase> mockStocks = stockMarket.getStocks(); // 从模拟市场获取数据
        for (StockBase stockBase : mockStocks) {
            Optional<StockInf> existingStock = stockInfRepository.findById(stockBase.getCode());
            if (existingStock.isPresent()) {
                // 如果存在，更新股票信息（如果名称或市场类型可能变更）
                StockInf stockToUpdate = existingStock.get();
                boolean changed = false;
                if (!stockToUpdate.getStockName().equals(stockBase.getName())) {
                    stockToUpdate.setStockName(stockBase.getName());
                    changed = true;
                }
                if (stockToUpdate.getMarketType() != stockBase.getMarketType()) {
                    stockToUpdate.setMarketType(stockBase.getMarketType());
                    changed = true;
                }
                if (stockToUpdate.getIsSuspended() != stockBase.getSuspended()){
                    stockToUpdate.setIsSuspended(stockBase.getSuspended());
                    changed = true;
                }
                if (changed) {
                    stockInfRepository.save(stockToUpdate); // 执行更新
                    System.out.println("[RDP-INIT] 更新了股票基础数据: " + stockBase.getCode() + " - " + stockBase.getName());
                }
            } else {
                // 如果不存在，插入新的股票信息
                StockInf newStock = new StockInf();
                newStock.setStockId(stockBase.getCode());
                newStock.setStockName(stockBase.getName());
                newStock.setMarketType(stockBase.getMarketType()); // 设置 MarketType
                newStock.setIsSuspended(stockBase.getSuspended());
                stockInfRepository.save(newStock); // 执行插入
                System.out.println("[RDP-INIT] 插入了新的股票基础数据: " + stockBase.getCode() + " - " + stockBase.getName());
            }
        }
        System.out.println("[RDP-INIT] 股票基础数据初始化完成。");

        // 启动定时任务调度器
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::polling, 0, pollingIntervalSeconds, TimeUnit.SECONDS);
        System.out.println("[RDP-INIT] RealtimeDataProcessor 已启动，轮询间隔: " + pollingIntervalSeconds + " 秒");
    }


    @PreDestroy
    @Transactional
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("[RDP-SHUTDOWN] RealtimeDataProcessor 的定时任务已关闭。");
        }
    }

    public void polling() {
        // 在每次 polling 方法开始时打印，确认是否被持续调用
        System.out.println("[RDP-POLLING] >>> 实时数据轮询任务开始执行： " + LocalDateTime.now() + " <<<");

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 每日任务标志重置
        if (lastProcessedDateForDailyTasks == null || !lastProcessedDateForDailyTasks.isEqual(today)) {
            resetDailyTaskFlags();
            lastProcessedDateForDailyTasks = today;
        }

        LocalTime marketOpenTime = LocalTime.of(updatePrevOpenCloseTime.getHour(), updatePrevOpenCloseTime.getMinute());
        LocalTime marketCloseTime = LocalTime.of(updateDailyDataTime.getHour(), updateDailyDataTime.getMinute());
        LocalTime dailyUpdateExecutionTime = LocalTime.of(updateDailyDataTime.getHour(), updateDailyDataTime.getMinute()+2);



        // --- 实时数据轮询逻辑 ---
        boolean isTradingNow = stockMarket.isTradingTime();
        System.out.println("[RDP-POLLING] stockMarket.isTradingTime() 返回: " + isTradingNow + " (当前时间: " + now + ")");

        if (isTradingNow) {
            System.out.println("[RDP-POLLING] 交易时间，正在获取实时数据...");
            List<StockBase> stockBases = stockMarket.getStocks(); // 获取原始股票数据
            System.out.println("[RDP-POLLING] 从 StockMarket 获取到 " + stockBases.size() + " 条股票基础数据。");

            List<StockRealtimeData> realtimeDataToSave = stockBases.stream().map(stockBase -> {
                try {
                    Stock stock = currentStockStates.computeIfAbsent(stockBase.getCode(), k -> {
                        Stock newStock = new Stock(stockBase);
                        newStock.setLowPrice(stockBase.getCurrentPrice());
                        newStock.setHighPrice(stockBase.getCurrentPrice());

                        // 加载前收盘价
                        stockDailyDataRepository.findByStockIdAndTradeDate(stockBase.getCode(), LocalDate.now().minusDays(1))
                                .ifPresentOrElse(prevDayData -> newStock.setPrevClosePrice(prevDayData.getClosePrice()),
                                                () -> System.out.println("[RDP-POLLING] 未找到 " + stockBase.getCode() + " 前收盘价。"));

                        // 加载开盘价或设置当前价为开盘价
                        stockDailyDataRepository.findByStockIdAndTradeDate(stockBase.getCode(), LocalDate.now())
                                .ifPresentOrElse(todayData -> {
                                    if (todayData.getOpenPrice() != null) {
                                        newStock.setOpenPrice(todayData.getOpenPrice());
                                    } else {
                                        newStock.setOpenPrice(stockBase.getCurrentPrice());
                                    }
                                }, () -> newStock.setOpenPrice(stockBase.getCurrentPrice())); // 如果今天数据也不存在，直接设开盘价为当前价
                        return newStock;
                    });

                    stock.setCurrentPrice(stockBase.getCurrentPrice());
                    stockCalculator.updateHighAndLow(stock, stock.getCurrentPrice());
                    stockCalculator.calculateChangeRate(stock);
                    stockCalculator.calculateAmplitude(stock);
                    stockCalculator.calculateVelocity(stock);

                    StockRealtimeData realtimeData = new StockRealtimeData();
                    realtimeData.setStockId(stock.getStockId());
                    realtimeData.setTimestamp(LocalDateTime.now());
                    realtimeData.setPrice(stock.getCurrentPrice());
                    realtimeData.setHighestPrice(stock.getHighPrice());
                    realtimeData.setLowestPrice(stock.getLowPrice());
                    realtimeData.setChangePercent(stock.getChangeRate());
                    realtimeData.setAmplitude(stock.getAmplitude());
                    realtimeData.setVelocity(stock.getVelocity());
                    
                    return realtimeData;
                } catch (Exception e) {
                    System.err.println("[RDP-ERROR] 处理股票 " + stockBase.getCode() + " 时发生异常: " + e.getMessage());
                    e.printStackTrace(); // 打印完整的堆栈跟踪
                    return null; // 返回 null，表示此条数据处理失败
                }
            }).filter(java.util.Objects::nonNull) // 过滤掉处理失败的 null 值
            .collect(Collectors.toList());

            if (!realtimeDataToSave.isEmpty()) {
                System.out.println("[RDP-POLLING] 准备保存 " + realtimeDataToSave.size() + " 条实时数据...");
                stockRealtimeDataRepository.saveAll(realtimeDataToSave);
                System.out.println("[RDP-POLLING] 实时数据已轮询并保存于: " + LocalDateTime.now());
            } else {
                System.out.println("[RDP-POLLING] 未获取到需要保存的实时数据（可能是 stockMarket.getStocks() 返回空）。");
            }
            // --- 每日开盘任务触发 ---
            // 检查是否在开盘时间点附近且当天开盘任务未执行
            if (!isOpenedProcessedToday && now.isAfter(marketOpenTime.minusMinutes(1)) && now.isBefore(marketOpenTime.plusMinutes(5))) {
                System.out.println("[RDP-DAILY-OPEN] 触发每日开盘任务：updatePrevCloseAndOpen");
                dailyDataProcessor.updatePrevCloseAndOpen(currentStockStates);
                isOpenedProcessedToday = true;
                System.out.println("[RDP-DAILY-OPEN] 每日开盘任务执行完毕。");
            }
        System.out.println("[RDP-POLLING] <<< 实时数据轮询任务执行结束。 >>>");
        } else {
            System.out.println("[RDP-POLLING] 非交易时间。跳过实时数据轮询逻辑。");
            // --- 每日收盘任务触发 ---
            if (!isClosedProcessedToday && now.isAfter(marketCloseTime) && now.isBefore(dailyUpdateExecutionTime.plusMinutes(5))) {
                System.out.println("[RDP-DAILY-CLOSE] 触发每日收盘任务：updateDailyData");
                dailyDataProcessor.updateDailyData(currentStockStates);
                isClosedProcessedToday = true;
                System.out.println("[RDP-DAILY-CLOSE] 每日收盘任务执行完毕。");
                currentStockStates.clear();
                System.out.println("[RDP-DAILY-CLOSE] 已清空当前股票状态缓存。");
            } else {
                 System.out.println("[RDP-DAILY-CLOSE] 非收盘任务触发时间窗口。isClosedProcessedToday=" + isClosedProcessedToday);
            }
        }

    }

    private void resetDailyTaskFlags() {
        System.out.println("[RDP-RESET] 重置每日任务标志。");
        isOpenedProcessedToday = false;
        isClosedProcessedToday = false;
    }
}