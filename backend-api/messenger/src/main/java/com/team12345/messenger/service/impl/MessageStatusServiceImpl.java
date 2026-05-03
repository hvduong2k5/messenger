package com.team12345.messenger.service.impl;

import com.team12345.messenger.repository.MessageStatusRepository;
import com.team12345.messenger.service.MessageStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link MessageStatusService}.
 */
@Service
@RequiredArgsConstructor
public class MessageStatusServiceImpl implements MessageStatusService {

    private final MessageStatusRepository messageStatusRepository;

    @Override
    @Transactional
    public void markAsRead(Long userId, Long conversationId) {
        messageStatusRepository.markAsRead(userId, conversationId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadMessages(Long userId, Long conversationId) {
        return messageStatusRepository.countUnreadMessages(userId, conversationId);
    }
}
