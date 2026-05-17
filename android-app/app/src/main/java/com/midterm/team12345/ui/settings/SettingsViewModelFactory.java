package com.midterm.team12345.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.domain.repository.AuthRepository;
import com.midterm.team12345.domain.repository.UserRepository;

public class SettingsViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    public SettingsViewModelFactory(UserRepository userRepository, AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(userRepository, authRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
