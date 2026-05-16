package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.remote.dto.response.ConversationResponse;
import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.request.ConversationUpdateDTO;
import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponse;
import com.midterm.team12345.data.remote.dto.response.MessageStatusResponseDTO;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.data.remote.dto.response.FriendRequestResponseDTO;
import com.midterm.team12345.utils.Resource;
import java.util.List;

public interface ChatRepository {
    // Conversations
    LiveData<Resource<List<ConversationResponse>>> getConversations(int page, int size);
    LiveData<Resource<ConversationResponse>> getConversationDetails(Long id);
    LiveData<Resource<ConversationResponse>> createConversation(ConversationRequestDTO request);
    LiveData<Resource<ConversationResponse>> updateConversation(Long id, ConversationUpdateDTO request);
    LiveData<Resource<Void>> addParticipant(Long conversationId, Long userId);
    LiveData<Resource<Void>> removeParticipant(Long conversationId, Long userId);
    LiveData<Resource<Void>> leaveConversation(Long id);
    LiveData<Resource<Void>> muteConversation(Long id);
    LiveData<Resource<Void>> unmuteConversation(Long id);

    // Messages
    LiveData<Resource<List<MessageResponse>>> getMessages(Long conversationId, int page, int size);
    LiveData<Resource<MessageResponse>> sendMessage(MessageRequestDTO request);
    LiveData<Resource<Void>> revokeMessage(Long messageId);
    LiveData<Resource<MessageResponse>> editMessage(Long messageId, MessageRequestDTO request);
    LiveData<Resource<List<MessageResponse>>> searchMessages(String keyword, Long conversationId, int page, int size);
    
    // Message Status
    LiveData<Resource<Void>> markConversationAsRead(Long conversationId);
    LiveData<Resource<Void>> updateMessageStatus(Long messageId, String status);
    LiveData<Resource<List<MessageStatusResponseDTO>>> getMessageStatuses(Long messageId);

    // Profile & Search
    LiveData<Resource<UserProfileResponseDTO>> getMyProfile();
    LiveData<Resource<UserProfileResponseDTO>> getUserProfile(Long id);
    LiveData<Resource<List<UserSearchResponseDTO>>> searchUsers(String query, int page, int size);

    // Friends
    LiveData<Resource<List<UserResponseDTO>>> getFriendsList();
    LiveData<Resource<FriendRequestResponseDTO>> sendFriendRequest(Long receiverId);
    LiveData<Resource<Void>> acceptFriendRequest(Long senderId);
    LiveData<Resource<Void>> declineFriendRequest(Long senderId);
    LiveData<Resource<Void>> unfriend(Long friendId);
    LiveData<Resource<List<FriendRequestResponseDTO>>> getPendingFriendRequests();
    LiveData<Resource<Void>> blockUser(Long userId);

    // Real-time
    LiveData<MqttMessageDTO> getRealTimeMessages();
    LiveData<Boolean> getConnectionStatus();
}
