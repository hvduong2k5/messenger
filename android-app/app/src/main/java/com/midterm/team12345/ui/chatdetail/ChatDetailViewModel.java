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
    private Long activeConversationId;
    private Observer<MqttMessageDTO> realTimeMessageObserver;

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
                               UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        observeRealTimeMessages();
    }

    private void observeRealTimeMessages() {
        realTimeMessageObserver = mqttMessage -> {
            if (mqttMessage != null) {
                String type = mqttMessage.getType();
                if ("NEW_MESSAGE".equals(type) || "text".equalsIgnoreCase(type) || "media".equalsIgnoreCase(type)) {
                    Long msgConvId = mqttMessage.getConversationId();
                    if (activeConversationId != null && activeConversationId.equals(msgConvId)) {
                        MessageResponseDTO responseDto = mqttMessage.toMessageResponseDTO();
                        if (responseDto != null) {
                            addMessageResponseLocally(responseDto);
                        } else {
                            addMessageLocally(mqttMessage.getSender(), mqttMessage.getPayload());
                        }
                    }
                } else if ("REVOKE_MESSAGE".equals(type) || "EDIT_MESSAGE".equals(type)) {
                    MessageResponseDTO responseDto = mqttMessage.toMessageResponseDTO();
                    if (responseDto != null && activeConversationId != null && activeConversationId.equals(responseDto.getConversationId())) {
                        updateMessageLocally(responseDto);
                    }
                }
            }
        };
        conversationRepository.getRealTimeMessages().observeForever(realTimeMessageObserver);
    }

    private void addMessageResponseLocally(MessageResponseDTO newMessage) {
        Resource<List<MessageResponseDTO>> currentState = _messageState.getValue();
        List<MessageResponseDTO> currentMessages = new ArrayList<>();
        if (currentState != null && currentState.data != null) {
            currentMessages.addAll(currentState.data);
        }

        boolean exists = currentMessages.stream()
                .anyMatch(m -> (newMessage.getMessageId() != null && newMessage.getMessageId().equals(m.getMessageId())) ||
                             (newMessage.getContent().equals(m.getContent()) && 
                              newMessage.getSenderId().equals(m.getSenderId()) &&
                              newMessage.getCreatedAt() != null && m.getCreatedAt() != null && 
                              Math.abs(newMessage.getCreatedAt() - m.getCreatedAt()) < 15000));

        if (!exists) {
            currentMessages.add(0, newMessage);
            _messageState.setValue(Resource.success(currentMessages));
        }
    }

    private void updateMessageLocally(MessageResponseDTO updatedMessage) {
        Resource<List<MessageResponseDTO>> currentState = _messageState.getValue();
        if (currentState != null && currentState.data != null) {
            List<MessageResponseDTO> list = new ArrayList<>(currentState.data);
            boolean updated = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMessageId().equals(updatedMessage.getMessageId())) {
                    list.set(i, updatedMessage);
                    updated = true;
                    break;
                }
            }
            if (updated) {
                _messageState.setValue(Resource.success(list));
            }
        }
    }

    private void addMessageLocally(String sender, String payload) {
        Resource<List<MessageResponseDTO>> currentState = _messageState.getValue();
        List<MessageResponseDTO> currentMessages = new ArrayList<>();
        if (currentState != null && currentState.data != null) {
            currentMessages.addAll(currentState.data);
        }

        boolean exists = currentMessages.stream()
                .anyMatch(m -> payload.equals(m.getContent()) && 
                             m.getCreatedAt() != null && Math.abs(System.currentTimeMillis() - m.getCreatedAt()) < 15000);

        if (!exists) {
            MessageResponseDTO newMessage = new MessageResponseDTO();
            newMessage.setMessageId(System.currentTimeMillis());
            newMessage.setSenderId(Long.parseLong(sender));
            newMessage.setContent(payload);
            newMessage.setCreatedAt(System.currentTimeMillis());
            newMessage.setStatus("SENT");
            
            // CHÈN VÀO ĐẦU DANH SÁCH (VỊ TRÍ DƯỚI CÙNG TRÊN MÀN HÌNH)
            currentMessages.add(0, newMessage);
            _messageState.setValue(Resource.success(currentMessages));
        }
    }

    public void fetchMyProfile() {
        userRepository.getMyProfile().observeForever(resource -> {
            _profileState.setValue(resource);
        });
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
        _messageState.setValue(Resource.loading(null));
        messageRepository.getMessages(conversationId, 0, 50).observeForever(resource -> {
            _messageState.setValue(resource);
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

        // 1. Tạo tin nhắn tạm thời
        MessageResponseDTO pendingMsg = new MessageResponseDTO();
        pendingMsg.setSenderId(senderId);
        pendingMsg.setContent(text);
        pendingMsg.setCreatedAt(System.currentTimeMillis());
        pendingMsg.setStatus("SENDING");
        pendingMsg.setConversationId(conversationId);
        pendingMsg.setMessageId(-System.currentTimeMillis()); 

        Resource<List<MessageResponseDTO>> currentState = _messageState.getValue();
        List<MessageResponseDTO> currentMessages = new ArrayList<>();
        if (currentState != null && currentState.data != null) {
            currentMessages.addAll(currentState.data);
        }
        
        // CHÈN VÀO ĐẦU DANH SÁCH (VỊ TRÍ DƯỚI CÙNG)
        currentMessages.add(0, pendingMsg);
        _messageState.setValue(Resource.success(currentMessages));

        if (files != null && !files.isEmpty()) {
            messageRepository.sendMessageWithAttachments(conversationId, text, clientMsgId, files).observeForever(resource -> {
                handleSendResult(resource, conversationId, pendingMsg.getMessageId());
            });
            clearSelectedFiles();
        } else {
            if (text == null || text.trim().isEmpty()) return;
            MessageRequestDTO request = new MessageRequestDTO(senderId, conversationId, text, clientMsgId);
            messageRepository.sendMessage(request).observeForever(resource -> {
                handleSendResult(resource, conversationId, pendingMsg.getMessageId());
            });
        }
    }

    private void handleSendResult(Resource<MessageResponseDTO> resource, Long conversationId, Long pendingId) {
        if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
            Resource<List<MessageResponseDTO>> currentState = _messageState.getValue();
            if (currentState != null && currentState.data != null) {
                List<MessageResponseDTO> list = new ArrayList<>(currentState.data);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getMessageId().equals(pendingId)) {
                        list.set(i, resource.data); 
                        break;
                    }
                }
                _messageState.setValue(Resource.success(list));
            }
        } else if (resource.status == Resource.Status.ERROR) {
            loadMessages(conversationId);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (realTimeMessageObserver != null) {
            conversationRepository.getRealTimeMessages().removeObserver(realTimeMessageObserver);
        }
    }
}
