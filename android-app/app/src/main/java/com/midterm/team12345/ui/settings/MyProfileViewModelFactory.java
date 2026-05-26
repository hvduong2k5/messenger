package com.midterm.team12345.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.domain.repository.AuthRepository;
import com.midterm.team12345.domain.repository.UserRepository;

public class MyProfileViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final TokenManager tokenManager;

    public MyProfileViewModelFactory(UserRepository userRepository, AuthRepository authRepository, TokenManager tokenManager) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MyProfileViewModel.class)) {
            return (T) new MyProfileViewModel(userRepository, authRepository, tokenManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
