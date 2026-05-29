package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.request.ForgotPasswordRequestDTO;
import com.midterm.team12345.data.remote.dto.request.LoginRequestDTO;
import com.midterm.team12345.data.remote.dto.request.RegisterRequestDTO;
import com.midterm.team12345.data.remote.dto.request.ResetPasswordRequestDTO;
import com.midterm.team12345.data.remote.dto.response.AuthResponseDTO;
import com.midterm.team12345.data.remote.dto.response.ForgotPasswordResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("/auth/login")
    Call<AuthResponseDTO> login(@Body LoginRequestDTO loginRequestDTO);

    @POST("/auth/register")
    Call<AuthResponseDTO> register(@Body RegisterRequestDTO registerRequestDTO);

    @POST("/auth/forgot-password")
    Call<ForgotPasswordResponseDTO> forgotPassword(@Body ForgotPasswordRequestDTO request);

    @POST("/auth/reset-password")
    Call<ForgotPasswordResponseDTO> resetPassword(@Body ResetPasswordRequestDTO request);

    @POST("/auth/logout")
    Call<Void> logout();
}
