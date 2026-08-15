package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {

    private static final String TAG = "TokenManager";
    private static final String PREF_FILE_NAME = "auth_prefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_FILE_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences, falling back to regular SharedPreferences.", e);
            // Fallback to regular SharedPreferences (less secure) or handle error
            sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
    }

    public void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.putString(REFRESH_TOKEN_KEY, refreshToken);
        editor.apply();
        Log.d(TAG, "Tokens saved.");
    }

    public String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null);
    }

    public void clearTokens() {
        sharedPreferences.edit().clear().apply();
        Log.d(TAG, "Tokens cleared.");
    }

    public boolean hasTokens() {
        return getAccessToken() != null && getRefreshToken() != null;
    }
}