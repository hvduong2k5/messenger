package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageStatusResponseDTO;
import com.midterm.team12345.utils.Resource;

import java.io.File;
import java.util.List;

public interface MessageRepository {
    /**
     * Lấy danh sách tin nhắn (có phân trang)
     */
    LiveData<Resource<List<MessageResponseDTO>>> getMessages(Long conversationId, int page, int size);

    LiveData<Resource<Boolean>> fetchNextPageOfMessages(Long conversationId, int page, int size);

    LiveData<Resource<MessageResponseDTO>> sendMessage(MessageRequestDTO request);

    /**
     * Gửi tin nhắn kèm tệp đính kèm (Multipart)
     */
    LiveData<Resource<MessageResponseDTO>> sendMessageWithAttachments(Long conversationId, String content, String clientMessageId, List<File> files);

    LiveData<Resource<Void>> revokeMessage(Long messageId);
    LiveData<Resource<MessageResponseDTO>> editMessage(Long messageId, MessageRequestDTO request);
    
    // Logic trạng thái tin nhắn
    LiveData<Resource<Void>> markConversationAsRead(Long conversationId);
    LiveData<Resource<Void>> updateMessageStatus(Long messageId, String status);
    LiveData<Resource<List<MessageStatusResponseDTO>>> getMessageStatuses(Long messageId);

    /**
     * Đồng bộ hóa tin nhắn đang chờ gửi khi có mạng trở lại.
     */
    void syncPendingMessages();
}
