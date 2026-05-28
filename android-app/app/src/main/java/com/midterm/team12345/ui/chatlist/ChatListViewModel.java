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
import com.midterm.team12345.data.local.entity.ConversationEntity;
import com.midterm.team12345.data.local.database.DatabaseProvider;
import androidx.lifecycle.Transformations;
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

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private LiveData<List<ConversationEntity>> localSource;

    private final Observer<List<ConversationEntity>> localObserver = entities -> {
        if (entities != null) {
            new java.lang.Thread(() -> {
                List<Conversation> domainList = entities.stream()
                        .map(this::mapEntityToDomain)
                        .collect(Collectors.toList());
                _conversationState.postValue(Resource.success(domainList));
                hideLoading();
            }).start();
        }
    };

    public ChatListViewModel(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        
        localSource = Transformations.switchMap(searchQuery, query -> {
            showLoading();
            if (query == null || query.trim().isEmpty()) {
                return conversationRepository.getLocalConversations();
            } else {
                return conversationRepository.searchLocalConversations("%" + query.trim() + "%");
            }
        });
        localSource.observeForever(localObserver);

        observeRealTimeMessages();
    }

    public void onSearchQueryChanged(String query) {
        searchQuery.setValue(query);
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
        // Just trigger fetchConversations to sync from network, or let local Room DB handle it if it was saved by MessageRepository.
        // Actually, we can just update the local DB directly.
        new java.lang.Thread(() -> {
            try {
                Long conversationId = mqttMessage.getConversationId();
                if (conversationId != null) {
                    Long senderId = null;
                    try {
                        if (mqttMessage.getSender() != null) senderId = Long.parseLong(mqttMessage.getSender());
                    } catch (Exception ignored) {}
                    
                    Long timestamp = mqttMessage.getTimestamp() != null ? mqttMessage.getTimestamp() : System.currentTimeMillis();
                    
                    // Lấy application context qua DatabaseProvider (phải dùng Context tĩnh hoặc repository)
                    // Vì ChatListViewModel không có tham chiếu tới Database, chúng ta có thể gọi API fetch để đồng bộ lại
                    fetchConversations();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateConversationListOnEditOrRevoke(MqttMessageDTO mqttMessage) {
        fetchConversations();
    }

    public void fetchConversations() {
        showLoading();
        conversationRepository.getConversations(0, 50).observeForever(resource -> {
            if (resource.status == Resource.Status.ERROR) {
                setError(resource.message);
                hideLoading();
            }
            // Note: SUCCESS is handled by localObserver since Room DB will emit new list!
        });
    }

    private Conversation mapEntityToDomain(ConversationEntity entity) {
        return new Conversation(
                entity.getId(),
                entity.getName(),
                entity.getIsGroup(),
                entity.getAvatarUrl(),
                entity.getUpdatedAt() != null ? entity.getUpdatedAt() : 0L,
                entity.getLastMessageContent(),
                entity.getLastMessageSenderId(),
                entity.getLastMessageCreatedAt() != null ? entity.getLastMessageCreatedAt() : 0L,
                entity.getUnreadCount() != null ? entity.getUnreadCount() : 0
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

    @Override
    protected void onCleared() {
        super.onCleared();
        if (realTimeMessageObserver != null) {
            conversationRepository.getRealTimeMessages().removeObserver(realTimeMessageObserver);
        }
    }
}