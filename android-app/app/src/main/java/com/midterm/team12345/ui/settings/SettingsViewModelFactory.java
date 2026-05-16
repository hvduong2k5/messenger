package com.midterm.team12345.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.domain.repository.AuthRepository;
import com.midterm.team12345.domain.repository.ChatRepository;

public class SettingsViewModelFactory implements ViewModelProvider.Factory {
    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;

    public SettingsViewModelFactory(ChatRepository chatRepository, AuthRepository authRepository) {
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(chatRepository, authRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
