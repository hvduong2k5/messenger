package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.data.remote.dto.request.UpdateProfileRequestDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.utils.Resource;
import okhttp3.MultipartBody;

public interface UserRepository {
    LiveData<Resource<UserProfileResponseDTO>> getMyProfile();
    LiveData<UserEntity> getLocalUser(Long userId);
    LiveData<Resource<UserProfileResponseDTO>> getUserProfile(Long id);
    LiveData<Resource<UserProfileResponseDTO>> updateProfile(UpdateProfileRequestDTO request);
    LiveData<Resource<UserProfileResponseDTO>> updateAvatar(MultipartBody.Part file);
    LiveData<Resource<PageResponse<UserSearchResponseDTO>>> searchUsers(String query, int page, int size);
}
