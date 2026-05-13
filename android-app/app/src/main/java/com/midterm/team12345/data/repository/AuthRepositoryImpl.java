package com.midterm.team12345.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.dto.AuthResponse;
import com.midterm.team12345.data.dto.LoginRequest;
import com.midterm.team12345.data.dto.RegisterRequest;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.remote.AuthApiService;
import com.midterm.team12345.util.Resource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepositoryImpl implements AuthRepository {
    private final AuthApiService authApiService;
    private final TokenManager tokenManager;

    public AuthRepositoryImpl(AuthApiService authApiService, TokenManager tokenManager) {
        this.authApiService = authApiService;
        this.tokenManager = tokenManager;
    }

    @Override
    public LiveData<Resource<AuthResponse>> login(LoginRequest loginRequest) {
        MutableLiveData<Resource<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        authApiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    tokenManager.saveToken(authResponse.getAccessToken());
                    result.setValue(Resource.success(authResponse));
                } else {
                    result.setValue(Resource.error("Login failed: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<AuthResponse>> register(RegisterRequest registerRequest) {
        MutableLiveData<Resource<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        authApiService.register(registerRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    tokenManager.saveToken(authResponse.getAccessToken());
                    result.setValue(Resource.success(authResponse));
                } else {
                    result.setValue(Resource.error("Registration failed: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return result;
    }
}
