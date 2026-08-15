package com.stockmonitor.repository;
import com.stockmonitor.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.stockmonitor.entity.*;

import java.util.List;

/**
 * StockInfRepository 接口，用于对 StockInf 实体进行数据访问操作。
 * 继承 JpaRepository 提供了基本的 CRUD (创建、读取、更新、删除) 功能。
 */
@Repository // 标记这是一个 Spring Repository 组件
public interface StockInfRepository extends JpaRepository<StockInf, String> {
    // JpaRepository<T, ID>
    // T: 实体类型 (这里是 StockInf)
    // ID: 实体主键的类型 (这里是 String，对应 StockInfId)

    // 除了 JpaRepository 提供的 findAll(), findById(), save(), delete() 等方法外，
    // 你还可以根据需要添加自定义查询方法。
    // Spring Data JPA 会根据方法名自动生成查询实现。

    /**
     * 根据股票名称查找股票列表。
     * @param StockName 股票名称
     * @return 匹配股票名称的列表
     */
    List<StockInf> findByStockName(String StockName);

    /**
     * 根据市场类型查找股票列表。
     * @param marketType 市场类型
     * @return 匹配市场类型的股票列表
     */
    List<StockInf> findByMarketType(MarketType marketType);

    /**
     * 根据是否暂停交易查找股票列表。
     * @param isSuspended 是否暂停交易
     * @return 匹配暂停状态的股票列表
     */
    List<StockInf> findByIsSuspended(Boolean isSuspended);

    /**
     * 根据股票名称和市场类型查找股票列表。
     * @param StockName 股票名称
     * @param marketType 市场类型
     * @return 匹配股票名称和市场类型的股票列表
     */
    List<StockInf> findByStockNameAndMarketType(String StockName, MarketType marketType);
}
