package com.stockmonitor.repository;
import com.stockmonitor.entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, String> { // AppConfig 是实体类，String 是主键类型

    // 可以添加自定义查询方法，例如根据 key 查找
    // Optional<AppConfig> findByConfigKey(String configKey); // findById已经提供了根据主键查找的功能
}