package com.midterm.team12345.ui.chatdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.midterm.team12345.data.dto.MessageRequestDTO;
import com.midterm.team12345.data.dto.MessageResponse;
import com.midterm.team12345.data.dto.MqttMessageDTO;
import com.midterm.team12345.data.repository.ChatRepository;
import com.midterm.team12345.util.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatDetailViewModel extends ViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<Resource<List<MessageResponse>>> _messageState = new MutableLiveData<>();
    public final LiveData<Resource<List<MessageResponse>>> messageState = _messageState;

    public ChatDetailViewModel(ChatRepository repository) {
        this.repository = repository;
        observeRealTimeMessages();
    }

    private void observeRealTimeMessages() {
        repository.getRealTimeMessages().observeForever(mqttMessage -> {
            if (mqttMessage != null && "NEW_MESSAGE".equals(mqttMessage.getType())) {
                Resource<List<MessageResponse>> currentState = _messageState.getValue();
                List<MessageResponse> currentMessages = new ArrayList<>();
                if (currentState != null && currentState.data != null) {
                    currentMessages.addAll(currentState.data);
                }
                
                // Tránh thêm tin nhắn trùng lặp nếu nó đã được thêm qua API callback
                boolean exists = currentMessages.stream()
                        .anyMatch(m -> mqttMessage.getPayload().equals(m.getContent()) && 
                                     Math.abs(System.currentTimeMillis() - m.getCreatedAt()) < 2000);
                
                if (!exists) {
                    MessageResponse newMessage = new MessageResponse(
                            System.currentTimeMillis(), // Temporary ID
                            Long.parseLong(mqttMessage.getSender()),
                            mqttMessage.getPayload(),
                            System.currentTimeMillis()
                    );
                    currentMessages.add(newMessage);
                    _messageState.setValue(Resource.success(currentMessages));
                }
            }
        });
    }

    public void loadMessages(Long conversationId) {
        _messageState.setValue(Resource.loading(null));
        repository.getMessages(conversationId).observeForever(resource -> {
            _messageState.setValue(resource);
        });
    }

    public void sendMessage(String text, Long conversationId, Long senderId) {
        if (text == null || text.trim().isEmpty()) return;
        
        String clientMsgId = UUID.randomUUID().toString();
        MessageRequestDTO request = new MessageRequestDTO(senderId, conversationId, text, clientMsgId);
        
        repository.sendMessage(request).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // Cập nhật UI với tin nhắn mới từ server
                Resource<List<MessageResponse>> currentState = _messageState.getValue();
                List<MessageResponse> currentMessages = new ArrayList<>();
                if (currentState != null && currentState.data != null) {
                    currentMessages.addAll(currentState.data);
                }
                currentMessages.add(resource.data);
                _messageState.setValue(Resource.success(currentMessages));
            }
        });
    }
}
