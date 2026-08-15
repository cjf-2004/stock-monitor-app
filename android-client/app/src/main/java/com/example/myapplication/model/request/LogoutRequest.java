package com.example.myapplication.model.request;

public class LogoutRequest {
    public String phoneNumber; // 根据你AuthContoller中使用的字段
    // 如果登出需要其他信息，可以添加更多字段
    // private String refreshToken; // 例如，如果登出时需要客户端明确发送refreshToken来使其失效
    public LogoutRequest(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
}