package com.midterm.team12345.data.repository;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.midterm.team12345.data.dto.AuthResponse;
import com.midterm.team12345.data.dto.LoginRequest;
import com.midterm.team12345.data.dto.RegisterRequest;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.remote.AuthApiService;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.util.Resource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepositoryImpl implements AuthRepository {
    private static AuthRepositoryImpl instance;
    private final AuthApiService authApiService;
    private final TokenManager tokenManager;

    private AuthRepositoryImpl(AuthApiService authApiService, TokenManager tokenManager) {
        this.authApiService = authApiService;
        this.tokenManager = tokenManager;
    }

    public static synchronized AuthRepositoryImpl getInstance(Application application) {
        if (instance == null) {
            AuthApiService apiService = RetrofitClient.getRetrofitInstance().create(AuthApiService.class);
            TokenManager tokenManager = new TokenManager(application);
            instance = new AuthRepositoryImpl(apiService, tokenManager);
        }
        return instance;
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
                    String errorMsg = "Login failed";
                    if (response.code() == 401) {
                        errorMsg = "Incorrect email or password";
                    } else if (response.code() == 400) {
                        errorMsg = "Invalid request data";
                    }
                    result.setValue(Resource.error(errorMsg, null));
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
                    String errorMsg = "Registration failed";
                    if (response.code() == 409) {
                        errorMsg = "User already exists";
                    } else if (response.code() == 400) {
                        errorMsg = "Registration data is invalid";
                    }
                    result.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network connection error", null));
            }
        });

        return result;
    }
}
