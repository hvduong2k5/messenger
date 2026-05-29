package com.midterm.team12345.data.repository;

import android.app.Application;
import androidx.annotation.NonNull;
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
import okhttp3.RequestBody;
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
            public void onResponse(@NonNull Call<UserProfileResponseDTO> call, @NonNull Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveUserToLocal(response.body());
                    data.setValue(Resource.success(response.body()));
                } else {
                    data.setValue(Resource.error("Lỗi đồng bộ thông tin", null));
                }
            }
            @Override
            public void onFailure(@NonNull Call<UserProfileResponseDTO> call, @NonNull Throwable t) {
                data.setValue(Resource.error("Lỗi kết nối", null));
            }
        });
        return data;
    }

    private void saveUserToLocal(UserProfileResponseDTO dto) {
        new Thread(() -> {
            // SSOT: Merge with existing data to avoid losing fields not in the profile DTO (like online status)
            UserEntity entity = database.userDao().getUserByIdSync(dto.getId());
            if (entity == null) {
                entity = new UserEntity();
                entity.setId(dto.getId());
            }
            
            entity.setUsername(dto.getUsername());
            entity.setEmail(dto.getEmail());
            entity.setAvatarUrl(dto.getAvatarUrl());
            entity.setBio(dto.getStatus());
            
            if (dto.getFriendshipStatus() != null) {
                entity.setFriendshipStatus(dto.getFriendshipStatus());
                entity.setIsFriend(dto.getFriendshipStatus() == com.midterm.team12345.data.remote.dto.response.FriendshipStatus.FRIEND);
            }
            
            database.userDao().insertUser(entity);
        }).start();
    }

    @Override
    public LiveData<UserEntity> getLocalUser(Long userId) {
        return database.userDao().getUserById(userId);
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> getUserProfile(Long id) {
        MutableLiveData<Resource<UserProfileResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        userApiService.getUserProfileById(id).enqueue(new Callback<UserProfileResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponseDTO> call, @NonNull Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveUserToLocal(response.body());
                    data.setValue(Resource.success(response.body()));
                } else {
                    data.setValue(Resource.error("Không thể tải thông tin người dùng", null));
                }
            }
            @Override
            public void onFailure(@NonNull Call<UserProfileResponseDTO> call, @NonNull Throwable t) {
                data.setValue(Resource.error("Lỗi kết nối: " + t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> updateProfile(UpdateProfileRequestDTO request) {
        MutableLiveData<Resource<UserProfileResponseDTO>> data = new MutableLiveData<>();
        userApiService.updateProfile(request).enqueue(new Callback<UserProfileResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponseDTO> call, @NonNull Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveUserToLocal(response.body());
                    data.setValue(Resource.success(response.body()));
                }
            }
            @Override
            public void onFailure(@NonNull Call<UserProfileResponseDTO> call, @NonNull Throwable t) {}
        });
        return data;
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> updateAvatar(MultipartBody.Part file) {
        MutableLiveData<Resource<UserProfileResponseDTO>> data = new MutableLiveData<>();
        userApiService.updateAvatar(file).enqueue(new Callback<UserProfileResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponseDTO> call, @NonNull Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveUserToLocal(response.body());
                    data.setValue(Resource.success(response.body()));
                }
            }
            @Override
            public void onFailure(@NonNull Call<UserProfileResponseDTO> call, @NonNull Throwable t) {}
        });
        return data;
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> updateProfileComplete(MultipartBody.Part avatar, RequestBody dataPayload) {
        MutableLiveData<Resource<UserProfileResponseDTO>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        userApiService.updateProfileComplete(avatar, dataPayload).enqueue(new Callback<UserProfileResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponseDTO> call, @NonNull Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveUserToLocal(response.body());
                    result.setValue(Resource.success(response.body()));
                } else {
                    String msg = "Cập nhật thất bại";
                    if (response.code() == 400) msg = "Mật khẩu hiện tại không chính xác hoặc dữ liệu không hợp lệ";
                    result.setValue(Resource.error(msg, null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponseDTO> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }

    @Override
    public LiveData<Resource<PageResponse<UserSearchResponseDTO>>> searchUsers(String query, int page, int size) {
        MutableLiveData<Resource<PageResponse<UserSearchResponseDTO>>> data = new MutableLiveData<>();
        if (query == null || query.trim().length() < 2) {
            PageResponse<UserSearchResponseDTO> emptyPage = new PageResponse<>();
            emptyPage.setContent(new java.util.ArrayList<>());
            emptyPage.setTotalElements(0L);
            emptyPage.setTotalPages(0);
            data.setValue(Resource.success(emptyPage));
            return data;
        }
        data.setValue(Resource.loading(null));
        userApiService.searchUsers(query, page, size).enqueue(new Callback<PageResponse<UserSearchResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<UserSearchResponseDTO>> call, @NonNull Response<PageResponse<UserSearchResponseDTO>> response) {
                if (response.isSuccessful()) {
                    data.setValue(Resource.success(response.body()));
                } else {
                    data.setValue(Resource.error("Không thể tìm kiếm người dùng", null));
                }
            }
            @Override
            public void onFailure(@NonNull Call<PageResponse<UserSearchResponseDTO>> call, @NonNull Throwable t) {
                data.setValue(Resource.error("Lỗi kết nối mạng", null));
            }
        });
        return data;
    }
}
