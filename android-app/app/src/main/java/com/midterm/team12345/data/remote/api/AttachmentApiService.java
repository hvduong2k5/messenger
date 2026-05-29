package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.response.AttachmentResponseDTO;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface AttachmentApiService {

    @Multipart
    @POST("attachments/upload")
    Call<AttachmentResponseDTO> uploadFile(@Part MultipartBody.Part file);

    @GET("attachments/{id}")
    Call<AttachmentResponseDTO> getAttachment(@Path("id") Long id);

    @GET("attachments/message/{messageId}")
    Call<List<AttachmentResponseDTO>> getAttachmentsByMessage(@Path("messageId") Long messageId);

    @DELETE("attachments/{id}")
    Call<Void> deleteAttachment(@Path("id") Long id);
}
