package com.midterm.team12345.data.remote;

import com.midterm.team12345.data.dto.MessageRequestDTO;
import com.midterm.team12345.data.dto.MessageResponseDTO;
import com.midterm.team12345.data.dto.PageResponse;

import java.util.List;

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
