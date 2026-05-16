package com.midterm.team12345.data.remote.dto;

public class MqttMessageDTO {
    private String type; // Maps to MqttEventType
    private String payload;
    private String sender;
    private Long timestamp;

    public MqttMessageDTO() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
