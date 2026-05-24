package com.midterm.team12345.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.data.remote.dto.request.UpdateProfileRequestDTO;
import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

public class EditProfileViewModel extends BaseViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<Resource<UserProfileResponseDTO>> _updateResult = new MutableLiveData<>();
    public final LiveData<Resource<UserProfileResponseDTO>> updateResult = _updateResult;

    public EditProfileViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<UserEntity> getLocalUser(Long userId) {
        return userRepository.getLocalUser(userId);
    }

    public void updateProfile(String email, String status, String password, String oldPassword) {
        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO(email, password, oldPassword, status);
        _updateResult.setValue(Resource.loading(null));
        userRepository.updateProfile(request).observeForever(result -> {
            _updateResult.setValue(result);
            if (result.status == Resource.Status.SUCCESS) {
                hideLoading();
            } else if (result.status == Resource.Status.ERROR) {
                hideLoading();
                setError(result.message);
            }
        });
    }
}
