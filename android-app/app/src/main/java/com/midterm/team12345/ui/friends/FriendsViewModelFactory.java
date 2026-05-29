package com.midterm.team12345.ui.friends;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.domain.repository.UserRepository;

public class FriendsViewModelFactory implements ViewModelProvider.Factory {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public FriendsViewModelFactory(FriendRepository friendRepository, UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FriendsViewModel.class)) {
            return (T) new FriendsViewModel(friendRepository, userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
