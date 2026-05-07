package com.team12345.messenger.service.impl;

import com.team12345.messenger.entity.MessageStatusEnum;
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
        messageStatusRepository.markAsReadByConversationId(userId, conversationId, MessageStatusEnum.read);
    }

    @Override
    @Transactional
    public void markMessageAsRead(Long userId, Long messageId) {
        messageStatusRepository.markAsReadByMessageId(userId, messageId, MessageStatusEnum.read);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadMessages(Long userId, Long conversationId) {
        return messageStatusRepository.countUnreadInConversation(userId, conversationId, MessageStatusEnum.read);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalUnreadMessages(Long userId) {
        return messageStatusRepository.countById_ReceiverIdAndStatusNot(userId, MessageStatusEnum.read);
    }
}
