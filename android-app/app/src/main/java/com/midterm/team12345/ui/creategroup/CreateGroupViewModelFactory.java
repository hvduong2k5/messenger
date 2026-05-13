package com.midterm.team12345.ui.creategroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.data.repository.ChatRepository;

public class CreateGroupViewModelFactory implements ViewModelProvider.Factory {
    private final ChatRepository repository;

    public CreateGroupViewModelFactory(ChatRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CreateGroupViewModel.class)) {
            return (T) new CreateGroupViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
