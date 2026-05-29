package com.midterm.team12345.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ID = "user_id";
    
    private static volatile TokenManager instance;
    private SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static TokenManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TokenManager.class) {
                if (instance == null) {
                    instance = new TokenManager(context);
                }
            }
        }
        return instance;
    }

    public void saveToken(String token) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply();
        }
    }

    public String getToken() {
        return sharedPreferences != null ? sharedPreferences.getString(KEY_ACCESS_TOKEN, null) : null;
    }

    public void saveUsername(String username) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
        }
    }

    public String getUsername() {
        return sharedPreferences != null ? sharedPreferences.getString(KEY_USERNAME, null) : null;
    }

    public void saveUserId(Long userId) {
        if (sharedPreferences != null && userId != null) {
            sharedPreferences.edit().putLong(KEY_USER_ID, userId).apply();
        }
    }

    public Long getUserId() {
        if (sharedPreferences != null && sharedPreferences.contains(KEY_USER_ID)) {
            return sharedPreferences.getLong(KEY_USER_ID, -1L);
        }
        return null;
    }

    public void clear() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
        }
    }
}
