package com.midterm.team12345.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.data.remote.dto.request.UpdateProfileRequestDTO;
import com.midterm.team12345.domain.repository.AuthRepository;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;
import com.midterm.team12345.utils.SingleLiveEvent;

public class UserProfileViewModel extends BaseViewModel {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final TokenManager tokenManager;

    // State: Thông tin người dùng hiện tại
    private final LiveData<UserEntity> _user;
    public final LiveData<UserEntity> user;

    // Event: Điều hướng sau khi logout
    public final SingleLiveEvent<Void> logoutEvent = new SingleLiveEvent<>();

    public UserProfileViewModel(UserRepository userRepository, AuthRepository authRepository, TokenManager tokenManager) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.tokenManager = tokenManager;

        // Lấy dữ liệu tức thì từ Room DB (SSOT)
        Long userId = tokenManager.getUserId();
        this._user = userRepository.getLocalUser(userId);
        this.user = _user;
    }

    /**
     * Làm mới dữ liệu từ API và lưu vào Room
     */
    public void refreshProfile() {
        userRepository.getMyProfile();
    }

    /**
     * Cập nhật trạng thái/Bio
     */
    public void updateBio(String bio) {
        showLoading();
        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setStatus(bio);
        userRepository.updateProfile(request).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                if (resource.status == Resource.Status.ERROR) {
                    setError(resource.message);
                }
            }
        });
    }

    /**
     * Xử lý đăng xuất chuyên sâu
     */
    public void logout() {
        showLoading();
        authRepository.logout().observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                // clearLocalData đã được gọi trong AuthRepositoryImpl
                logoutEvent.call();
            }
        });
    }
}
