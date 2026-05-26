package com.midterm.team12345.ui.groupinfo;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.domain.repository.ConversationRepository;

public class GroupInfoViewModelFactory implements ViewModelProvider.Factory {
    private final ConversationRepository repository;

    public GroupInfoViewModelFactory(ConversationRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GroupInfoViewModel.class)) {
            return (T) new GroupInfoViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
