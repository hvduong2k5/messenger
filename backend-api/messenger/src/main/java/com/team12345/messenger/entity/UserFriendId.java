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
public class UserFriendId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "friend_id")
    private Long friendId;
}
