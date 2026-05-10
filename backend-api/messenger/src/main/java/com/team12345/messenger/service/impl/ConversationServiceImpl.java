package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.response.AttachmentResponseDTO;
import com.team12345.messenger.dto.response.ConversationResponseDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.entity.Message;
import com.team12345.messenger.entity.MessageStatusEnum;
import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.entity.ParticipantId;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.repository.ConversationRepository;
import com.team12345.messenger.repository.MessageRepository;
import com.team12345.messenger.repository.MessageStatusRepository;
import com.team12345.messenger.repository.ParticipantRepository;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ConversationService} providing business logic for chat management.
 */
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponseDTO> getUserConversations(Long userId, Pageable pageable) {
        Page<Conversation> conversations = conversationRepository.findConversationsByUserId(userId, pageable);
        return conversations.map(conversation -> mapToConversationResponseDTO(conversation, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponseDTO getConversationDetails(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return mapToConversationResponseDTO(conversation, userId);
    }

    private ConversationResponseDTO mapToConversationResponseDTO(Conversation conversation, Long userId) {
        Optional<Message> lastMessage = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId());
        long unreadCount = messageStatusRepository.countUnreadInConversation(userId, conversation.getId(), MessageStatusEnum.read);
        
        return ConversationResponseDTO.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .isGroup(conversation.getIsGroup())
                .updatedAt(conversation.getUpdatedAt())
                .lastMessageContent(lastMessage.map(Message::getContent).orElse(null))
                .lastMessageCreatedAt(lastMessage.map(Message::getCreatedAt).orElse(null))
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getConversationMessages(Long conversationId, Pageable pageable) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new RuntimeException("Conversation not found");
        }
        
        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        
        return messages.map(message -> {
            List<AttachmentResponseDTO> attachmentDTOs = new ArrayList<>();
            if (message.getAttachments() != null) {
                attachmentDTOs = message.getAttachments().stream()
                        .map(att -> AttachmentResponseDTO.builder()
                                .url(att.getFileUrl())
                                .type(att.getFileType())
                                .fileSize(att.getFileSize())
                                .build())
                        .collect(Collectors.toList());
            }

            String type = "text";
            if (!attachmentDTOs.isEmpty()) {
                type = "media";
            }

            return MessageResponseDTO.builder()
                    .messageId(message.getId())
                    .conversationId(message.getConversation().getId())
                    .senderId(message.getSender().getId())
                    .senderUsername(message.getSender().getUsername())
                    .senderAvatarUrl(message.getSender().getAvatarUrl())
                    .content(message.getContent())
                    .type(type)
                    .status("SENT") // placeholder
                    .createdAt(message.getCreatedAt())
                    .attachments(attachmentDTOs)
                    .build();
        });
    }

    @Override
    @Transactional
    public Conversation createConversation(String name, boolean isGroup, List<Long> participantIds) {
        Conversation conversation = Conversation.builder()
                .name(name)
                .isGroup(isGroup)
                .updatedAt(LocalDateTime.now())
                .build();
        
        conversation = conversationRepository.save(conversation);
        
        for (Long userId : participantIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            
            Participant participant = Participant.builder()
                    .id(new ParticipantId(conversation.getId(), userId))
                    .conversation(conversation)
                    .user(user)
                    .build();
            
            participantRepository.save(participant);
        }
        
        return conversation;
    }

    @Override
    @Transactional
    public void addParticipant(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (participantRepository.existsById(new ParticipantId(conversationId, userId))) {
            throw new RuntimeException("User is already a participant");
        }
        
        Participant participant = Participant.builder()
                .id(new ParticipantId(conversationId, userId))
                .conversation(conversation)
                .user(user)
                .build();
        
        participantRepository.save(participant);
    }

    @Override
    @Transactional
    public void removeParticipant(Long conversationId, Long userId) {
        ParticipantId participantId = new ParticipantId(conversationId, userId);
        if (!participantRepository.existsById(participantId)) {
            throw new RuntimeException("Participant not found");
        }
        participantRepository.deleteById(participantId);
    }
}