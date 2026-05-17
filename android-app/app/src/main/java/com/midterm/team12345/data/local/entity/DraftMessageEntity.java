package com.midterm.team12345.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "draft_messages")
public class DraftMessageEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "conversation_id")
    private Long conversationId;

    @ColumnInfo(name = "draft_content")
    private String draftContent;

    @ColumnInfo(name = "updated_at")
    private Long updatedAt;

    public DraftMessageEntity() {}

    @Ignore
    public DraftMessageEntity(@NonNull Long conversationId, String draftContent, Long updatedAt) {
        this.conversationId = conversationId;
        this.draftContent = draftContent;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(@NonNull Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getDraftContent() {
        return draftContent;
    }

    public void setDraftContent(String draftContent) {
        this.draftContent = draftContent;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
