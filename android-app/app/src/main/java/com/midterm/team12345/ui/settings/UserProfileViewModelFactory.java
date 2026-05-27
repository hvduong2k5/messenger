package com.midterm.team12345.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.domain.repository.UserRepository;

public class UserProfileViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public UserProfileViewModelFactory(UserRepository userRepository, FriendRepository friendRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserProfileViewModel.class)) {
            return (T) new UserProfileViewModel(userRepository, friendRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
