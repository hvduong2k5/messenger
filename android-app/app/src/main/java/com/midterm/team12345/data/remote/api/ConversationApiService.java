package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.request.ConversationUpdateDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ConversationApiService {
    @GET("conversations")
    Call<PageResponse<ConversationResponseDTO>> getConversations(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("conversations/{id}")
    Call<ConversationResponseDTO> getConversationDetails(@Path("id") Long id);

    @POST("conversations")
    Call<ConversationResponseDTO> createConversation(@Body ConversationRequestDTO request);

    @PUT("conversations/{id}")
    Call<ConversationResponseDTO> updateConversation(@Path("id") Long id, @Body ConversationUpdateDTO request);

    @POST("conversations/{id}/participants")
    Call<Void> addParticipant(@Path("id") Long id, @Body Map<String, Long> body);


    @DELETE("conversations/{id}/participants/{userId}")
    Call<Void> removeParticipant(@Path("id") Long id, @Path("userId") Long userId);

    @GET("conversations/{id}/participants")
    Call<PageResponse<ParticipantResponseDTO>> getParticipants(
            @Path("id") Long id,
            @Query("keyword") String keyword,
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("conversations/{id}/leave")
    Call<Map<String, String>> leaveConversation(
            @Path("id") Long conversationId
    );

    @PATCH("conversations/{id}/participants/{participantId}/role")
    Call<Map<String, String>> updateParticipantRole(
            @Path("id") Long conversationId,
            @Path("participantId") Long participantId,
            @Body Map<String, String> body
    );

    @GET("conversations/{conversationId}/messages")
    Call<PageResponse<MessageResponseDTO>> getMessages(
            @Path("conversationId") Long conversationId,
            @Query("page") int page,
            @Query("size") int size
    );
}
