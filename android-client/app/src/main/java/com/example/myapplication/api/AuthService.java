package com.example.myapplication.api;

import com.example.myapplication.model.User;
import com.example.myapplication.model.request.LoginRequest;
import com.example.myapplication.model.request.LogoutRequest;
import com.example.myapplication.model.request.RefreshTokenRequest;
import com.example.myapplication.model.request.RegisterRequest;
import com.example.myapplication.model.response.*;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthService {

    @POST("api/auth/login")
    Call<JwtResponse> login(@Body LoginRequest loginRequest);

    @POST("api/auth/register")
    Call<ApiResponse<Number>>register(@Body RegisterRequest registerRequest); // Assuming register returns a simple message

    @POST("api/auth/logout")
    Call<ApiResponse<Number>> logout (); // Assuming register returns a simple message

    @POST("api/auth/refresh-token")
    Call<JwtResponse> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);
    // --- Example of a protected API endpoint (replace with your actual protected APIs) ---
    @GET("api/user/profile") // Example endpoint, adjust as needed
    Call<ApiResponse<User>> getUserProfile(); // Or a specific DTO for profile



}