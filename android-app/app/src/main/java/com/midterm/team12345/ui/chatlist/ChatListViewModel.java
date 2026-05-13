package com.midterm.team12345.ui.chatlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.midterm.team12345.data.dto.ConversationResponse;
import com.midterm.team12345.data.repository.ChatRepository;
import com.midterm.team12345.util.Resource;
import java.util.List;

public class ChatListViewModel extends ViewModel {
    private final ChatRepository chatRepository;
    private final MutableLiveData<Resource<List<ConversationResponse>>> _conversationState = new MutableLiveData<>();
    public final LiveData<Resource<List<ConversationResponse>>> conversationState = _conversationState;

    public ChatListViewModel(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public void fetchConversations() {
        _conversationState.setValue(Resource.loading(null));
        // Use the repository to fetch data. 
        // For now, we'll assume the repository provides a LiveData that we can observe or pipe.
        // In a real scenario, this might involve a UseCase or direct repo call.
        chatRepository.getConversations().observeForever(resource -> {
            _conversationState.setValue(resource);
        });
    }
}
