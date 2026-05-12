package com.team12345.messenger.service;

import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.dto.response.SaveMessageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    SaveMessageResult saveMessage(MessageRequestDTO requestDTO);

    Page<MessageResponseDTO> getMessagesByConversation(Long conversationId, Long userId, Pageable pageable);

    void revokeMessage(Long messageId, Long userId);

    MessageResponseDTO editMessage(Long messageId, Long userId, String newContent);

    Page<MessageResponseDTO> searchMessages(String keyword, Long conversationId, Long userId, Pageable pageable);
}