package com.midterm.team12345.ui.conversationsettings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.domain.repository.FriendRepository;

public class ConversationSettingsViewModelFactory implements ViewModelProvider.Factory {
    private final ConversationRepository conversationRepository;
    private final FriendRepository friendRepository;

    public ConversationSettingsViewModelFactory(ConversationRepository conversationRepository, FriendRepository friendRepository) {
        this.conversationRepository = conversationRepository;
        this.friendRepository = friendRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ConversationSettingsViewModel.class)) {
            return (T) new ConversationSettingsViewModel(conversationRepository, friendRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
