package com.example.myapplication;

import android.app.Application;
import android.util.Log;

import com.example.myapplication.api.AuthInterceptor;
import com.example.myapplication.api.AuthService;
import com.example.myapplication.utils.TokenManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    // Replace with your actual backend IP address and port
    // Emulator: "http://10.0.2.2:8080/" or "https://10.0.2.2:8443/" (for HTTPS)
    // Real device: "http://YOUR_LOCAL_NETWORK_IP:8080/" or "https://YOUR_LOCAL_NETWORK_IP:8443/"
    private static final String BASE_URL = "https://10.0.2.2:8443/"; // Ensure it ends with a slash

    private TokenManager tokenManager;
    private AuthService authService; // Used for login/register (no interceptor initially)
    private Retrofit protectedRetrofit; // Used for authenticated API calls (with interceptor)

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MyApplication onCreate started.");
        tokenManager = new TokenManager(this);
        initRetrofitClients();
        Log.d(TAG, "MyApplication onCreate finished.");
    }

    private void initRetrofitClients() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Log request and response bodies

        // 1. Create an OkHttpClient and Retrofit instance WITHOUT the AuthInterceptor
        //    This client is used for login/register/refresh token calls to prevent circular dependencies.
        OkHttpClient baseOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                // Add SSL certificate handling if using HTTPS and self-signed certs
                // .sslSocketFactory(YourSslTrustManager.getSslSocketFactory(), YourSslTrustManager.getTrustManager())
                // .hostnameVerifier(YourSslTrustManager.getHostnameVerifier())
                .build();

        Retrofit baseRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(baseOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = baseRetrofit.create(AuthService.class); // This AuthService is used for login/refresh

        // 2. Create an OkHttpClient and Retrofit instance WITH the AuthInterceptor
        //    This client is used for all protected API calls.
        AuthInterceptor authInterceptor = new AuthInterceptor(this, tokenManager, authService); // Pass the baseAuthService
        OkHttpClient protectedOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor) // Logging for all requests
                .addInterceptor(authInterceptor)     // Intercepts requests to add Authorization header
                .authenticator(authInterceptor)      // Handles 401 Unauthorized errors to refresh tokens
                // Add SSL certificate handling if using HTTPS
                // .sslSocketFactory(YourSslTrustManager.getSslSocketFactory(), YourSslTrustManager.getTrustManager())
                // .hostnameVerifier(YourSslTrustManager.getHostnameVerifier())
                .build();

        protectedRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // 注意结尾斜杠
                .addConverterFactory(ScalarsConverterFactory.create()) // 先加这个
                .addConverterFactory(GsonConverterFactory.create())     // 再加 Gson
                .client(protectedOkHttpClient)
                .build();
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    // Use this for login, register, and refresh token calls (no AuthInterceptor applied)
    public AuthService getAuthService() {
        return authService;
    }

    // Use this for all other protected API calls (AuthInterceptor will apply)
    public <T> T createProtectedApiService(Class<T> serviceClass) {
        return protectedRetrofit.create(serviceClass);
    }
}