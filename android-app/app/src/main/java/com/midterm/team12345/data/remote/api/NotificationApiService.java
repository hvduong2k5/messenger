package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.response.NotificationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApiService {

    @GET("notifications")
    Call<PageResponse<NotificationResponseDTO>> getNotifications(
            @Query("page") int page,
            @Query("size") int size
    );

    @PATCH("notifications/{id}/read")
    Call<Void> markAsRead(@Path("id") Long id);

    @PUT("notifications/read-all")
    Call<Map<String, String>> markAllAsRead();

    @DELETE("notifications/{id}")
    Call<Void> deleteNotification(@Path("id") Long id);
}
