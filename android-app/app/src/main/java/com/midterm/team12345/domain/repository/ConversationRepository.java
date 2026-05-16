package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.request.ConversationUpdateDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.utils.Resource;

public interface ConversationRepository {
    LiveData<Resource<PageResponse<ConversationResponseDTO>>> getConversations(int page, int size);
    LiveData<Resource<ConversationResponseDTO>> getConversationDetails(Long id);
    LiveData<Resource<Void>> createConversation(ConversationRequestDTO request); // Backend returns Conversation entity, but DTO is safer
    LiveData<Resource<Void>> addParticipant(Long conversationId, Long userId);
    LiveData<Resource<Void>> removeParticipant(Long conversationId, Long userId);
    LiveData<Resource<Void>> updateConversation(Long id, ConversationUpdateDTO request);
    LiveData<Resource<PageResponse<MessageResponseDTO>>> getMessages(Long conversationId, int page, int size);
}
