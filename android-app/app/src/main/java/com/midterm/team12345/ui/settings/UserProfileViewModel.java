package com.midterm.team12345.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;
import com.midterm.team12345.utils.SingleLiveEvent;

public class UserProfileViewModel extends BaseViewModel {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private Long targetUserId;

    public final SingleLiveEvent<String> statusMessageEvent = new SingleLiveEvent<>();

    public UserProfileViewModel(UserRepository userRepository, FriendRepository friendRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }

    public void setTargetUserId(Long userId) {
        this.targetUserId = userId;
    }

    /**
     * Observe user data from Room DB (SSOT)
     */
    public LiveData<UserEntity> getUser() {
        return userRepository.getLocalUser(targetUserId);
    }

    /**
     * Refresh user data from API
     */
    public void fetchUserProfile() {
        showLoading();
        userRepository.getUserProfile(targetUserId).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                if (resource.status == Resource.Status.ERROR) {
                    setError(resource.message);
                }
            }
        });
    }

    public void sendFriendRequest() {
        showLoading();
        friendRepository.sendFriendRequest(targetUserId).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                if (resource.status == Resource.Status.SUCCESS) {
                    statusMessageEvent.setValue("Đã gửi lời mời kết bạn");
                    fetchUserProfile(); // Refresh data to update status in DB and UI
                } else {
                    setError(resource.message);
                }
            }
        });
    }

    public void acceptFriendRequest() {
        showLoading();
        friendRepository.acceptFriendRequest(targetUserId).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                if (resource.status == Resource.Status.SUCCESS) {
                    statusMessageEvent.setValue("Đã chấp nhận lời mời kết bạn");
                    fetchUserProfile();
                } else {
                    setError(resource.message);
                }
            }
        });
    }

    public void rejectFriendRequest() {
        showLoading();
        friendRepository.rejectFriendRequest(targetUserId).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                if (resource.status == Resource.Status.SUCCESS) {
                    statusMessageEvent.setValue("Đã từ chối lời mời kết bạn");
                    fetchUserProfile();
                } else {
                    setError(resource.message);
                }
            }
        });
    }

    public void unfriend() {
        showLoading();
        friendRepository.unfriend(targetUserId).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                if (resource.status == Resource.Status.SUCCESS) {
                    statusMessageEvent.setValue("Đã hủy kết bạn");
                    fetchUserProfile();
                } else {
                    setError(resource.message);
                }
            }
        });
    }

    public void cancelFriendRequest() {
        showLoading();
        friendRepository.cancelFriendRequest(targetUserId).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                if (resource.status == Resource.Status.SUCCESS) {
                    statusMessageEvent.setValue("Đã hủy yêu cầu kết bạn");
                    fetchUserProfile();
                } else {
                    setError(resource.message);
                }
            }
        });
    }
}
