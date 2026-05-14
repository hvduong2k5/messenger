package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.dto.ConversationResponse;
import com.midterm.team12345.data.dto.ConversationRequestDTO;
import com.midterm.team12345.data.dto.MqttMessageDTO;
import com.midterm.team12345.data.dto.UserDTO;
import com.midterm.team12345.util.Resource;
import java.util.List;

public interface ChatRepository {
    LiveData<Resource<List<ConversationResponse>>> getConversations();
    LiveData<Resource<List<UserDTO>>> getFriends();
    LiveData<Resource<ConversationResponse>> createConversation(ConversationRequestDTO request);
    LiveData<MqttMessageDTO> getRealTimeMessages();
    LiveData<Boolean> getConnectionStatus();
}
