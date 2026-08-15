package com.stockmonitor.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // 方便构建带有所有字段的构造函数
@NoArgsConstructor  // 添加无参构造函数以满足JSON反序列化
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer"; // 标准 JWT token 类型
}