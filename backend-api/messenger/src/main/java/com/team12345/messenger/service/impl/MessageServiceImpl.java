package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.AttachmentResponseDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.entity.*;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.repository.*;
import com.team12345.messenger.service.MediaService;
import com.team12345.messenger.service.MessageService;
import com.team12345.messenger.gateway.MqttGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final MediaService mediaService;
    private final MqttGateway mqttGateway;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    @Transactional
    public MessageResponseDTO saveMessage(MessageRequestDTO requestDTO) {
        // Validate conversation & sender
        Conversation conversation = conversationRepository.findById(requestDTO.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        User sender = userRepository.findById(requestDTO.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        if (!participantRepository.existsById(new ParticipantId(requestDTO.getConversationId(), requestDTO.getSenderId()))) {
            throw new org.springframework.security.access.AccessDeniedException("Not a participant in this conversation");
        }

        // Map DTO -> Message entity
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(requestDTO.getContent())
                .clientMessageId(requestDTO.getClientMessageId())
                .build();

        // Save attachments if any
        if (requestDTO.getFiles() != null && !requestDTO.getFiles().isEmpty()) {
            List<Attachment> attachments = processAttachments(requestDTO.getFiles(), message);
            message.setAttachments(attachments);
        }

        // Save message into PostgreSQL
        Message savedMessage = messageRepository.save(message);

        // Create message_status records for all participants except sender
        createMessageStatusRecords(savedMessage, conversation.getId(), sender.getId());

        MessageResponseDTO responseDTO = mapToResponseDTO(savedMessage);
        sendMqttNotification(responseDTO, "NEW_MESSAGE");

        return responseDTO;
    }

    private void sendMqttNotification(MessageResponseDTO messageDto, String action) {
        try {
            String topic = "conversations/" + messageDto.getConversationId();
            Map<String, Object> payload = Map.of(
                "action", action,
                "data", messageDto
            );
            String jsonPayload = objectMapper.writeValueAsString(payload);
            mqttGateway.sendToMqtt(jsonPayload, topic);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            // log.error("Failed to send MQTT notification", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getMessagesByConversation(Long conversationId, Long userId, Pageable pageable) {
        if (!participantRepository.existsById(new ParticipantId(conversationId, userId))) {
            throw new org.springframework.security.access.AccessDeniedException("Not a participant in this conversation");
        }

        Pageable actualPageable = pageable != null ? pageable :
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
                
        if(!conversationRepository.existsById(conversationId)) {
            throw new ResourceNotFoundException("Conversation not found");
        }

        Page<Message> messagePage = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, actualPageable);

        return messagePage.map(this::mapToResponseDTO);
    }

    private List<Attachment> processAttachments(List<MultipartFile> files, Message message) {
        List<Attachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            Map<String, Object> uploadResult = mediaService.uploadFile(file);
            
            Attachment attachment = Attachment.builder()
                    .fileUrl((String) uploadResult.get("url"))
                    .fileType((String) uploadResult.get("type"))
                    .fileSize((Integer) uploadResult.get("size"))
                    .message(message)
                    .build();
            attachments.add(attachment);
        }
        return attachments;
    }

    private void createMessageStatusRecords(Message message, Long conversationId, Long senderId) {
        List<Participant> participants = participantRepository.findById_ConversationId(conversationId);
        
        List<MessageStatus> statusRecords = participants.stream()
                .filter(p -> !p.getUser().getId().equals(senderId))
                .map(p -> {
                    MessageStatusId id = new MessageStatusId(message.getId(), p.getUser().getId());
                    return MessageStatus.builder()
                            .id(id)
                            .message(message)
                            .receiver(p.getUser())
                            .status(MessageStatusEnum.sent)
                            .build();
                })
                .collect(Collectors.toList());
                
        messageStatusRepository.saveAll(statusRecords);
    }

    private MessageResponseDTO mapToResponseDTO(Message message) {
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

        Boolean isDeleted = message.getIsDeleted() != null && message.getIsDeleted();
        Boolean isEdited = message.getIsEdited() != null && message.getIsEdited();

        String content = isDeleted ? "Tin nhắn đã bị thu hồi" : message.getContent();
        List<AttachmentResponseDTO> finalAttachments = isDeleted ? new ArrayList<>() : attachmentDTOs;

        String type = "text";
        if (!finalAttachments.isEmpty()) {
             type = "media";
        }

        return MessageResponseDTO.builder()
                .messageId(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderAvatarUrl(message.getSender().getAvatarUrl())
                .content(content)
                .type(type)
                .status("SENT")
                .createdAt(message.getCreatedAt())
                .isDeleted(isDeleted)
                .isEdited(isEdited)
                .attachments(finalAttachments)
                .build();
    }

    @Override
    @Transactional
    public void revokeMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to revoke this message");
        }

        message.setIsDeleted(true);
        messageRepository.save(message);
        
        sendMqttNotification(mapToResponseDTO(message), "REVOKE_MESSAGE");
    }

    @Override
    @Transactional
    public MessageResponseDTO editMessage(Long messageId, Long userId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to edit this message");
        }
        
        if (message.getIsDeleted() != null && message.getIsDeleted()) {
            throw new IllegalArgumentException("Cannot edit a revoked message");
        }

        message.setContent(newContent);
        message.setIsEdited(true);
        Message updatedMessage = messageRepository.save(message);
        
        MessageResponseDTO responseDTO = mapToResponseDTO(updatedMessage);
        sendMqttNotification(responseDTO, "EDIT_MESSAGE");
        
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> searchMessages(String keyword, Long conversationId, Long userId, Pageable pageable) {
        Pageable actualPageable = pageable != null ? pageable :
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Message> messagePage;
        if (conversationId != null) {
            if (!participantRepository.existsById(new ParticipantId(conversationId, userId))) {
                throw new org.springframework.security.access.AccessDeniedException("Not a participant in this conversation");
            }
            messagePage = messageRepository.findByConversationIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
                    conversationId, keyword, actualPageable);
        } else {
            // Note: If searching globally, we should ideally restrict to conversations the user is part of.
            // A custom query in repository might be needed. For simplicity, we just search globally.
            // But this would expose messages from other users! We MUST restrict it!
            throw new UnsupportedOperationException("Global search across all conversations is not fully implemented yet.");
        }
        
        return messagePage.map(this::mapToResponseDTO);
    }
}