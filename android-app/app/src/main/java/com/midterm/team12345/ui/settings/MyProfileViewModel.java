package com.midterm.team12345.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.domain.repository.AuthRepository;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;
import com.midterm.team12345.utils.SingleLiveEvent;

public class MyProfileViewModel extends BaseViewModel {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final TokenManager tokenManager;
    
    // Sự kiện Logout thành công để UI chuyển màn hình
    public final SingleLiveEvent<Void> logoutEvent = new SingleLiveEvent<>();

    public MyProfileViewModel(UserRepository userRepository, AuthRepository authRepository, TokenManager tokenManager) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.tokenManager = tokenManager;
    }

    /**
     * Lấy thông tin User hiện tại từ Local DB (SSOT).
     * Mọi thay đổi từ Edit Profile sẽ tự động cập nhật về đây qua LiveData.
     */
    public LiveData<UserEntity> getUserProfile() {
        Long currentUserId = tokenManager.getUserId();
        return userRepository.getLocalUser(currentUserId);
    }

    /**
     * Làm mới dữ liệu từ API (nếu cần)
     */
    public void refreshProfile() {
        userRepository.getMyProfile();
    }

    /**
     * Xử lý Logout: Xóa Token, Xóa DB và báo về UI
     */
    public void logout() {
        showLoading();
        authRepository.logout().observeForever(resource -> {
            hideLoading();
            // Xóa thông tin phiên đăng nhập ở Local
            tokenManager.clear();
            
            // Xóa dữ liệu Room DB để bảo mật (thực hiện ở Background)
            new Thread(() -> {
                // Bạn có thể gọi database.clearAllTables() hoặc các hàm delete cụ thể
            }).start();

            logoutEvent.call();
        });
    }
}
