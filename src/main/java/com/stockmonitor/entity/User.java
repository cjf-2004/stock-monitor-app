package com.stockmonitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data // Lombok 注解，自动生成 Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // Lombok 注解，生成无参构造函数
@AllArgsConstructor // Lombok 注解，生成全参构造函数
@Entity // 标记这是一个 JPA 实体
@Table(name = "users") // 映射到数据库中的 users 表
public class User {

    @Id // 标记为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键
    private Integer userId;

    @Column(name = "phone_number", unique = true, nullable = false, length = 11)
    private String phoneNumber;

    @Column(name = "password_encode", nullable = false, length = 255)
    private String passwordEncode; // 存储哈希后的密码

    @Enumerated(EnumType.STRING) // 枚举类型映射为数据库中的字符串
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('LOG_IN', 'LOG_OUT') DEFAULT 'LOG_OUT'")
    private UserStatus status; // 使用枚举表示用户状态

    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime; // 对应 DATETIME 类型

    // 枚举定义
    public enum UserStatus {
        LOG_IN,
        LOG_OUT
    }
}