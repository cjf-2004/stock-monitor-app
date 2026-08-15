package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.MyApplication;
import com.example.myapplication.R;
import com.example.myapplication.api.AuthService;
import com.example.myapplication.model.request.LoginRequest;
import com.example.myapplication.model.request.RefreshTokenRequest;
import com.example.myapplication.model.response.ApiResponse; // Keep if registerUser was moved but you still need it for some other reason, though typically not here
import com.example.myapplication.model.response.JwtResponse;
import com.example.myapplication.utils.TokenManager;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etPhoneNumber, etPassword;
    private Button btnLogin; // Removed btnRegister
    private TextView btnGoToRegister; // New button for navigation to RegisterActivity

    // UI elements for loading state
    private View loadingIndicator; // e.g., a ProgressBar
    private View loginFormContainer; // e.g., a LinearLayout containing your input fields and buttons

    private AuthService authService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        });
        // Initialize UI elements
        etPhoneNumber = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoToRegister = findViewById(R.id.register_tv); // Initialize new button

        // Find your loading views if you have them in activity_login.xml
        // loadingIndicator = findViewById(R.id.loading_indicator); // Example ID
        // loginFormContainer = findViewById(R.id.login_form_container); // Example ID

        // Get instances from your Application class
        MyApplication application = (MyApplication) getApplication();
        tokenManager = application.getTokenManager();
        authService = application.getAuthService(); // This AuthService should NOT have AuthInterceptor as Authenticator

        // --- Auto-login check (Remember Me) ---
        if (tokenManager.hasTokens()) {
            Log.d(TAG, "Existing tokens found. Attempting silent token refresh.");
            showLoading(); // Show loading indicator during silent refresh
            attemptSilentRefresh(); // <--- Call silent refresh instead of direct navigation
        } else {
            Log.d(TAG, "No existing tokens found. Displaying login UI.");
            setupLoginUI(); // Ensure login UI is visible if no tokens
        }
        // --- End Auto-login check ---

        // Set up listeners for login and register buttons
        btnLogin.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (phoneNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入手机号和密码。", Toast.LENGTH_SHORT).show();
                return;
            }
            showLoading(); // Show loading indicator during manual login attempt
            loginUser(phoneNumber, password);
        });

        // New listener for the "Go to Register" button
        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            // You can optionally finish LoginActivity here if you don't want it on the back stack
            // finish();
        });
    }

    private void loginUser(String phoneNumber, String password) {
        LoginRequest loginRequest = new LoginRequest(phoneNumber, password);

        authService.login(loginRequest).enqueue(new Callback<JwtResponse>() {
            @Override
            public void onResponse(@NonNull Call<JwtResponse> call, @NonNull Response<JwtResponse> response) {
                hideLoading(); // Hide loading indicator regardless of success or failure
                if (response.isSuccessful() && response.body() != null) {
                    JwtResponse jwtResponse = response.body();
                    tokenManager.saveTokens(jwtResponse.getAccessToken(), jwtResponse.getRefreshToken());

                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    });
                } else {
                    String errorMessage = "";
                    try {
                        errorMessage += response.errorBody() != null ? response.errorBody().string() : "未知错误";
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorMessage += "解析错误信息失败。";
                    }
                    Log.e(TAG, errorMessage);
                    String finalErrorMessage = errorMessage;
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, finalErrorMessage, Toast.LENGTH_LONG).show();
                        setupLoginUI(); // Show login UI again if login fails
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<JwtResponse> call, @NonNull Throwable t) {
                hideLoading(); // Hide loading indicator
                Log.e(TAG, "登录网络错误: ", t);
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    setupLoginUI(); // Show login UI again on network error
                });
            }
        });
    }

    // Removed registerUser method from LoginActivity

    // New method for silent token refresh
    private void attemptSilentRefresh() {
        String refreshToken = tokenManager.getRefreshToken();

        if (refreshToken == null) {
            Log.e(TAG, "刷新令牌为null，尽管hasTokens()为true。");
            tokenManager.clearTokens(); // Defensive clear
            setupLoginUI(); // Show login UI
            return;
        }

        // Call the refresh-token API
        authService.refreshToken(new RefreshTokenRequest(refreshToken)).enqueue(new Callback<JwtResponse>() {
            @Override
            public void onResponse(@NonNull Call<JwtResponse> call, @NonNull Response<JwtResponse> response) {
                hideLoading(); // Hide loading indicator
                if (response.isSuccessful() && response.body() != null) {
                    JwtResponse jwtResponse = response.body();
                    tokenManager.saveTokens(jwtResponse.getAccessToken(), jwtResponse.getRefreshToken());

                    Log.d(TAG, "静默刷新成功。导航到主界面。");
                    navigateToMainActivity();
                } else {
                    // Refresh token is expired or invalid
                    String errorMessage = "静默刷新失败。请重新登录。错误码: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "解析刷新错误体时出错", e);
                    }
                    Log.e(TAG, errorMessage);
                    tokenManager.clearTokens(); // Clear all tokens if refresh fails
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "会话已过期，请重新登录。", Toast.LENGTH_LONG).show();
                        setupLoginUI(); // Show login UI
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<JwtResponse> call, @NonNull Throwable t) {
                hideLoading(); // Hide loading indicator
                Log.e(TAG, "静默刷新网络错误: " + t.getMessage(), t);
                tokenManager.clearTokens(); // Clear tokens on network error
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "网络错误，请检查网络连接。", Toast.LENGTH_LONG).show();
                    setupLoginUI(); // Show login UI
                });
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close LoginActivity
    }

    //region UI State Management
    private void showLoading() {
        if (loginFormContainer != null) {
            loginFormContainer.setVisibility(View.GONE); // Hide login form
        }
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.VISIBLE); // Show progress bar
        }
    }

    private void hideLoading() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE); // Hide progress bar
        }
    }

    private void setupLoginUI() {
        if (loginFormContainer != null) {
            loginFormContainer.setVisibility(View.VISIBLE); // Show login form
        }
        // Ensure input fields are enabled and clear any previous input if needed
        etPhoneNumber.setEnabled(true);
        etPassword.setEnabled(true);
        btnLogin.setEnabled(true);
        btnGoToRegister.setEnabled(true); // Enable the new button
        // etPassword.setText(""); // Optional: clear password field
    }
    //endregion
}