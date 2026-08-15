package com.stockmonitor.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stockmonitor.model.MarketType;
import com.stockmonitor.model.StockBase;

@Service
public class StockDataSource {

    public List<StockBase> getStocks() {
        List<StockBase> stocks = new ArrayList<>();

        // 生成主板股票（5支深市 + 5支沪市）
        String[] mainNames1 = {"平安银行", "万科A", "格力电器", "中兴通讯", "五粮液"};
        String[] mainNames2 = {"浦发银行", "中国平安", "招商银行", "贵州茅台", "中信证券"};
        stocks.addAll(generateMarketStocks(1, 5, MarketType.MAIN_BOARD, mainNames1,
                new BigDecimal("10"), new BigDecimal("50"), 0.01));
        stocks.addAll(generateMarketStocks(600000, 5, MarketType.MAIN_BOARD, mainNames2,
                new BigDecimal("20"), new BigDecimal("60"), 0.01));

        // 生成科创板股票
        String[] sciTechNames = {"中芯国际", "寒武纪", "澜起科技", "金山办公", "传音控股",
                "华熙生物", "微芯生物", "晶晨股份", "天宜上佳", "安集科技"};
        stocks.addAll(generateMarketStocks(688001, 10, MarketType.SCI_TECH, sciTechNames,
                new BigDecimal("50"), new BigDecimal("200"), 0.05));

        // 生成创业板股票
        String[] growthNames = {"宁德时代", "东方财富", "迈瑞医疗", "爱尔眼科", "智飞生物",
                "温氏股份", "蓝思科技", "乐普医疗", "汇川技术", "沃森生物"};
        stocks.addAll(generateMarketStocks(300001, 10, MarketType.GROWTH, growthNames,
                new BigDecimal("20"), new BigDecimal("80"), 0.03));

        return stocks;
    }

    private List<StockBase> generateMarketStocks(int startCode, int count, MarketType marketType,
                                                 String[] names, BigDecimal basePriceMin,
                                                 BigDecimal basePriceMax, double volatility) {
        List<StockBase> stocks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < count; i++) {
            StockBase stock = new StockBase();
            // 生成股票代码
            stock.setCode(String.format("%06d", startCode + i));
            stock.setName(names[i]);
            stock.setMarketType(marketType);
            stock.setSuspended(false);

            // 生成基准价格
            BigDecimal basePrice = basePriceMin.add(
                    new BigDecimal(Math.random()).multiply(basePriceMax.subtract(basePriceMin))
            );

            // 计算时间相关价格波动
            int hour = now.getHour();
            int minute = now.getMinute();
            double timeFactor = hour + minute / 60.0;

            // 价格波动组成要素
            double linearTrend = (timeFactor / 24) * 0.005; // 日趋势波动
            double cycle = Math.sin(timeFactor * 2 * Math.PI / 6) * volatility * 0.5; // 周期波动
            double randomChange = (Math.random() * 2 - 1) * volatility; // 随机波动

            // 计算最终价格
            BigDecimal priceChange = basePrice.multiply(
                    new BigDecimal(linearTrend + cycle + randomChange)
            );
            BigDecimal currentPrice = basePrice.add(priceChange);

            // 保证价格有效性
            currentPrice = currentPrice.max(new BigDecimal("0.01"));
            stock.setCurrentPrice(currentPrice.setScale(2, RoundingMode.HALF_UP));

            stocks.add(stock);
        }
        return stocks;
    }
}
