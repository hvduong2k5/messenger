package com.midterm.team12345.data.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.remote.api.AuthApiService;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.dto.request.ForgotPasswordRequestDTO;
import com.midterm.team12345.data.remote.dto.request.LoginRequestDTO;
import com.midterm.team12345.data.remote.dto.request.RegisterRequestDTO;
import com.midterm.team12345.data.remote.dto.request.ResetPasswordRequestDTO;
import com.midterm.team12345.data.remote.dto.response.AuthResponseDTO;
import com.midterm.team12345.data.remote.dto.response.ForgotPasswordResponseDTO;
import com.midterm.team12345.domain.repository.AuthRepository;
import com.midterm.team12345.utils.Resource;

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
            instance = new AuthRepositoryImpl(
                    RetrofitClient.getAuthApiService(application),
                    new TokenManager(application)
            );
        }
        return instance;
    }

    @Override
    public LiveData<Resource<AuthResponseDTO>> login(LoginRequestDTO loginRequestDTO) {
        MutableLiveData<Resource<AuthResponseDTO>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        authApiService.login(loginRequestDTO).enqueue(new Callback<AuthResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponseDTO> call, @NonNull Response<AuthResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponseDTO authResponse = response.body();
                    tokenManager.saveToken(authResponse.getAccessToken());
                    if (authResponse.getUser() != null) {
                        tokenManager.saveUsername(authResponse.getUser().getUsername());
                        tokenManager.saveUserId(authResponse.getUser().getId());
                    }
                    result.setValue(Resource.success(authResponse));
                } else {
                    result.setValue(Resource.error("Login failed: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponseDTO> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<AuthResponseDTO>> register(RegisterRequestDTO registerRequestDTO) {
        MutableLiveData<Resource<AuthResponseDTO>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        authApiService.register(registerRequestDTO).enqueue(new Callback<AuthResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponseDTO> call, @NonNull Response<AuthResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Registration failed: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponseDTO> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<ForgotPasswordResponseDTO>> forgotPassword(ForgotPasswordRequestDTO request) {
        MutableLiveData<Resource<ForgotPasswordResponseDTO>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        authApiService.forgotPassword(request).enqueue(new Callback<ForgotPasswordResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ForgotPasswordResponseDTO> call, @NonNull Response<ForgotPasswordResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Request failed: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForgotPasswordResponseDTO> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<ForgotPasswordResponseDTO>> resetPassword(ResetPasswordRequestDTO request) {
        MutableLiveData<Resource<ForgotPasswordResponseDTO>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        authApiService.resetPassword(request).enqueue(new Callback<ForgotPasswordResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ForgotPasswordResponseDTO> call, @NonNull Response<ForgotPasswordResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Reset failed: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForgotPasswordResponseDTO> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<Void>> logout() {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        authApiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                tokenManager.clear();
                result.setValue(Resource.success(null));
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                tokenManager.clear(); // Vẫn xóa token cục bộ nếu logout API lỗi
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return result;
    }
}
