// src/main/java/com/stockmonitor/payload/LogoutRequest.java
package com.stockmonitor.payload;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Lombok 注解，自动生成 Getter, Setter, equals, hashCode, toString
@NoArgsConstructor // Lombok 注解，生成无参构造函数
@AllArgsConstructor // Lombok 注解，生成全参构造函数 (如果需要)
public class LogoutRequest {
    public String phoneNumber; // 根据你AuthContoller中使用的字段
    // 如果登出需要其他信息，可以添加更多字段
    // private String refreshToken; // 例如，如果登出时需要客户端明确发送refreshToken来使其失效
}