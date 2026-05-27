package com.midterm.team12345.data.mapper;

import android.os.Build;
import com.midterm.team12345.data.local.entity.AttachmentEntity;
import com.midterm.team12345.data.local.entity.MessageEntity;
import com.midterm.team12345.data.local.entity.SyncState;
import com.midterm.team12345.data.local.entity.DeliveryStatus;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.data.remote.dto.response.AttachmentResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.domain.model.Message;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MessageMapper {

    public static MessageEntity toEntity(MessageResponseDTO dto) {
        if (dto == null) return null;
        MessageEntity entity = new MessageEntity();
        entity.setMessageId(dto.getMessageId());
        entity.setConversationId(dto.getConversationId());
        entity.setSenderId(dto.getSenderId());
        entity.setSenderUsername(dto.getSenderUsername());
        entity.setSenderAvatarUrl(dto.getSenderAvatarUrl());
        entity.setContent(dto.getContent());
        entity.setType(dto.getType());
        entity.setSyncState(SyncState.SENT);
        entity.setDeliveryStatus(parseDeliveryStatus(dto.getStatus()));
        
        Long createdAt = dto.getCreatedAt();
        entity.setServerCreatedAt(createdAt);
        entity.setLocalCreatedAt(createdAt != null ? createdAt : System.currentTimeMillis());
        
        entity.setDeletedAt(dto.getIsDeleted() != null && dto.getIsDeleted() ? System.currentTimeMillis() : null);
        entity.setEditedAt(dto.getIsEdited() != null && dto.getIsEdited() ? System.currentTimeMillis() : null);
        entity.setClientMessageId(java.util.UUID.randomUUID().toString());
        return entity;
    }

    public static MessageEntity toEntity(MessageResponseDTO dto, String clientMessageId) {
        if (dto == null) return null;
        MessageEntity entity = toEntity(dto);
        if (clientMessageId != null) {
            entity.setClientMessageId(clientMessageId);
        }
        return entity;
    }

    public static MessageEntity toEntity(MqttMessageDTO mqttDto) {
        if (mqttDto == null) return null;
        
        MessageResponseDTO responseDto = mqttDto.toMessageResponseDTO();
        if (responseDto != null) {
            return toEntity(responseDto);
        }
        
        MessageEntity entity = new MessageEntity();
        
        Long senderId = null;
        try {
            senderId = Long.parseLong(mqttDto.getSender());
        } catch (NumberFormatException ignored) {}
        
        entity.setSenderId(senderId);
        entity.setContent(mqttDto.getPayload());
        entity.setType("TEXT");
        entity.setSyncState(SyncState.SENT);
        entity.setDeliveryStatus(DeliveryStatus.SENT);
        entity.setLocalCreatedAt(mqttDto.getTimestamp() != null ? mqttDto.getTimestamp() : System.currentTimeMillis());
        entity.setServerCreatedAt(mqttDto.getTimestamp());
        entity.setClientMessageId(java.util.UUID.randomUUID().toString());
        entity.setConversationId(mqttDto.getConversationId() != null ? mqttDto.getConversationId() : senderId);
        return entity;
    }

    public static Message toDomain(MessageEntity entity) {
        if (entity == null) return null;
        return new Message(
                entity.getLocalId(),
                entity.getClientMessageId(),
                entity.getMessageId(),
                entity.getConversationId(),
                entity.getSenderId(),
                entity.getSenderUsername(),
                entity.getSenderAvatarUrl(),
                entity.getContent(),
                entity.getType(),
                entity.getSyncState(),
                entity.getDeliveryStatus(),
                entity.getLocalCreatedAt(),
                entity.getServerCreatedAt(),
                entity.getDeletedAt(),
                entity.getEditedAt(),
                entity.getRetryCount(),
                entity.getLastRetryAt()
        );
    }

    public static MessageEntity toEntity(Message domain) {
        if (domain == null) return null;
        MessageEntity entity = new MessageEntity();
        entity.setLocalId(domain.getLocalId());
        entity.setClientMessageId(domain.getClientMessageId());
        entity.setMessageId(domain.getMessageId());
        entity.setConversationId(domain.getConversationId());
        entity.setSenderId(domain.getSenderId());
        entity.setSenderUsername(domain.getSenderUsername());
        entity.setSenderAvatarUrl(domain.getSenderAvatarUrl());
        entity.setContent(domain.getContent());
        entity.setType(domain.getType());
        entity.setSyncState(domain.getSyncState());
        entity.setDeliveryStatus(domain.getDeliveryStatus());
        entity.setLocalCreatedAt(domain.getLocalCreatedAt());
        entity.setServerCreatedAt(domain.getServerCreatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setEditedAt(domain.getEditedAt());
        entity.setRetryCount(domain.getRetryCount());
        entity.setLastRetryAt(domain.getLastRetryAt());
        return entity;
    }

    public static MessageResponseDTO toResponse(MessageEntity entity) {
        if (entity == null) return null;
        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessageId(entity.getMessageId() != null ? entity.getMessageId() : (entity.getLocalId() != null ? -entity.getLocalId() : -System.currentTimeMillis()));
        response.setConversationId(entity.getConversationId());
        response.setSenderId(entity.getSenderId());
        response.setSenderUsername(entity.getSenderUsername());
        response.setSenderAvatarUrl(entity.getSenderAvatarUrl());
        response.setContent(entity.getContent());
        response.setType(entity.getType());
        if (entity.getDeliveryStatus() == DeliveryStatus.PENDING) {
            response.setStatus("SENDING");
        } else {
            response.setStatus(entity.getDeliveryStatus() != null ? entity.getDeliveryStatus().name() : "SENT");
        }
        response.setCreatedAt(entity.getServerCreatedAt() != null ? entity.getServerCreatedAt() : entity.getLocalCreatedAt());
        response.setDeleted(entity.getDeletedAt() != null);
        response.setEdited(entity.getEditedAt() != null);
        return response;
    }

    public static List<Message> toDomainList(List<MessageEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
                .map(MessageMapper::toDomain)
                .collect(Collectors.toList());
    }

    public static List<MessageEntity> toEntityList(List<MessageResponseDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .map(dto -> toEntity(dto))
                .collect(Collectors.toList());
    }

    public static List<MessageResponseDTO> toResponseList(List<MessageEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
                .map(MessageMapper::toResponse)
                .collect(Collectors.toList());
    }

    private static DeliveryStatus parseDeliveryStatus(String statusStr) {
        if (statusStr == null) return DeliveryStatus.PENDING;
        try {
            return DeliveryStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DeliveryStatus.PENDING;
        }
    }

    public static Long parseDateStringToLong(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                return java.time.Instant.parse(timeStr).toEpochMilli();
            } catch (Exception ignored) {}
            try {
                return java.time.OffsetDateTime.parse(timeStr).toInstant().toEpochMilli();
            } catch (Exception ignored) {}
            try {
                return java.time.ZonedDateTime.parse(timeStr).toInstant().toEpochMilli();
            } catch (Exception ignored) {}
            try {
                return java.time.LocalDateTime.parse(timeStr)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();
            } catch (Exception ignored) {}
        }
        
        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ",
            "yyyy-MM-dd'T'HH:mm:ssZZZZZ",
            "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String format : formats) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, java.util.Locale.getDefault());
                if (format.endsWith("'Z'")) {
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                }
                return sdf.parse(timeStr).getTime();
            } catch (Exception ignored) {}
        }
        
        try {
            return Long.parseLong(timeStr);
        } catch (NumberFormatException ignored) {}
        
        return null;
    }

    public static AttachmentEntity toAttachmentEntity(AttachmentResponseDTO dto, String clientMessageId) {
        if (dto == null) return null;
        AttachmentEntity entity = new AttachmentEntity();
        entity.setId(dto.getId());
        entity.setClientMessageId(clientMessageId);
        entity.setUrl(dto.getUrl());
        entity.setMimeType(dto.getType());
        entity.setFileSize(dto.getFileSize() != null ? dto.getFileSize().longValue() : 0L);
        entity.setPublicId(dto.getPublicId());
        entity.setUiState(com.midterm.team12345.data.local.entity.AttachmentUiState.SUCCESS);
        return entity;
    }

    public static AttachmentResponseDTO toAttachmentResponse(AttachmentEntity entity) {
        if (entity == null) return null;
        AttachmentResponseDTO dto = new AttachmentResponseDTO();
        dto.setId(entity.getId());
        dto.setUrl(entity.getUrl());
        dto.setType(entity.getMimeType());
        dto.setFileSize(entity.getFileSize() != null ? entity.getFileSize().intValue() : 0);
        dto.setPublicId(entity.getPublicId());
        return dto;
    }
}
