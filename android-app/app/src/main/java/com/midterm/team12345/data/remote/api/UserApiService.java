package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.request.UpdateProfileRequestDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApiService {

    @GET("users/me")
    Call<UserProfileResponseDTO> getMyProfile();

    @GET("users/{id}")
    Call<UserProfileResponseDTO> getUserProfileById(@Path("id") Long id);

    @PUT("users/profile")
    Call<UserProfileResponseDTO> updateProfile(@Body UpdateProfileRequestDTO request);

    @Multipart
    @PATCH("users/avatar")
    Call<UserProfileResponseDTO> updateAvatar(@Part MultipartBody.Part file);

    @Multipart
    @PATCH("users/me")
    Call<UserProfileResponseDTO> updateProfileComplete(
            @Part MultipartBody.Part avatar,
            @Part("data") RequestBody data
    );

    @GET("users/search/paged")
    Call<PageResponse<UserSearchResponseDTO>> searchUsers(
            @Query("q") String query,
            @Query("page") int page,
            @Query("size") int size
    );
}
