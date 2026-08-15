package com.stockmonitor.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequest {
    public String phoneNumber;
    public String password;
}
