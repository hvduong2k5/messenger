package com.midterm.team12345.ui.chatdetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.domain.repository.MessageRepository;
import com.midterm.team12345.domain.repository.UserRepository;

import org.jetbrains.annotations.Contract;

public class ChatDetailViewModelFactory implements ViewModelProvider.Factory {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @Contract(pure = true)
    public ChatDetailViewModelFactory(MessageRepository messageRepository,
                                     ConversationRepository conversationRepository,
                                     UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatDetailViewModel.class)) {
            return (T) new ChatDetailViewModel(messageRepository, conversationRepository, userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
