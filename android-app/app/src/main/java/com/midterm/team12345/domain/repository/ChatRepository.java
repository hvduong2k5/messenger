package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.remote.dto.response.ConversationResponse;
import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponse;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.utils.Resource;
import java.util.List;

public interface ChatRepository {
    LiveData<Resource<List<ConversationResponse>>> getConversations();
    LiveData<Resource<List<UserResponseDTO>>> getFriends();
    LiveData<Resource<ConversationResponse>> createConversation(ConversationRequestDTO request);
    LiveData<Resource<List<MessageResponse>>> getMessages(Long conversationId);
    LiveData<Resource<MessageResponse>> sendMessage(MessageRequestDTO request);
    LiveData<Resource<UserProfileResponseDTO>> getMyProfile();
    LiveData<MqttMessageDTO> getRealTimeMessages();
    LiveData<Boolean> getConnectionStatus();
}
