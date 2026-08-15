package com.stockmonitor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "system_configs") 
public class AppConfig {

    @Id
    @Column(name = "config_key", length = 50) // key的长度
    private String configKey;

    @Column(name = "config_value", length = 255) // value的长度
    private String configValue;

    @Column(name = "description", length = 255) // 描述的长度
    private String description;

    // 无参构造函数 (JPA要求)
    public AppConfig() {
    }

    // 构造函数（可选，方便创建对象）
    public AppConfig(String configKey, String configValue, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
    }

    // Getters and Setters
    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
