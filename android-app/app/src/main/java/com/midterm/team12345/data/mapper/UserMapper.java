package com.midterm.team12345.data.mapper;

import android.os.Build;
import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.domain.model.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserEntity toEntity(UserResponseDTO dto) {
        if (dto == null) return null;
        UserEntity entity = new UserEntity();
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setAvatarUrl(dto.getAvatarUrl());
        entity.setPresenceStatus(dto.getStatus());
        entity.setIsOnline(dto.getIsOnline());
        entity.setLastSeenAt(parseDateStringToLong(dto.getLastSeen()));
        entity.setIsFriend(false);
        entity.setIsBlocked(false);
        return entity;
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getAvatarUrl(),
                entity.getBio(),
                entity.getPresenceStatus(),
                entity.getIsOnline(),
                entity.getLastSeenAt(),
                entity.getIsFriend(),
                entity.getFriendshipEstablishedAt(),
                entity.getIsBlocked()
        );
    }

    public static List<User> toDomainList(List<UserEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
                .map(UserMapper::toDomain)
                .collect(Collectors.toList());
    }

    public static List<UserEntity> toEntityList(List<UserResponseDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .map(UserMapper::toEntity)
                .collect(Collectors.toList());
    }

    public static Long parseDateStringToLong(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return LocalDateTime.parse(timeStr)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();
            } else {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                return sdf.parse(timeStr).getTime();
            }
        } catch (Exception e) {
            try {
                return Long.parseLong(timeStr);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
