package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponse;
import com.midterm.team12345.data.remote.dto.response.MessageStatusResponseDTO;
import com.midterm.team12345.utils.Resource;
import java.util.List;

public interface MessageRepository {
    /**
     * Lấy danh sách tin nhắn (có phân trang)
     */
    LiveData<Resource<List<MessageResponse>>> getMessages(Long conversationId, int page, int size);

    LiveData<Resource<MessageResponse>> sendMessage(MessageRequestDTO request);
    LiveData<Resource<Void>> revokeMessage(Long messageId);
    LiveData<Resource<MessageResponse>> editMessage(Long messageId, MessageRequestDTO request);
    
    // Logic trạng thái tin nhắn
    LiveData<Resource<Void>> markConversationAsRead(Long conversationId);
    LiveData<Resource<Void>> updateMessageStatus(Long messageId, String status);
    LiveData<Resource<List<MessageStatusResponseDTO>>> getMessageStatuses(Long messageId);
}
