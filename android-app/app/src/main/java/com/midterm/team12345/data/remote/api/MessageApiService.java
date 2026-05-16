package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MessageApiService {

    @POST("messages")
    Call<MessageResponseDTO> sendMessage(@Body MessageRequestDTO request);

    @DELETE("messages/{messageId}")
    Call<Void> revokeMessage(@Path("messageId") Long messageId);

    @PUT("messages/{messageId}")
    Call<MessageResponseDTO> editMessage(@Path("messageId") Long messageId, @Body MessageRequestDTO request);

    @GET("messages/search")
    Call<PageResponse<MessageResponseDTO>> searchMessages(
            @Query("keyword") String keyword,
            @Query("conversationId") Long conversationId,
            @Query("page") int page,
            @Query("size") int size
    );
}
