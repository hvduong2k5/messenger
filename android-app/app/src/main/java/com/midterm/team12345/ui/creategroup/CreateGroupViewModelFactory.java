package com.midterm.team12345.ui.creategroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.domain.repository.FriendRepository;

public class CreateGroupViewModelFactory implements ViewModelProvider.Factory {
    private final FriendRepository friendRepository;
    private final ConversationRepository conversationRepository;

    public CreateGroupViewModelFactory(FriendRepository friendRepository, ConversationRepository conversationRepository) {
        this.friendRepository = friendRepository;
        this.conversationRepository = conversationRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CreateGroupViewModel.class)) {
            return (T) new CreateGroupViewModel(friendRepository, conversationRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
