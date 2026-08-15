package com.stockmonitor.service;
import com.stockmonitor.repository.*;
import com.stockmonitor.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AppConfigService {

    @Autowired
    private AppConfigRepository appConfigRepository;

    // 缓存配置，避免频繁查询数据库
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    /**
     * 根据 key 获取配置值。如果缓存中没有，则从数据库加载并放入缓存。
     * @param key 配置的键
     * @return 配置的值，如果不存在则返回null
     */
    public String getConfigValue(String key) {
        // 尝试从缓存获取
        String value = configCache.get(key);
        if (value != null) {
            return value;
        }

        // 缓存未命中，从数据库加载
        Optional<AppConfig> appConfigOptional = appConfigRepository.findById(key);
        if (appConfigOptional.isPresent()) {
            AppConfig appConfig = appConfigOptional.get();
            configCache.put(key, appConfig.getConfigValue()); // 放入缓存
            return appConfig.getConfigValue();
        }
        return null; // 配置不存在
    }

    /**
     * 获取所有配置并加载到缓存中。
     * 可以在应用启动时调用，预加载所有配置。
     */
    public void loadAllConfWigsIntoCache() {
        List<AppConfig> allConfigs = appConfigRepository.findAll();
        configCache.clear(); // 清除旧缓存
        for (AppConfig config : allConfigs) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
        System.out.println("所有配置已加载到缓存中。");
    }

    // 示例：获取特定类型的配置值
    public Integer getAlertRecordRetentionDays() {
        String value = getConfigValue("ALERT_RECORD_RETENTION_DAYS");
        return value != null ? Integer.parseInt(value) : null;
    }

    public Integer getAttentionMax() {
        String value = getConfigValue("ATTENTION_MAX");
        return value != null ? Integer.parseInt(value) : null;
    }

    public Integer getPollingInterval() {
        String value = getConfigValue("POLLING_INTERVAL");
        return value != null ? Integer.parseInt(value) : null;
    }

    public Integer getMaxInactiveDates() {
        String value = getConfigValue("MAX_INACTIVATE_DATES");
        return value != null ? Integer.parseInt(value) : null;
    }

    public Integer getVelocityWindow() {
        String value = getConfigValue("VELOCITY_WINDOW");
        return value != null ? Integer.parseInt(value) : null;
    }

    public Integer getResentMax() {
        String value = getConfigValue("RESENT_MAX");
        return value != null ? Integer.parseInt(value) : null;
    }
    public Integer getDetailAlertDisplayCount() {
        String value = getConfigValue("DETAIL_ALERT_DISPLAY_COUNT");
        return value != null ? Integer.parseInt(value) : null;
    }
}