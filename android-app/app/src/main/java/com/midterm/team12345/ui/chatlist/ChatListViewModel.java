package com.midterm.team12345.ui.chatlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.midterm.team12345.data.dto.ConversationResponse;
import com.midterm.team12345.data.dto.MqttMessageDTO;
import com.midterm.team12345.data.repository.ChatRepository;
import com.midterm.team12345.util.Resource;

import java.util.ArrayList;
import java.util.List;

public class ChatListViewModel extends ViewModel {
    private final ChatRepository chatRepository;
    private final MutableLiveData<Resource<List<ConversationResponse>>> _conversationState = new MutableLiveData<>();
    public final LiveData<Resource<List<ConversationResponse>>> conversationState = _conversationState;

    public ChatListViewModel(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
        observeRealTimeMessages();
    }

    private void observeRealTimeMessages() {
        chatRepository.getRealTimeMessages().observeForever(mqttMessage -> {
            if (mqttMessage != null && "NEW_MESSAGE".equals(mqttMessage.getType())) {
                updateConversationList(mqttMessage);
            }
        });
    }

    private void updateConversationList(MqttMessageDTO mqttMessage) {
        Resource<List<ConversationResponse>> currentResource = _conversationState.getValue();
        if (currentResource != null && currentResource.status == Resource.Status.SUCCESS && currentResource.data != null) {
            List<ConversationResponse> list = new ArrayList<>(currentResource.data);
            Long senderId = Long.parseLong(mqttMessage.getSender());
            
            int foundIndex = -1;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getConversationId().equals(senderId)) { // Simplified: senderId as conversationId for 1-1
                    foundIndex = i;
                    break;
                }
            }

            if (foundIndex != -1) {
                ConversationResponse old = list.remove(foundIndex);
                ConversationResponse updated = new ConversationResponse(
                        old.getConversationId(),
                        old.getConversationName(),
                        mqttMessage.getPayload(),
                        old.getAvatarUrl(),
                        System.currentTimeMillis(),
                        (old.getUnreadCount() != null ? old.getUnreadCount() : 0) + 1,
                        old.getDeleted(),
                        old.getEdited(),
                        old.getGroup()
                );
                list.add(0, updated);
            }
            _conversationState.setValue(Resource.success(list));
        }
    }

    public void fetchConversations() {
        _conversationState.setValue(Resource.loading(null));
        chatRepository.getConversations().observeForever(resource -> {
            _conversationState.setValue(resource);
        });
    }
}
