package com.midterm.team12345.data.remote;

import com.midterm.team12345.data.dto.ConversationRequestDTO;
import com.midterm.team12345.data.dto.ConversationResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ConversationApiService {
    @GET("conversations")
    Call<List<ConversationResponseDTO>> getConversations();

    @POST("conversations")
    Call<ConversationResponseDTO> createConversation(@Body ConversationRequestDTO request);
}
