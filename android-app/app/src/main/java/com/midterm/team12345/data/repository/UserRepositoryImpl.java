package com.midterm.team12345.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.local.database.MessengerDatabase;
import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.api.UserApiService;
import com.midterm.team12345.data.remote.dto.request.UpdateProfileRequestDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.utils.Resource;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepositoryImpl implements UserRepository {
    private static UserRepositoryImpl instance;
    private final UserApiService userApiService;
    private final MessengerDatabase database;

    private UserRepositoryImpl(UserApiService userApiService, MessengerDatabase database) {
        this.userApiService = userApiService;
        this.database = database;
    }

    public static synchronized UserRepositoryImpl getInstance(Application application) {
        if (instance == null) {
            instance = new UserRepositoryImpl(
                    RetrofitClient.getUserApiService(application),
                    MessengerDatabase.getInstance(application)
            );
        }
        return instance;
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> getMyProfile() {
        MutableLiveData<Resource<UserProfileResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        userApiService.getMyProfile().enqueue(new Callback<UserProfileResponseDTO>() {
            @Override
            public void onResponse(Call<UserProfileResponseDTO> call, Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveUserToLocal(response.body());
                    data.setValue(Resource.success(response.body()));
                } else {
                    data.setValue(Resource.error("Lỗi đồng bộ thông tin", null));
                }
            }
            @Override
            public void onFailure(Call<UserProfileResponseDTO> call, Throwable t) {
                data.setValue(Resource.error("Lỗi kết nối", null));
            }
        });
        return data;
    }

    private void saveUserToLocal(UserProfileResponseDTO dto) {
        new Thread(() -> {
            UserEntity entity = new UserEntity();
            entity.setId(dto.getId());
            entity.setUsername(dto.getUsername());
            entity.setEmail(dto.getEmail());
            entity.setAvatarUrl(dto.getAvatarUrl());
            entity.setBio(dto.getBio());
            entity.setIsOnline(true);
            database.userDao().insertUser(entity);
        }).start();
    }

    @Override
    public LiveData<UserEntity> getLocalUser(Long userId) {
        return database.userDao().getUserById(userId);
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> getUserProfile(Long id) {
        // Implement similarly...
        return new MutableLiveData<>();
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> updateProfile(UpdateProfileRequestDTO request) {
        MutableLiveData<Resource<UserProfileResponseDTO>> data = new MutableLiveData<>();
        userApiService.updateProfile(request).enqueue(new Callback<UserProfileResponseDTO>() {
            @Override
            public void onResponse(Call<UserProfileResponseDTO> call, Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveUserToLocal(response.body());
                    data.setValue(Resource.success(response.body()));
                }
            }
            @Override
            public void onFailure(Call<UserProfileResponseDTO> call, Throwable t) {}
        });
        return data;
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> updateAvatar(MultipartBody.Part file) {
        return new MutableLiveData<>();
    }

    @Override
    public LiveData<Resource<PageResponse<UserSearchResponseDTO>>> searchUsers(String query, int page, int size) {
        return new MutableLiveData<>();
    }
}
