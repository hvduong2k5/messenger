package com.midterm.team12345.ui.chatlist;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.data.repository.ChatRepository;

public class ChatListViewModelFactory implements ViewModelProvider.Factory {
    private final ChatRepository repository;

    public ChatListViewModelFactory(ChatRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatListViewModel.class)) {
            return (T) new ChatListViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
