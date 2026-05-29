package com.midterm.team12345.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.domain.repository.UserRepository;

public class EditProfileViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;

    public EditProfileViewModelFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EditProfileViewModel.class)) {
            return (T) new EditProfileViewModel(userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
