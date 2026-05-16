package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.response.FriendRequestResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface FriendApiService {

    @POST("friends/request/{receiverId}")
    Call<FriendRequestResponseDTO> sendFriendRequest(@Path("receiverId") Long receiverId);

    @PUT("friends/request/{senderId}/accept")
    Call<Void> acceptFriendRequest(@Path("senderId") Long senderId);

    @PUT("friends/request/{senderId}/decline")
    Call<Void> declineFriendRequest(@Path("senderId") Long senderId);

    @DELETE("friends/{friendId}")
    Call<Void> unfriend(@Path("friendId") Long friendId);

    @GET("friends")
    Call<List<UserResponseDTO>> getFriendsList();

    @GET("friends/requests/pending")
    Call<List<FriendRequestResponseDTO>> getPendingRequests();
}
