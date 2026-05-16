package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MessageApiService {
    @GET("conversations/{conversationId}/messages")
    Call<PageResponse<MessageResponseDTO>> getMessagesByConversation(@Path("conversationId") Long conversationId);

    @POST("messages")
    Call<MessageResponseDTO> sendMessage(@Body MessageRequestDTO request);
}
