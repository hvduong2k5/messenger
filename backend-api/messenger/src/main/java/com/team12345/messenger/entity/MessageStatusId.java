package com.team12345.messenger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatusId implements Serializable {

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "receiver_id")
    private Long receiverId;
}