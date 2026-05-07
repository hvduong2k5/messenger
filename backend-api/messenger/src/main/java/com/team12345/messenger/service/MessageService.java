package com.team12345.messenger.service;

import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    MessageResponseDTO saveMessage(MessageRequestDTO requestDTO);

    Page<MessageResponseDTO> getMessagesByConversation(Long conversationId, Pageable pageable);
}