package com.midterm.team12345.ui.chatlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.domain.model.Conversation;
import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.domain.model.MqttEventType;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;
import com.midterm.team12345.data.mapper.ConversationMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatListViewModel extends BaseViewModel {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private Observer<MqttMessageDTO> realTimeMessageObserver;

    private final MutableLiveData<Resource<List<Conversation>>> _conversationState = new MutableLiveData<>();
    public final LiveData<Resource<List<Conversation>>> conversationState = _conversationState;

    private final MutableLiveData<Resource<UserProfileResponseDTO>> _profileState = new MutableLiveData<>();
    public final LiveData<Resource<UserProfileResponseDTO>> profileState = _profileState;

    public LiveData<Boolean> getConnectionStatus() {
        return conversationRepository.getConnectionStatus();
    }

    public ChatListViewModel(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        observeRealTimeMessages();
    }

    private void observeRealTimeMessages() {
        realTimeMessageObserver = mqttMessage -> {
            if (mqttMessage != null) {
                String type = mqttMessage.getType();
                if ("NEW_MESSAGE".equals(type) || "text".equalsIgnoreCase(type) || "media".equalsIgnoreCase(type)) {
                    updateConversationList(mqttMessage);
                } else if ("REVOKE_MESSAGE".equals(type) || "EDIT_MESSAGE".equals(type)) {
                    updateConversationListOnEditOrRevoke(mqttMessage);
                }
            }
        };
        conversationRepository.getRealTimeMessages().observeForever(realTimeMessageObserver);
    }

    private void updateConversationList(MqttMessageDTO mqttMessage) {
        Resource<List<Conversation>> currentResource = _conversationState.getValue();
        if (currentResource != null && currentResource.status == Resource.Status.SUCCESS
                && currentResource.data != null) {
            List<Conversation> list = new ArrayList<>(currentResource.data);

            Long conversationId = mqttMessage.getConversationId();
            Long senderId = null;
            try {
                if (mqttMessage.getSender() != null) {
                    senderId = Long.parseLong(mqttMessage.getSender());
                }
            } catch (NumberFormatException ignored) {
            }

            int foundIndex = -1;
            for (int i = 0; i < list.size(); i++) {
                Conversation item = list.get(i);
                if (conversationId != null && item.getConversationId().equals(conversationId)) {
                    foundIndex = i;
                    break;
                } else if (conversationId == null && senderId != null && item.getConversationId().equals(senderId)) {
                    foundIndex = i;
                    break;
                }
            }

            if (foundIndex != -1) {
                Conversation old = list.remove(foundIndex);
                Conversation updated = new Conversation(
                        old.getConversationId(),
                        old.getConversationName(),
                        old.getGroup(),
                        old.getAvatarUrl(),
                        mqttMessage.getTimestamp() != null ? mqttMessage.getTimestamp() : System.currentTimeMillis(),
                        mqttMessage.getPayload(),
                        senderId,
                        mqttMessage.getTimestamp() != null ? mqttMessage.getTimestamp() : System.currentTimeMillis(),
                        (old.getUnreadCount() != null ? old.getUnreadCount() : 0) + 1);
                list.add(0, updated);
            } else {
                fetchConversations();
                return;
            }
            _conversationState.setValue(Resource.success(list));
        }
    }

    private void updateConversationListOnEditOrRevoke(MqttMessageDTO mqttMessage) {
        Resource<List<Conversation>> currentResource = _conversationState.getValue();
        if (currentResource != null && currentResource.status == Resource.Status.SUCCESS
                && currentResource.data != null) {
            List<Conversation> list = new ArrayList<>(currentResource.data);
            Long conversationId = mqttMessage.getConversationId();

            if (conversationId != null) {
                for (int i = 0; i < list.size(); i++) {
                    Conversation old = list.get(i);
                    if (old.getConversationId().equals(conversationId)) {
                        Conversation updated = new Conversation(
                                old.getConversationId(),
                                old.getConversationName(),
                                old.getGroup(),
                                old.getAvatarUrl(),
                                old.getUpdatedAt(),
                                mqttMessage.getPayload(),
                                old.getLastMessageSenderId(),
                                old.getLastMessageCreatedAt(),
                                old.getUnreadCount());
                        list.set(i, updated);
                        break;
                    }
                }
                _conversationState.setValue(Resource.success(list));
            }
        }
    }

    public void fetchConversations() {
        showLoading();
        conversationRepository.getConversations(0, 50).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // Thực hiện mapping từ DTO sang Domain model và sắp xếp theo cái mới nhất lên
                // trên
                List<Conversation> domainList = resource.data.getContent().stream()
                        .map(this::mapToDomain)
                        .sorted((c1, c2) -> {
                            long t1 = Math.max(
                                    c1.getLastMessageCreatedAt() != null ? c1.getLastMessageCreatedAt() : 0L,
                                    c1.getUpdatedAt() != null ? c1.getUpdatedAt() : 0L);
                            long t2 = Math.max(
                                    c2.getLastMessageCreatedAt() != null ? c2.getLastMessageCreatedAt() : 0L,
                                    c2.getUpdatedAt() != null ? c2.getUpdatedAt() : 0L);
                            return Long.compare(t2, t1);
                        })
                        .collect(Collectors.toList());

                _conversationState.setValue(Resource.success(domainList));
                hideLoading();
            } else if (resource.status == Resource.Status.ERROR) {
                _conversationState
                        .setValue(Resource.error(resource.message != null ? resource.message : "Error", null));
                setError(resource.message);
                hideLoading();
            }
        });
    }

    private Conversation mapToDomain(ConversationResponseDTO dto) {
        String timeStr = (dto.getLastMessageCreatedAt() != null) ? dto.getLastMessageCreatedAt() : dto.getUpdatedAt();
        Long parsed = ConversationMapper.parseDateStringToLong(timeStr);
        long timestamp = parsed != null ? parsed : 0L;

        return new Conversation(
                dto.getId(),
                dto.getName(),
                dto.getIsGroup(),
                dto.getAvatarUrl(),
                timestamp,
                dto.getLastMessageContent(),
                null,
                timestamp,
                dto.getUnreadCount() != null ? dto.getUnreadCount().intValue() : 0);
    }

    public void fetchMyProfile() {
        userRepository.getMyProfile().observeForever(resource -> {
            _profileState.setValue(resource);
            if (resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (realTimeMessageObserver != null) {
            conversationRepository.getRealTimeMessages().removeObserver(realTimeMessageObserver);
        }
    }
}