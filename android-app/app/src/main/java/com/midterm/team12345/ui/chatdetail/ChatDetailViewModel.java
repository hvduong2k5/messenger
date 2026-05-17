package com.midterm.team12345.ui.chatdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
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

    private final MutableLiveData<Resource<List<MessageResponseDTO>>> _messageState = new MutableLiveData<>();
    public final LiveData<Resource<List<MessageResponseDTO>>> messageState = _messageState;

    private final MutableLiveData<Resource<UserProfileResponseDTO>> _profileState = new MutableLiveData<>();
    public final LiveData<Resource<UserProfileResponseDTO>> profileState = _profileState;

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
        conversationRepository.getRealTimeMessages().observeForever(mqttMessage -> {
            if (mqttMessage != null && "NEW_MESSAGE".equals(mqttMessage.getType())) {
                addMessageLocally(mqttMessage.getSender(), mqttMessage.getPayload());
            }
        });
    }

    private void addMessageLocally(String sender, String payload) {
        Resource<List<MessageResponseDTO>> currentState = _messageState.getValue();
        List<MessageResponseDTO> currentMessages = new ArrayList<>();
        if (currentState != null && currentState.data != null) {
            currentMessages.addAll(currentState.data);
        }

        boolean exists = currentMessages.stream()
                .anyMatch(m -> payload.equals(m.getContent()) && 
                             m.getCreatedAt() != null && Math.abs(System.currentTimeMillis() - m.getCreatedAt()) < 2000);

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

    public void loadMessages(Long conversationId) {
        _messageState.setValue(Resource.loading(null));
        messageRepository.getMessages(conversationId, 0, 50).observeForever(resource -> {
            _messageState.setValue(resource);
        });
    }

    public void addSelectedFile(File file) {
        List<File> current = _selectedFiles.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(file);
        _selectedFiles.setValue(current);
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
}
