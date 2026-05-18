package com.midterm.team12345.data.mapper;

import android.os.Build;
import com.midterm.team12345.data.local.entity.ConversationEntity;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.domain.model.Conversation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConversationMapper {

    public static ConversationEntity toEntity(ConversationResponseDTO dto) {
        if (dto == null) return null;
        ConversationEntity entity = new ConversationEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setIsGroup(dto.getIsGroup());
        entity.setAvatarUrl(dto.getAvatarUrl());
        entity.setUpdatedAt(parseDateStringToLong(dto.getUpdatedAt()));
        entity.setLastMessageContent(dto.getLastMessageContent());
        entity.setLastMessageCreatedAt(parseDateStringToLong(dto.getLastMessageCreatedAt()));
        entity.setUnreadCount(dto.getUnreadCount() != null ? dto.getUnreadCount().intValue() : 0);
        return entity;
    }

    public static Conversation toDomain(ConversationEntity entity) {
        if (entity == null) return null;
        return new Conversation(
                entity.getId(),
                entity.getName(),
                entity.getIsGroup(),
                entity.getAvatarUrl(),
                entity.getUpdatedAt(),
                entity.getLastMessageContent(),
                entity.getLastMessageSenderId(),
                entity.getLastMessageCreatedAt(),
                entity.getUnreadCount()
        );
    }

    public static ConversationEntity toEntity(Conversation domain) {
        if (domain == null) return null;
        ConversationEntity entity = new ConversationEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setIsGroup(domain.getIsGroup());
        entity.setAvatarUrl(domain.getAvatarUrl());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setLastMessageContent(domain.getLastMessageContent());
        entity.setLastMessageSenderId(domain.getLastMessageSenderId());
        entity.setLastMessageCreatedAt(domain.getLastMessageCreatedAt());
        entity.setUnreadCount(domain.getUnreadCount());
        return entity;
    }



    public static List<Conversation> toDomainList(List<ConversationEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
                .map(ConversationMapper::toDomain)
                .collect(Collectors.toList());
    }

    public static List<ConversationEntity> toEntityList(List<ConversationResponseDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .map(ConversationMapper::toEntity)
                .collect(Collectors.toList());
    }



    public static ConversationResponseDTO toDto(ConversationEntity entity) {
        if (entity == null) return null;
        ConversationResponseDTO dto = new ConversationResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setIsGroup(entity.getIsGroup());
        dto.setAvatarUrl(entity.getAvatarUrl());
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? String.valueOf(entity.getUpdatedAt()) : null);
        dto.setLastMessageContent(entity.getLastMessageContent());
        dto.setLastMessageCreatedAt(entity.getLastMessageCreatedAt() != null ? String.valueOf(entity.getLastMessageCreatedAt()) : null);
        dto.setUnreadCount(entity.getUnreadCount() != null ? entity.getUnreadCount().longValue() : 0L);
        return dto;
    }

    public static List<ConversationResponseDTO> toDtoList(List<ConversationEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
                .map(ConversationMapper::toDto)
                .collect(Collectors.toList());
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
}
