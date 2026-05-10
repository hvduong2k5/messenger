package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.AttachmentResponseDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.entity.*;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.repository.*;
import com.team12345.messenger.service.MediaService;
import com.team12345.messenger.service.MessageService;
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

    @Override
    @Transactional
    public MessageResponseDTO saveMessage(MessageRequestDTO requestDTO) {
        // Validate conversation & sender
        Conversation conversation = conversationRepository.findById(requestDTO.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        User sender = userRepository.findById(requestDTO.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

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

        return mapToResponseDTO(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getMessagesByConversation(Long conversationId, Pageable pageable) {
        // Default pagination: 20 messages, sorted by createdAt DESC
        Pageable actualPageable = pageable != null ? pageable :
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
                
        // Ensure conversation exists
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

        String type = "text";
        if (!attachmentDTOs.isEmpty()) {
             type = "media"; // basic logic, can be improved based on actual fileType
        }

        return MessageResponseDTO.builder()
                .messageId(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderAvatarUrl(message.getSender().getAvatarUrl())
                .content(message.getContent())
                .type(type)
                .status("SENT") // Should ideally be calculated based on MessageStatus table for the requesting user
                .createdAt(message.getCreatedAt())
                .attachments(attachmentDTOs)
                .build();
    }
}