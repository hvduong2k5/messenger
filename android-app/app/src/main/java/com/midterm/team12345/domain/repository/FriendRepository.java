package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.remote.dto.response.FriendRequestResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.utils.Resource;
import java.util.List;

public interface FriendRepository {
    LiveData<Resource<FriendRequestResponseDTO>> sendFriendRequest(Long receiverId);
    LiveData<Resource<Void>> acceptFriendRequest(Long senderId);
    LiveData<Resource<Void>> declineFriendRequest(Long senderId);
    LiveData<Resource<Void>> unfriend(Long friendId);
    LiveData<Resource<List<UserResponseDTO>>> getFriendsList();
    LiveData<Resource<List<FriendRequestResponseDTO>>> getPendingFriendRequests();
}
