package com.midterm.team12345.ui.chatdetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.data.repository.ChatRepository;

public class ChatDetailViewModelFactory implements ViewModelProvider.Factory {
    private final ChatRepository repository;

    public ChatDetailViewModelFactory(ChatRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatDetailViewModel.class)) {
            return (T) new ChatDetailViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
