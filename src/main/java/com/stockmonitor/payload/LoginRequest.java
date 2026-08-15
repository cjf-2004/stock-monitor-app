package com.stockmonitor.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {
    public String phoneNumber;
    public String password;
}