package com.stockmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 启用 Spring 的定时任务调度功能
public class StockMonitorBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockMonitorBackendApplication.class, args);
    }
}