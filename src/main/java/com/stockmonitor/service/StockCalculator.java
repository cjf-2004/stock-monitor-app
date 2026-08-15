package com.stockmonitor.service;

import com.stockmonitor.model.PricePoint;
import com.stockmonitor.model.Stock;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Deque;

import org.springframework.stereotype.Service; // 让 Spring 能够管理和注入它

@Service
public class StockCalculator {

    private final AppConfigService appConfigService;

    public StockCalculator(AppConfigService appConfigService){
        this.appConfigService = appConfigService;
    }
    public void calculateChangeRate(Stock stock) {
        if (stock.getPrevClosePrice() != null && stock.getPrevClosePrice().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal change = stock.getBase().getCurrentPrice().subtract(stock.getPrevClosePrice());
            BigDecimal changeRate = change.divide(stock.getPrevClosePrice(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            stock.setChangeRate(changeRate);
        } else {
            stock.setChangeRate(BigDecimal.ZERO);
        }
    }

    public void calculateAmplitude(Stock stock) {
        if (stock.getOpenPrice() != null && stock.getPrevClosePrice() != null) {
            BigDecimal highest = stock.getHighPrice();
            BigDecimal lowest = stock.getLowPrice();
            if (highest != null && lowest != null) {
                BigDecimal amplitude = highest.subtract(lowest)
                        .divide(stock.getPrevClosePrice(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                stock.setAmplitude(amplitude);
            }
        }
    }

    public void updateHighAndLow(Stock stock, BigDecimal currentPrice) {
        if (stock.getHighPrice() == null || currentPrice.compareTo(stock.getHighPrice()) > 0) {
            stock.setHighPrice(currentPrice);
        }
        if (stock.getLowPrice() == null || currentPrice.compareTo(stock.getLowPrice()) < 0) {
            stock.setLowPrice(currentPrice);
        }
    }

    /**
     * 计算股票的涨速。
     * 涨速 = (窗口末尾价格 - 窗口起始价格) / 窗口起始价格 / 窗口持续时间(秒) * 100%
     *
     * @param stockMetrics 股票的实时指标对象
     */
    public void calculateVelocity(Stock stockMetrics) {
        Deque<PricePoint> vWindow = stockMetrics.getvWindow();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal currentPrice = stockMetrics.getCurrentPrice();

        // 1. 在队尾添加当前时间点和价格
        PricePoint newPoint = new PricePoint(now, currentPrice);
        vWindow.addLast(newPoint);

        // 2. 移除队头，直到窗口内的持续时间满足 VELOCITY_WINDOW_SECONDS
        // 至少保留两个点以便计算，并且只有当窗口时间跨度超过设定的窗口大小时才移除
        while (vWindow.size() > 1) {
            PricePoint firstPoint = vWindow.getFirst();
            PricePoint lastPoint = vWindow.getLast();
            long windowDurationSeconds = ChronoUnit.SECONDS.between(firstPoint.getTimestamp(), lastPoint.getTimestamp());

            if (windowDurationSeconds > appConfigService.getVelocityWindow()) {
                vWindow.removeFirst();
            } else {
                break; // 窗口时间跨度未超限，停止移除
            }
        }

        // 3. 计算涨速
        BigDecimal velocity = BigDecimal.ZERO;
        if (vWindow.size() >= 2) { // 确保至少有两个点才能计算涨速
            PricePoint firstPoint = vWindow.getFirst();
            PricePoint lastPoint = vWindow.getLast();

            BigDecimal priceChange = lastPoint.getPrice().subtract(firstPoint.getPrice());
            long timeDiffSeconds = ChronoUnit.SECONDS.between(firstPoint.getTimestamp(), lastPoint.getTimestamp());

            // 避免除以零或初始价格为零的情况
            if (timeDiffSeconds > 0 && firstPoint.getPrice().compareTo(BigDecimal.ZERO) != 0) {
                // 涨速定义为：(价格变化 / 窗口起始价格) / 时间变化（秒） * 100%
                velocity = priceChange
                        .divide(firstPoint.getPrice(), 8, RoundingMode.HALF_UP) // 价格变化率
                        .divide(BigDecimal.valueOf(timeDiffSeconds), 8, RoundingMode.HALF_UP) // 变化率/秒
                        .multiply(new BigDecimal("100")); // 转换为百分比
            }
        }

        // 4. 更新 StockRealtimeMetrics 实例中的 velocity 字段
        stockMetrics.setVelocity(velocity);
    }

    public void updatePriceWindow(Stock stock) {
        // This method seems to update the highPrice and lowPrice within a window,
        // which is handled by updateHighAndLow with each new price.
        // If it means something else (e.g., specific time window), you'd need to adjust.
        // For now, it's covered by updateHighAndLow.
    }
}