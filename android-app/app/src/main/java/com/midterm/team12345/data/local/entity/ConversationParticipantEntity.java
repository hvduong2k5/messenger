package com.midterm.team12345.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(
    tableName = "conversation_participants",
    primaryKeys = {"conversation_id", "user_id"}
)
public class ConversationParticipantEntity {

    @NonNull
    @ColumnInfo(name = "conversation_id")
    private Long conversationId;

    @NonNull
    @ColumnInfo(name = "user_id")
    private Long userId;

    @ColumnInfo(name = "role")
    private String role; // e.g. MEMBER, ADMIN, OWNER

    @ColumnInfo(name = "nickname")
    private String nickname;

    @ColumnInfo(name = "joined_at")
    private Long joinedAt;

    public ConversationParticipantEntity() {}

    @Ignore
    public ConversationParticipantEntity(@NonNull Long conversationId, @NonNull Long userId, String role, String nickname, Long joinedAt) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
        this.nickname = nickname;
        this.joinedAt = joinedAt;
    }

    @NonNull
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(@NonNull Long conversationId) {
        this.conversationId = conversationId;
    }

    @NonNull
    public Long getUserId() {
        return userId;
    }

    public void setUserId(@NonNull Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Long joinedAt) {
        this.joinedAt = joinedAt;
    }
}
