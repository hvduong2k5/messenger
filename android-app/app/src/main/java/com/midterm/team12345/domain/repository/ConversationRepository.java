package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.request.ConversationUpdateDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.utils.Resource;

public interface ConversationRepository {
    LiveData<Resource<PageResponse<ConversationResponseDTO>>> getConversations(int page, int size);
    LiveData<Resource<ConversationResponseDTO>> getConversationDetails(Long id);
    LiveData<Resource<ConversationResponseDTO>> createConversation(ConversationRequestDTO request);
    LiveData<Resource<Void>> addParticipant(Long conversationId, Long userId);
    LiveData<Resource<Void>> removeParticipant(Long conversationId, Long userId);
    LiveData<Resource<Void>> updateConversation(Long id, ConversationUpdateDTO request);
    LiveData<Resource<Void>> leaveConversation(Long conversationId);
    LiveData<Resource<PageResponse<MessageResponseDTO>>> getMessages(Long conversationId, int page, int size);
    
    // Thêm hàm lấy danh sách thành viên
    LiveData<Resource<PageResponse<ParticipantResponseDTO>>> getParticipants(Long id, String keyword, int page, int size);

    // Real-time (Mqtt)
    LiveData<MqttMessageDTO> getRealTimeMessages();
    LiveData<Boolean> getConnectionStatus();
    void emitRealTimeMessage(MqttMessageDTO message);
    void updateConnectionStatus(boolean connected);
}
