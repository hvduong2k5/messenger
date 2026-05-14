package com.midterm.team12345.data.dto;

import java.util.List;

public class MessageRequestDTO {
    private Long senderId;
    private Long conversationId;
    private String content;
    private String clientMessageId;
    private List<Object> files;

    public MessageRequestDTO(Long senderId, Long conversationId, String content, String clientMessageId) {
        this.senderId = senderId;
        this.conversationId = conversationId;
        this.content = content;
        this.clientMessageId = clientMessageId;
    }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getClientMessageId() { return clientMessageId; }
    public void setClientMessageId(String clientMessageId) { this.clientMessageId = clientMessageId; }

    public List<Object> getFiles() { return files; }
    public void setFiles(List<Object> files) { this.files = files; }
}
