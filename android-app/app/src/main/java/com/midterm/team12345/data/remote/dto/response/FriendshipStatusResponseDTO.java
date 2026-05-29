package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class FriendshipStatusResponseDTO {
    @SerializedName("friendshipStatus")
    private FriendshipStatus friendshipStatus;

    public FriendshipStatus getFriendshipStatus() {
        return friendshipStatus;
    }

    public void setFriendshipStatus(FriendshipStatus friendshipStatus) {
        this.friendshipStatus = friendshipStatus;
    }
}
