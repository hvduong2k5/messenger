package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.remote.dto.response.FriendRequestResponseDTO;
import com.midterm.team12345.data.remote.dto.response.FriendshipStatusResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.utils.Resource;
import java.util.List;

public interface FriendRepository {
    LiveData<Resource<Void>> sendFriendRequest(Long receiverId);
    LiveData<Resource<Void>> acceptFriendRequest(Long requestId);
    LiveData<Resource<Void>> rejectFriendRequest(Long requestId);
    LiveData<Resource<Void>> removeFriend(Long friendId);
    LiveData<Resource<Void>> unfriend(Long friendId);
    LiveData<Resource<List<UserResponseDTO>>> getFriends();
    LiveData<Resource<List<UserResponseDTO>>> getFriendsList();
    LiveData<Resource<List<FriendRequestResponseDTO>>> getPendingRequests();
    LiveData<Resource<Void>> cancelFriendRequest(Long receiverId);
    LiveData<Resource<FriendshipStatusResponseDTO>> checkFriendshipStatus(Long userId);
}
