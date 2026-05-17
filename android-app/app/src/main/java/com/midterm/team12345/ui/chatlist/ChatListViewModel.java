package com.midterm.team12345.ui.chatlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponse;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.domain.model.MqttEventType;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatListViewModel extends BaseViewModel {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<Resource<List<ConversationResponse>>> _conversationState = new MutableLiveData<>();
    public final LiveData<Resource<List<ConversationResponse>>> conversationState = _conversationState;

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
        conversationRepository.getRealTimeMessages().observeForever(mqttMessage -> {
            if (mqttMessage != null && MqttEventType.NEW_MESSAGE.name().equals(mqttMessage.getType())) {
                updateConversationList(mqttMessage);
            }
        });
    }

    private void updateConversationList(MqttMessageDTO mqttMessage) {
        Resource<List<ConversationResponse>> currentResource = _conversationState.getValue();
        if (currentResource != null && currentResource.status == Resource.Status.SUCCESS && currentResource.data != null) {
            List<ConversationResponse> list = new ArrayList<>(currentResource.data);
            Long senderId;
            try {
                senderId = Long.parseLong(mqttMessage.getSender());
            } catch (NumberFormatException e) {
                return;
            }

            int foundIndex = -1;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getConversationId().equals(senderId)) {
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
            } else {
                fetchConversations();
                return;
            }
            _conversationState.setValue(Resource.success(list));
        }
    }

    public void fetchConversations() {
        showLoading();
        conversationRepository.getConversations(0, 50).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // Thực hiện mapping từ DTO sang Domain model
                List<ConversationResponse> domainList = resource.data.getContent().stream()
                        .map(this::mapToDomain)
                        .collect(Collectors.toList());

                _conversationState.setValue(Resource.success(domainList));
                hideLoading();
            } else if (resource.status == Resource.Status.ERROR) {
                _conversationState.setValue(Resource.error(resource.message != null ? resource.message : "Error", null));
                setError(resource.message);
                hideLoading();
            }
        });
    }

    private ConversationResponse mapToDomain(ConversationResponseDTO dto) {
        long timestamp = 0;
        String timeStr = (dto.getLastMessageCreatedAt() != null) ? dto.getLastMessageCreatedAt() : dto.getUpdatedAt();

        if (timeStr != null && !timeStr.isEmpty()) {
            try {
                // Parse chuỗi ISO sang milliseconds
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    timestamp = LocalDateTime.parse(timeStr)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli();
                }
            } catch (Exception e) {
                try {
                    timestamp = Long.parseLong(timeStr);
                } catch (NumberFormatException ignored) {}
            }
        }

        return new ConversationResponse(
                dto.getId(),
                dto.getName(),
                dto.getLastMessageContent(),
                dto.getAvatarUrl(),
                timestamp,
                dto.getUnreadCount() != null ? dto.getUnreadCount().intValue() : 0,
                false,
                false,
                dto.getIsGroup()
        );
    }

    public void fetchMyProfile() {
        userRepository.getMyProfile().observeForever(resource -> {
            _profileState.setValue(resource);
            if (resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }
}