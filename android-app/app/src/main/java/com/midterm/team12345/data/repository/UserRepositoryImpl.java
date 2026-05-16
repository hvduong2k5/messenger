package com.midterm.team12345.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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

    private UserRepositoryImpl(UserApiService userApiService) {
        this.userApiService = userApiService;
    }

    public static synchronized UserRepositoryImpl getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepositoryImpl(RetrofitClient.getUserApiService(context));
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
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Lỗi lấy thông tin cá nhân", null));
            }
            @Override
            public void onFailure(Call<UserProfileResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> getUserProfile(Long id) {
        MutableLiveData<Resource<UserProfileResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        userApiService.getUserProfile(id).enqueue(new Callback<UserProfileResponseDTO>() {
            @Override
            public void onResponse(Call<UserProfileResponseDTO> call, Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Lỗi lấy thông tin người dùng", null));
            }
            @Override
            public void onFailure(Call<UserProfileResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> updateProfile(UpdateProfileRequestDTO request) {
        MutableLiveData<Resource<UserProfileResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        userApiService.updateProfile(request).enqueue(new Callback<UserProfileResponseDTO>() {
            @Override
            public void onResponse(Call<UserProfileResponseDTO> call, Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Lỗi cập nhật profile", null));
            }
            @Override
            public void onFailure(Call<UserProfileResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> updateAvatar(MultipartBody.Part file) {
        MutableLiveData<Resource<UserProfileResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        userApiService.updateAvatar(file).enqueue(new Callback<UserProfileResponseDTO>() {
            @Override
            public void onResponse(Call<UserProfileResponseDTO> call, Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Lỗi cập nhật ảnh đại diện", null));
            }
            @Override
            public void onFailure(Call<UserProfileResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<PageResponse<UserSearchResponseDTO>>> searchUsers(String query, int page, int size) {
        MutableLiveData<Resource<PageResponse<UserSearchResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        userApiService.searchUsers(query, page, size).enqueue(new Callback<PageResponse<UserSearchResponseDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<UserSearchResponseDTO>> call, Response<PageResponse<UserSearchResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Lỗi tìm kiếm người dùng", null));
            }
            @Override
            public void onFailure(Call<PageResponse<UserSearchResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }
}
