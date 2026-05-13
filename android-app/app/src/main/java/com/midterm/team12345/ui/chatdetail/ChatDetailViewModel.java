package com.midterm.team12345.ui.chatdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.midterm.team12345.data.dto.MessageResponse;
import com.midterm.team12345.util.Resource;
import java.util.ArrayList;
import java.util.List;

public class ChatDetailViewModel extends ViewModel {

    private final MutableLiveData<Resource<List<MessageResponse>>> _messageState = new MutableLiveData<>();
    public final LiveData<Resource<List<MessageResponse>>> messageState = _messageState;

    private final List<MessageResponse> messages = new ArrayList<>();

    public void loadMessages(Long conversationId) {
        _messageState.setValue(Resource.loading(null));
        
        // Mock data
        messages.clear();
        messages.add(new MessageResponse(1L, 100L, "Hello, Jacob!", System.currentTimeMillis() - 10000));
        messages.add(new MessageResponse(2L, 100L, "How are you doing?", System.currentTimeMillis() - 5000));
        messages.add(new MessageResponse(3L, 1L, "I'm doing great! How about you?", System.currentTimeMillis()));

        _messageState.setValue(Resource.success(new ArrayList<>(messages)));
    }

    public void sendMessage(String text, Long senderId) {
        if (text == null || text.trim().isEmpty()) return;

        MessageResponse newMessage = new MessageResponse(
                (long) (messages.size() + 1),
                senderId,
                text,
                System.currentTimeMillis()
        );
        messages.add(newMessage);
        _messageState.setValue(Resource.success(new ArrayList<>(messages)));
    }
}
