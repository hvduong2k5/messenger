package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface MessageApiService {

    @FormUrlEncoded
    @POST("messages")
    Call<MessageResponseDTO> sendMessage(
            @Field("senderId") Long senderId,
            @Field("conversationId") Long conversationId,
            @Field("content") String content,
            @Field("clientMessageId") String clientMessageId
    );

    @Multipart
    @POST("messages")
    Call<MessageResponseDTO> sendMessageMultipart(
            @Part("conversationId") RequestBody conversationId,
            @Part("content") RequestBody content,
            @Part("clientMessageId") RequestBody clientMessageId,
            @Part List<MultipartBody.Part> files
    );

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
