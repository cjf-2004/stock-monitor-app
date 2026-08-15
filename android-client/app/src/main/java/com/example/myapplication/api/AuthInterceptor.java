package com.example.myapplication.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.model.request.RefreshTokenRequest;
import com.example.myapplication.model.response.JwtResponse;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.utils.TokenManager;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class AuthInterceptor implements Interceptor, Authenticator {

    private static final String TAG = "AuthInterceptor";
    private TokenManager tokenManager;
    private AuthService authService; // Injected AuthService for token refresh
    private Context context; // For redirecting to LoginActivity

    public AuthInterceptor(Context context, TokenManager tokenManager, AuthService authService) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.authService = authService;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String accessToken = tokenManager.getAccessToken();

        // 1. Add Access Token to the Authorization header if available
        if (accessToken != null && tokenManager.getRefreshToken() != null) {
            Log.d(TAG, "Adding Authorization header to request: " + originalRequest.url());
            originalRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
        }

        // Proceed with the modified request
        Response response = chain.proceed(originalRequest);

        // 2. If 401 Unauthorized, and it's not the refresh token request itself,
        // let the Authenticator handle it.
        // It's crucial to prevent intercepting the refresh token request's own 401,
        // which would lead to an infinite loop.
        if (response.code() == 401 && !originalRequest.url().encodedPath().contains("api/auth/refresh-token")) {
            Log.w(TAG, "Received 401 Unauthorized for: " + originalRequest.url() + ". Authenticator will attempt refresh.");
            // Return the response so Authenticator can process it
            return response;
        }

        return response;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        Log.d(TAG, "Authenticator triggered for URL: " + response.request().url());

        // Important: Synchronize this block to prevent multiple threads from refreshing tokens simultaneously.
        // This ensures only one refresh request is made at a time.
        synchronized (this) {
            // Get the current access token *after* synchronization, as another thread might have refreshed it
            String currentAccessToken = tokenManager.getAccessToken();

            // Check if the token that caused 401 is still the current one.
            // If not, it means another thread already refreshed it or it was cleared.
            if (currentAccessToken != null && currentAccessToken.equals(
                    response.request().header("Authorization") != null ?
                            response.request().header("Authorization").replace("Bearer ", "") : null)) {
                // Token is still the same, proceed with refresh attempt

                String refreshToken = tokenManager.getRefreshToken();
                if (refreshToken == null) {
                    Log.e(TAG, "No refresh token found. Clearing tokens and redirecting to login.");
                    tokenManager.clearTokens();
                    redirectToLogin();
                    return null; // No refresh token, can't retry
                }

                Log.d(TAG, "Attempting to refresh token with refresh token.");
                // Synchronously call the refresh token API
                // This 'execute()' call is crucial for Authenticator's synchronous nature
                retrofit2.Response<JwtResponse> refreshResponse = authService.refreshToken(new RefreshTokenRequest(refreshToken)).execute();

                if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                    JwtResponse jwtResponse = refreshResponse.body();
                    String newAccessToken = jwtResponse.getAccessToken();
                    String newRefreshToken = jwtResponse.getRefreshToken();

                    tokenManager.saveTokens(newAccessToken, newRefreshToken); // Save new tokens
                    Log.d(TAG, "Token refresh successful. New Access Token obtained.");

                    // Retry the original request with the new access token
                    return response.request().newBuilder()
                            .header("Authorization", "Bearer " + newAccessToken)
                            .build();
                } else {
                    String errorBody = refreshResponse.errorBody() != null ? refreshResponse.errorBody().string() : "Unknown error";
                    Log.e(TAG, "Token refresh failed with code " + refreshResponse.code() + ": " + errorBody);
                    tokenManager.clearTokens(); // Clear tokens if refresh failed
                    redirectToLogin(); // Redirect to login
                    return null; // Refresh failed, don't retry
                }
            } else {
                // Token has been refreshed or cleared by another thread/process.
                // Simply retry the original request with the potentially new token (if any)
                // Or, if no new token, it means a previous refresh failed, and we need to re-login.
                Log.d(TAG, "Access token has changed or is null before retry. Retrying with current tokens or redirecting.");
                if (tokenManager.getAccessToken() != null) {
                    return response.request().newBuilder()
                            .header("Authorization", "Bearer " + tokenManager.getAccessToken())
                            .build();
                } else {
                    // No valid token after attempted refresh or clear, force re-login
                    redirectToLogin();
                    return null;
                }
            }
        }
    }

    private void redirectToLogin() {
        // Use Handler to post to main thread if this is called from background thread
        // (Authenticator runs on OkHttp's dispatcher thread)
        new android.os.Handler(context.getMainLooper()).post(() -> {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
            context.startActivity(intent);
        });
    }
}