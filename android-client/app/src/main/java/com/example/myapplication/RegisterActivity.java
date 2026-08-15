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
import com.example.myapplication.model.request.RegisterRequest;
import com.example.myapplication.model.response.ApiResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText etPhoneNumber, etPassword, etPasswordAg;
    private TextView btnConfirmRegister, btnGoToLogin;

    // UI elements for loading state (optional, you can add them if needed)
    private View loadingIndicator;
    private View registerFormContainer;

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        });

        etPhoneNumber = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etPasswordAg = findViewById(R.id.et_password_ag);

        btnConfirmRegister = findViewById(R.id.btn_register);
        btnGoToLogin = findViewById(R.id.login_tv);

        // Get AuthService instance from your Application class
        MyApplication application = (MyApplication) getApplication();
        authService = application.getAuthService();

        btnConfirmRegister.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String passwordAg = etPasswordAg.getText().toString().trim();
            // 1. Basic empty input check
            if (phoneNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入手机号和密码。", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Phone number validation (CHAR(11), 11 digits)
            if (phoneNumber.length() != 11 || !phoneNumber.matches("\\d+")) { // \\d+ matches one or more digits
                Toast.makeText(this, "手机号必须是11位数字。", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Password validation (VARCHAR(16), 8-16 alphanumeric/underscore)
            // Regex explanation:
            // ^                   # Start of the string
            // [a-zA-Z0-9_]{8,16}  # Matches any uppercase letter (a-z), lowercase letter (A-Z), digit (0-9), or underscore (_),
            //                     # exactly between 8 and 16 times.
            // $                   # End of the string
            if (password.length() < 8 || password.length() > 16 || !password.matches("^[a-zA-Z0-9_]{8,16}$")) {
                Toast.makeText(this, "密码长度需为8-16位，且只能包含大小写英文、数字和下划线。", Toast.LENGTH_LONG).show();
                return;
            }

            if (!passwordAg.equals(password)){
                Toast.makeText(this, "两次密码不一致!", Toast.LENGTH_LONG).show();
                return;
            }
            showLoading(); // Show loading indicator during registration attempt
            registerUser(phoneNumber, password);
        });

        btnGoToLogin.setOnClickListener(v -> {
            // Navigate back to LoginActivity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            // Optional: If you want to clear the back stack so pressing back from LoginActivity doesn't bring you here
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Close RegisterActivity
        });

        // Initialize loading views if you have them in activity_register.xml
        // loadingIndicator = findViewById(R.id.loading_indicator);
        // registerFormContainer = findViewById(R.id.register_form_container);
        // setupRegisterUI(); // Initial UI state
    }

    private void registerUser(String phoneNumber, String password) {
        RegisterRequest registerRequest = new RegisterRequest(phoneNumber, password);

        authService.register(registerRequest).enqueue(new Callback<ApiResponse<Number>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Number>> call, @NonNull Response<ApiResponse<Number>> response) {
                hideLoading(); // Hide loading indicator
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Number> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this,
                                    "注册成功: " + apiResponse.getMessage() + ". 请登录。",
                                    Toast.LENGTH_LONG).show();
                            // After successful registration, navigate back to LoginActivity
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish(); // Close RegisterActivity
                        });
                    } else {
                        String errorMsg = "注册失败: " + apiResponse.getMessage();
                        Log.e(TAG, errorMsg);
                        final String finalErrorMsg = errorMsg;
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, finalErrorMsg, Toast.LENGTH_LONG).show();
                            // setupRegisterUI(); // Show register UI again on business error
                        });
                    }
                } else {
                    String errorMessage = "请求失败: ";
                    try {
                        errorMessage += response.errorBody() != null ? response.errorBody().string() : "未知错误";
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorMessage += "解析错误信息失败";
                    }
                    Log.e(TAG, errorMessage);
                    String finalErrorMessage = errorMessage;
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, finalErrorMessage, Toast.LENGTH_LONG).show();
                        // setupRegisterUI(); // Show register UI again on HTTP error
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Number>> call, @NonNull Throwable t) {
                hideLoading(); // Hide loading indicator
                Log.e(TAG, "网络错误: ", t);
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this,
                            "网络错误: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // setupRegisterUI(); // Show register UI again on network error
                });
            }
        });
    }

    // region UI State Management (Copy from LoginActivity if you add loading views)
    private void showLoading() {
        // Implement show loading UI logic here if you have loadingIndicator/registerFormContainer
        // For example:
        // if (registerFormContainer != null) registerFormContainer.setVisibility(View.GONE);
        // if (loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        // Implement hide loading UI logic here
        // For example:
        // if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
        // if (registerFormContainer != null) registerFormContainer.setVisibility(View.VISIBLE);
    }

    private void setupRegisterUI() {
        // Implement initial UI setup logic here (e.g., enable inputs)
        // For example:
        // etPhoneNumber.setEnabled(true);
        // etPassword.setEnabled(true);
        // btnConfirmRegister.setEnabled(true);
        // btnGoToLogin.setEnabled(true);
    }
    // endregion
}