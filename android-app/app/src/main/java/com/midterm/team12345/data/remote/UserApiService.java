package com.midterm.team12345.data.remote;

import com.midterm.team12345.data.dto.UserProfileResponseDTO;
import com.midterm.team12345.data.dto.UserDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UserApiService {
    @GET("users/me")
    Call<UserProfileResponseDTO> getMyProfile();

    @GET("users/search")
    Call<List<UserDTO>> searchUsers(@Query("keyword") String keyword);

    @GET("friends")
    Call<List<UserDTO>> getFriends();
}
