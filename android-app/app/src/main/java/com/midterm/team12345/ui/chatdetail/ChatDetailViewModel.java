package com.midterm.team12345.ui.chatdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.domain.repository.MessageRepository;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.utils.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatDetailViewModel extends ViewModel {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final com.midterm.team12345.data.local.dao.ConversationDao conversationDao;
    private final com.midterm.team12345.data.local.dao.MessageDao messageDao;
    private Long activeConversationId;
    private Long currentUserId;
    private int currentPage = 0;
    private boolean isLastPage = false;
    private final MutableLiveData<Boolean> _isLoadingMore = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoadingMore = _isLoadingMore;

    private LiveData<Resource<List<MessageResponseDTO>>> currentMessagesSource;
    private Observer<Resource<List<MessageResponseDTO>>> messagesObserver;
    private Observer<com.midterm.team12345.data.remote.dto.MqttMessageDTO> realTimeMessageObserver;
    private Observer<Boolean> connectionStatusObserver;

    private final MutableLiveData<Resource<List<MessageResponseDTO>>> _messageState = new MutableLiveData<>();
    public final LiveData<Resource<List<MessageResponseDTO>>> messageState = _messageState;

    private final MutableLiveData<Resource<UserProfileResponseDTO>> _profileState = new MutableLiveData<>();
    public final LiveData<Resource<UserProfileResponseDTO>> profileState = _profileState;

    private final MutableLiveData<Resource<ConversationResponseDTO>> _createConversationState = new MutableLiveData<>();
    public final LiveData<Resource<ConversationResponseDTO>> createConversationState = _createConversationState;

    private final MutableLiveData<List<File>> _selectedFiles = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<File>> selectedFiles = _selectedFiles;

    public ChatDetailViewModel(MessageRepository messageRepository, 
                               ConversationRepository conversationRepository,
                               UserRepository userRepository,
                               com.midterm.team12345.data.local.dao.ConversationDao conversationDao,
                               com.midterm.team12345.data.local.dao.MessageDao messageDao) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.conversationDao = conversationDao;
        this.messageDao = messageDao;
    }

    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
        if (activeConversationId != null && activeConversationId != -1L) {
            markConversationAsRead(activeConversationId, userId);
        }
    }

    public void fetchMyProfile() {
        userRepository.getMyProfile().observeForever(resource -> {
            _profileState.setValue(resource);
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                this.currentUserId = resource.data.getId();
                markConversationAsRead(activeConversationId, currentUserId);
            }
        });
    }

    public Long getActiveConversationId() {
        return activeConversationId;
    }

    private void markConversationAsRead(Long conversationId, Long userId) {
        if (conversationId == null || userId == null || userId == -1L) return;
        new java.lang.Thread(() -> {
            try {
                conversationDao.resetUnreadCount(conversationId);
                messageDao.markAllReceivedMessagesAsRead(conversationId, userId);
                
                messageRepository.markConversationAsRead(conversationId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startConversationWithPartner(Long partnerId, String partnerName) {
        _createConversationState.setValue(Resource.loading(null));
        List<Long> participants = new ArrayList<>();
        participants.add(partnerId);
        
        ConversationRequestDTO request = new ConversationRequestDTO(
                partnerName != null ? partnerName : "Chat",
                false,
                participants
        );
        
        conversationRepository.createConversation(request).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                activeConversationId = resource.data.getId();
                _createConversationState.setValue(resource);
                loadMessages(activeConversationId);
            } else if (resource.status == Resource.Status.ERROR) {
                _createConversationState.setValue(Resource.error(resource.message, null));
            }
        });
    }

    public void loadMessages(Long conversationId) {
        this.activeConversationId = conversationId;
        this.currentPage = 0;
        this.isLastPage = false;
        _isLoadingMore.setValue(false);
        
        if (currentMessagesSource != null && messagesObserver != null) {
            currentMessagesSource.removeObserver(messagesObserver);
        }

        if (realTimeMessageObserver != null) {
            conversationRepository.getRealTimeMessages().removeObserver(realTimeMessageObserver);
        }
        if (connectionStatusObserver != null) {
            conversationRepository.getConnectionStatus().removeObserver(connectionStatusObserver);
        }

        markConversationAsRead(conversationId, currentUserId);

        realTimeMessageObserver = mqttMessage -> {
            if (mqttMessage != null && conversationId.equals(mqttMessage.getConversationId())) {
                Long senderId = mqttMessage.getSenderId();
                if (senderId != null && !senderId.equals(currentUserId)) {
                    String type = mqttMessage.getType();
                    if ("NEW_MESSAGE".equals(type) || "text".equalsIgnoreCase(type) || "media".equalsIgnoreCase(type)) {
                        markConversationAsRead(conversationId, currentUserId);
                    }
                }
            }
        };
        conversationRepository.getRealTimeMessages().observeForever(realTimeMessageObserver);

        connectionStatusObserver = connected -> {
            if (connected != null && connected) {
                com.midterm.team12345.data.remote.mqtt.MqttManager.getInstance().subscribe("conversation/" + conversationId + "/read");
                com.midterm.team12345.data.remote.mqtt.MqttManager.getInstance().subscribe("conversation/" + conversationId + "/message-status");
            }
        };
        conversationRepository.getConnectionStatus().observeForever(connectionStatusObserver);

        _messageState.setValue(Resource.loading(null));
        
        currentMessagesSource = messageRepository.getMessages(conversationId, 0, 20);
        messagesObserver = resource -> {
            _messageState.setValue(resource);
        };
        currentMessagesSource.observeForever(messagesObserver);
    }

    public void loadMoreMessages() {
        if (activeConversationId == null || isLastPage || Boolean.TRUE.equals(_isLoadingMore.getValue())) {
            return;
        }

        _isLoadingMore.setValue(true);
        int nextPage = currentPage + 1;

        LiveData<Resource<Boolean>> liveData = messageRepository.fetchNextPageOfMessages(activeConversationId, nextPage, 20);
        liveData.observeForever(new Observer<Resource<Boolean>>() {
            @Override
            public void onChanged(Resource<Boolean> resource) {
                if (resource == null) return;
                if (resource.status != Resource.Status.LOADING) {
                    liveData.removeObserver(this);
                    _isLoadingMore.setValue(false);
                    if (resource.status == Resource.Status.SUCCESS) {
                        currentPage = nextPage;
                        if (resource.data != null) {
                            isLastPage = resource.data;
                        }
                    }
                }
            }
        });
    }

    public void addSelectedFile(File file) {
        List<File> current = _selectedFiles.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(file);
        _selectedFiles.setValue(new ArrayList<>(current));
    }

    public void removeSelectedFile(File file) {
        List<File> current = _selectedFiles.getValue();
        if (current != null) {
            current.remove(file);
            _selectedFiles.setValue(new ArrayList<>(current));
        }
    }

    public void clearSelectedFiles() {
        _selectedFiles.setValue(new ArrayList<>());
    }

    public void sendMessage(String text, Long conversationId, Long senderId) {
        List<File> files = _selectedFiles.getValue();
        String clientMsgId = UUID.randomUUID().toString();

        if (files != null && !files.isEmpty()) {
            messageRepository.sendMessageWithAttachments(conversationId, text, clientMsgId, files);
            clearSelectedFiles();
        } else {
            if (text == null || text.trim().isEmpty()) return;
            MessageRequestDTO request = new MessageRequestDTO(senderId, conversationId, text, clientMsgId);
            messageRepository.sendMessage(request);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentMessagesSource != null && messagesObserver != null) {
            currentMessagesSource.removeObserver(messagesObserver);
        }
        if (realTimeMessageObserver != null) {
            conversationRepository.getRealTimeMessages().removeObserver(realTimeMessageObserver);
        }
        if (connectionStatusObserver != null) {
            conversationRepository.getConnectionStatus().removeObserver(connectionStatusObserver);
        }
        if (activeConversationId != null) {
            com.midterm.team12345.data.remote.mqtt.MqttManager.getInstance().unsubscribe("conversation/" + activeConversationId + "/read");
            com.midterm.team12345.data.remote.mqtt.MqttManager.getInstance().unsubscribe("conversation/" + activeConversationId + "/message-status");
        }
    }
}
