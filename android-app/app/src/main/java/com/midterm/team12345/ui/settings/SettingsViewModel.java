package com.midterm.team12345.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.domain.repository.AuthRepository;
import com.midterm.team12345.domain.repository.ChatRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

public class SettingsViewModel extends BaseViewModel {
    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<UserProfileResponseDTO>> _profileState = new MutableLiveData<>();
    public final LiveData<Resource<UserProfileResponseDTO>> profileState = _profileState;

    private final MutableLiveData<Resource<Void>> _logoutState = new MutableLiveData<>();
    public final LiveData<Resource<Void>> logoutState = _logoutState;

    public SettingsViewModel(ChatRepository chatRepository, AuthRepository authRepository) {
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;
    }

    public void fetchMyProfile() {
        showLoading();
        chatRepository.getMyProfile().observeForever(resource -> {
            _profileState.setValue(resource);
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
            }
        });
    }

    public void logout() {
        showLoading();
        authRepository.logout().observeForever(resource -> {
            _logoutState.setValue(resource);
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
            }
        });
    }
}
