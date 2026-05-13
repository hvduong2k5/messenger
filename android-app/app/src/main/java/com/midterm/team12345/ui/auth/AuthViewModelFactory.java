package com.midterm.team12345.ui.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.data.repository.AuthRepository;

public class AuthViewModelFactory implements ViewModelProvider.Factory {
    private final AuthRepository authRepository;

    public AuthViewModelFactory(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(authRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
