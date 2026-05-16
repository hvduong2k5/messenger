package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ConversationApiService {
    @GET("conversations")
    Call<PageResponse<ConversationResponseDTO>> getConversations();

    @POST("conversations")
    Call<ConversationResponseDTO> createConversation(@Body ConversationRequestDTO request);
}
