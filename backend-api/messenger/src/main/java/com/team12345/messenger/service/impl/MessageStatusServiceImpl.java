package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.response.MessageStatusResponseDTO;
import com.team12345.messenger.entity.MessageStatus;
import com.team12345.messenger.entity.MessageStatusEnum;
import com.team12345.messenger.entity.MessageStatusId;
import com.team12345.messenger.entity.ParticipantId;
import com.team12345.messenger.gateway.MqttGateway;
import com.team12345.messenger.repository.MessageStatusRepository;
import com.team12345.messenger.repository.ParticipantRepository;
import com.team12345.messenger.service.MessageStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link MessageStatusService}.
 */
@Service
@RequiredArgsConstructor
public class MessageStatusServiceImpl implements MessageStatusService {

    private final MessageStatusRepository messageStatusRepository;
    private final ParticipantRepository participantRepository;
    private final MqttGateway mqttGateway;

    @Override
    @Transactional
    public void markConversationAsRead(Long conversationId, Long userId) {
        if (!participantRepository.existsById(new ParticipantId(conversationId, userId))) {
            throw new AccessDeniedException("User is not a participant of this conversation");
        }
        
        int updatedCount = messageStatusRepository.markAsReadByConversationId(userId, conversationId, MessageStatusEnum.read);
        
        if (updatedCount > 0) {
            String topic = "conversation/" + conversationId + "/read";
            String payload = String.format("{\"conversationId\": %d, \"userId\": %d, \"status\": \"READ\"}", conversationId, userId);
            mqttGateway.sendToMqtt(payload, topic);
        }
    }

    @Override
    @Transactional
    public void updateMessageStatus(Long messageId, Long userId, String status) {
        MessageStatusEnum newStatus;
        try {
            newStatus = MessageStatusEnum.valueOf(status.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        
        MessageStatus messageStatus = messageStatusRepository.findById(new MessageStatusId(messageId, userId))
                .orElseThrow(() -> new AccessDeniedException("Status not found or user is not a recipient of this message"));
        
        messageStatus.setStatus(newStatus);
        messageStatusRepository.save(messageStatus);

        Long conversationId = messageStatus.getMessage().getConversation().getId();
        String topic = "conversation/" + conversationId + "/message-status";
        String payload = String.format("{\"messageId\": %d, \"userId\": %d, \"status\": \"%s\"}", messageId, userId, newStatus.name().toUpperCase());
        mqttGateway.sendToMqtt(payload, topic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageStatusResponseDTO> getMessageStatuses(Long messageId) {
        return messageStatusRepository.findById_MessageId(messageId).stream()
                .map(ms -> MessageStatusResponseDTO.builder()
                        .userId(ms.getReceiver().getId())
                        .username(ms.getReceiver().getUsername())
                        .avatarUrl(ms.getReceiver().getAvatarUrl())
                        .status(ms.getStatus().name().toUpperCase())
                        .updatedAt(ms.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
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

