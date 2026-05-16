package com.midterm.team12345.ui.conversationsettings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.domain.repository.ChatRepository;

public class ConversationSettingsViewModelFactory implements ViewModelProvider.Factory {
    private final ChatRepository repository;

    public ConversationSettingsViewModelFactory(ChatRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ConversationSettingsViewModel.class)) {
            return (T) new ConversationSettingsViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
