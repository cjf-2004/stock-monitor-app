package com.stockmonitor.service;

import com.stockmonitor.model.*;
import com.stockmonitor.model.CronTimeParser.SimpleTime;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service // 让 Spring 能够管理和注入它
public class MockStockMarket implements StockMarket {
    @Value("${app.dailyProcessor.updatePrevOpenCloseCron}")
    private String updatePrevOpenCloseCron;

    @Value("${app.dailyProcessor.updateDailyDataCron}")
    private String updateDailyDataCron;

    @Value("${app.dailyProcessor.CloseNoon}")
    private String closeNoonCron;

    @Value("${app.dailyProcessor.OpenNoon}")
    private String OpenNoonCron;

    // 存储解析后的时间对象
    private SimpleTime updatePrevOpenCloseTime;
    private SimpleTime updateDailyDataTime;
    private SimpleTime closeNoonTime;
    private SimpleTime openNoonTime;

    //数据源
    private StockDataSource dataSource;
    
    public MockStockMarket(StockDataSource dataSource){
        this.dataSource = dataSource;
    }
    @PostConstruct
    public void init() {
                // 使用 CronTimeParser 解析第一个 cron 表达式
        updatePrevOpenCloseTime = CronTimeParser.parseSimpleCronTime(updatePrevOpenCloseCron);
        if (updatePrevOpenCloseTime != null) {
            System.out.println("解析 'updatePrevOpenClose' Cron 表达式时间: " + updatePrevOpenCloseCron);
        } else {
            System.err.println("无法解析 'updatePrevOpenCloseCron' 表达式: " + updatePrevOpenCloseCron);
            // 这里可以添加错误处理，例如设置默认时间或抛出异常
        }

        // 使用 CronTimeParser 解析第二个 cron 表达式
        updateDailyDataTime = CronTimeParser.parseSimpleCronTime(updateDailyDataCron);
        if (updateDailyDataTime != null) {
            System.out.println("解析 'updateDailyDataCron' 表达式时间: " + updateDailyDataCron);
        } else {
            System.err.println("无法解析 'updateDailyDataCron' 表达式: " + updateDailyDataCron);
            // 这里可以添加错误处理
        }

        // 使用 CronTimeParser 解析第二个 cron 表达式
        closeNoonTime = CronTimeParser.parseSimpleCronTime(closeNoonCron);
        if (closeNoonTime != null) {
            System.out.println("解析 'closeNoonCron' 表达式时间: " + closeNoonCron);
        } else {
            System.err.println("无法解析 'closeNoonCron' 表达式: " + closeNoonCron);
            // 这里可以添加错误处理
        }

        openNoonTime = CronTimeParser.parseSimpleCronTime(OpenNoonCron);
        if (openNoonTime != null) {
            System.out.println("解析 'OpenNoonCron' 表达式时间: " + OpenNoonCron);
        } else {
            System.err.println("无法解析 'OpenNoonCron' 表达式: " + OpenNoonCron);
            // 这里可以添加错误处理
        }

    }

    @Override
    public boolean isTradingTime() {
        LocalTime now = LocalTime.now();
        LocalTime marketOpen = LocalTime.of(updatePrevOpenCloseTime.getHour(), updatePrevOpenCloseTime.getMinute());
        LocalTime marketCloseNoon = LocalTime.of(closeNoonTime.getHour(), closeNoonTime.getMinute());
        LocalTime marketOpenNoon = LocalTime.of(openNoonTime.getHour(),openNoonTime.getMinute());
        LocalTime marketClose = LocalTime.of(updateDailyDataTime.getHour(), updateDailyDataTime.getMinute()); // 假设下午 3:00 收盘

        return isTradingDay(LocalDate.now()) && (now.isAfter(marketOpen) && now.isBefore(marketCloseNoon)) || (now.isAfter(marketOpenNoon) && now.isBefore(marketClose));
    }

    public static boolean isTradingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        // 如果是 SATURDAY (周六) 或 SUNDAY (周日)，则不是交易日
        return !(dayOfWeek == DayOfWeek.SATURDAY ||dayOfWeek == DayOfWeek.SUNDAY); 
    }

    @Override
    public List<StockBase> getStocks() {
        
        // List<StockBase> stocks = new ArrayList<>();
        // // 模拟获取股票数据
        // StockBase stock1 = new StockBase();
        // stock1.setCode("000001");
        // stock1.setName("平安银行");
        // stock1.setMarketType(MarketType.MAIN_BOARD);
        // stock1.setSuspended(false);
        // stock1.setCurrentPrice(new BigDecimal("10.50").add(new BigDecimal(Math.random() * 2 - 0.25))); // 模拟价格波动
        // stocks.add(stock1);

        // StockBase stock2 = new StockBase();
        // stock2.setCode("600000");
        // stock2.setName("浦发银行");
        // stock2.setMarketType(MarketType.MAIN_BOARD);
        // stock2.setSuspended(false);
        // stock2.setCurrentPrice(new BigDecimal("12.00").add(new BigDecimal(Math.random() * 2 - 0.3)));
        // stocks.add(stock2);

        // StockBase stock3 = new StockBase();
        // stock3.setCode("688001");
        // stock3.setName("中芯国际");
        // stock3.setMarketType(MarketType.SCI_TECH);
        // stock3.setSuspended(false);
        // stock3.setCurrentPrice(new BigDecimal("50.00").add(new BigDecimal(Math.random() * 2 - 0.5)));
        // stocks.add(stock3);
        
        return dataSource.getStocks();
    }
}