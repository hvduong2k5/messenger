package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.response.MessageStatusResponseDTO;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MessageStatusApiService {

    @PUT("message-statuses/conversations/{conversationId}/read")
    Call<Void> markConversationAsRead(@Path("conversationId") Long conversationId);

    @PATCH("message-statuses/messages/{messageId}")
    Call<Void> updateMessageStatus(
            @Path("messageId") Long messageId,
            @Body Map<String, String> statusMap
    );

    @GET("message-statuses/messages/{messageId}")
    Call<List<MessageStatusResponseDTO>> getMessageStatuses(@Path("messageId") Long messageId);
}
