package com.team12345.messenger.service;

import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.DetailMessageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    DetailMessageResponseDTO saveMessage(MessageRequestDTO requestDTO);

    Page<DetailMessageResponseDTO> getMessagesByConversation(Long conversationId, Pageable pageable);
}