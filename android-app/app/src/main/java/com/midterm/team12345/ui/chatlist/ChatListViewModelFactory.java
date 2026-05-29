package com.midterm.team12345.ui.chatlist;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.domain.repository.UserRepository;

public class ChatListViewModelFactory implements ViewModelProvider.Factory {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ChatListViewModelFactory(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatListViewModel.class)) {
            return (T) new ChatListViewModel(conversationRepository, userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
