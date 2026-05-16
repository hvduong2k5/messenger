package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageStatusResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.utils.Resource;
import java.util.List;

public interface MessageRepository {
    LiveData<Resource<MessageResponseDTO>> sendMessage(MessageRequestDTO request);
    LiveData<Resource<Void>> revokeMessage(Long messageId);
    LiveData<Resource<MessageResponseDTO>> editMessage(Long messageId, MessageRequestDTO request);
    LiveData<Resource<PageResponse<MessageResponseDTO>>> searchMessages(String keyword, Long conversationId, int page, int size);
    
    // Message Status logic
    LiveData<Resource<Void>> markConversationAsRead(Long conversationId);
    LiveData<Resource<Void>> updateMessageStatus(Long messageId, String status);
    LiveData<Resource<List<MessageStatusResponseDTO>>> getMessageStatuses(Long messageId);
}
