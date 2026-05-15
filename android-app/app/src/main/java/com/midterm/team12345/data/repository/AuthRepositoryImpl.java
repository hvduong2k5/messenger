package com.midterm.team12345.data.repository;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.midterm.team12345.data.dto.response.AuthResponseDTO;
import com.midterm.team12345.data.dto.request.LoginRequestDTO;
import com.midterm.team12345.data.dto.request.RegisterRequestDTO;
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
            AuthApiService apiService = RetrofitClient.getAuthApiService(application);
            TokenManager tokenManager = new TokenManager(application);
            instance = new AuthRepositoryImpl(apiService, tokenManager);
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
                    AuthResponseDTO authResponseDTO = response.body();
                    tokenManager.saveToken(authResponseDTO.getAccessToken());
                    result.setValue(Resource.success(authResponseDTO));
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
                    AuthResponseDTO authResponseDTO = response.body();
                    tokenManager.saveToken(authResponseDTO.getAccessToken());
                    result.setValue(Resource.success(authResponseDTO));
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
            public void onFailure(@NonNull Call<AuthResponseDTO> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network connection error", null));
            }
        });

        return result;
    }
}
