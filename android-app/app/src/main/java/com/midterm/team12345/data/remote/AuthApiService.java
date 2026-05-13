package com.midterm.team12345.data.remote;

import com.midterm.team12345.data.dto.AuthResponse;
import com.midterm.team12345.data.dto.LoginRequest;
import com.midterm.team12345.data.dto.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("/auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest registerRequest);
}
