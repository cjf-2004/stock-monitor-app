package com.example.myapplication.model;

public class User {
    private Integer userId;
    private String phoneNumber;
    private String passwordEncode;
    private UserStatus status;
    private String lastActiveTime;
    // 枚举定义
    public enum UserStatus {
        LOG_IN,
        LOG_OUT
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
}
