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
import com.team12345.messenger.exception.UserNotFoundException;
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
        if (!participantRepository.existsById(new ParticipantId(conversationId, userId))) {
            throw new RuntimeException("User is not a participant of this conversation");
        }
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return mapToConversationResponseDTO(conversation, userId);
    }

    private ConversationResponseDTO mapToConversationResponseDTO(Conversation conversation, Long userId) {
        Optional<Message> lastMessage = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId());
        long unreadCount = messageStatusRepository.countUnreadInConversation(userId, conversation.getId(), MessageStatusEnum.read);
        
        String displayableName = conversation.getName();
        
        // Nếu là chat 1-1 và name đang trống
        if (Boolean.FALSE.equals(conversation.getIsGroup()) && (displayableName == null || displayableName.trim().isEmpty())) {
            List<Participant> participants = participantRepository.findById_ConversationId(conversation.getId());
            displayableName = participants.stream()
                    .filter(p -> !p.getUser().getId().equals(userId))
                    .findFirst()
                    .map(p -> p.getUser().getUsername()) // Hoặc getFullName() tuỳ logic hiển thị
                    .orElse("Người dùng ẩn danh"); 
        }

        return ConversationResponseDTO.builder()
                .id(conversation.getId())
                .name(displayableName)
                .isGroup(conversation.getIsGroup())
                .updatedAt(conversation.getUpdatedAt())
                .lastMessageContent(lastMessage.map(Message::getContent).orElse(null))
                .lastMessageCreatedAt(lastMessage.map(Message::getCreatedAt).orElse(null))
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getConversationMessages(Long conversationId, Long userId, Pageable pageable) {
        if (!participantRepository.existsById(new ParticipantId(conversationId, userId))) {
            throw new RuntimeException("User is not a participant of this conversation");
        }
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
                    .conversationId(message.getConversation() != null ? message.getConversation().getId() : null)
                    .senderId(message.getSender() != null ? message.getSender().getId() : null)
                    .senderUsername(message.getSender() != null ? message.getSender().getUsername() : null)
                    .senderAvatarUrl(message.getSender() != null ? message.getSender().getAvatarUrl() : null)
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
    public ConversationResponseDTO createConversation(Long currentUserId, String name, boolean isGroup, List<Long> participantIds) {
        if (!participantIds.contains(currentUserId)) {
            participantIds.add(currentUserId);
        }
        if (!isGroup && participantIds.size() == 2) {
            Optional<Conversation> existing = conversationRepository.findOneToOneConversation(participantIds.get(0), participantIds.get(1));
            if (existing.isPresent()) {
                return mapToConversationResponseDTO(existing.get(), currentUserId);
            }
        }
        if (!isGroup) {
            name = "";
        }
        Conversation conversation = Conversation.builder()
                .name(name)
                .isGroup(isGroup)
                .build();
        
        conversation = conversationRepository.save(conversation);
        
        for (Long userId : participantIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            
            Participant participant = Participant.builder()
                    .id(new ParticipantId(conversation.getId(), userId))
                    .conversation(conversation)
                    .user(user)
                    .role((isGroup && userId.equals(currentUserId)) ? com.team12345.messenger.entity.ParticipantRole.admin : com.team12345.messenger.entity.ParticipantRole.member)
                    .build();
            
            participantRepository.save(participant);
            conversation.getParticipants().add(participant);
        }
        
        return mapToConversationResponseDTO(conversation, currentUserId);
    }

    @Override
    @Transactional
    public Conversation updateConversation(Long conversationId, Long currentUserId, String name, String avatarUrl) {
        Participant currentParticipant = participantRepository.findById(new ParticipantId(conversationId, currentUserId))
                .orElseThrow(() -> new RuntimeException("User is not a participant"));
        
        if (currentParticipant.getConversation().getIsGroup() && currentParticipant.getRole() != com.team12345.messenger.entity.ParticipantRole.admin) {
             throw new RuntimeException("Only admins can update group info");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        if (name != null) {
            conversation.setName(name);
        }
        
        // Handle avatarUrl if we had GroupSettingRepository or avatarUrl on Conversation.
        // Assuming Conversation doesn't have avatarUrl field and we aren't creating a GroupSetting now.
        return conversationRepository.save(conversation);
    }

    @Override
    @Transactional
    public void addParticipant(Long conversationId, Long currentUserId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        Participant currentParticipant = participantRepository.findById(new ParticipantId(conversationId, currentUserId))
                .orElseThrow(() -> new RuntimeException("User is not a participant"));

        if (conversation.getIsGroup() && currentParticipant.getRole() != com.team12345.messenger.entity.ParticipantRole.admin) {
            throw new RuntimeException("Only admins can add participants");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (participantRepository.existsById(new ParticipantId(conversationId, userId))) {
            throw new RuntimeException("User is already a participant");
        }
        
        Participant participant = Participant.builder()
                .id(new ParticipantId(conversationId, userId))
                .conversation(conversation)
                .user(user)
                .role(com.team12345.messenger.entity.ParticipantRole.member)
                .build();
        
        participantRepository.save(participant);
    }

    @Override
    @Transactional
    public void removeParticipant(Long conversationId, Long currentUserId, Long userId) {
        ParticipantId targetId = new ParticipantId(conversationId, userId);
        if (!participantRepository.existsById(targetId)) {
            throw new RuntimeException("Participant not found");
        }

        if (!currentUserId.equals(userId)) {
            Participant currentParticipant = participantRepository.findById(new ParticipantId(conversationId, currentUserId))
                    .orElseThrow(() -> new RuntimeException("User is not a participant"));
            if (currentParticipant.getConversation().getIsGroup() && currentParticipant.getRole() != com.team12345.messenger.entity.ParticipantRole.admin) {
                throw new RuntimeException("Only admins can remove other participants");
            }
        }
        participantRepository.deleteById(targetId);
    }
}

