package com.midterm.team12345.data.remote.api;

import com.midterm.team12345.data.remote.dto.request.InitiateCallRequestDTO;
import com.midterm.team12345.data.remote.dto.request.SignalingRequestDTO;
import com.midterm.team12345.data.remote.dto.response.CallResponseDTO;
import com.midterm.team12345.data.remote.dto.response.SignalingResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CallApiService {

    @POST("calls")
    Call<CallResponseDTO> initiateCall(@Body InitiateCallRequestDTO request);

    @PUT("calls/{id}/answer")
    Call<CallResponseDTO> answerCall(@Path("id") Long id);

    @PUT("calls/{id}/reject")
    Call<CallResponseDTO> rejectCall(@Path("id") Long id);

    @PUT("calls/{id}/end")
    Call<CallResponseDTO> endCall(@Path("id") Long id);

    @POST("calls/{id}/signaling")
    Call<SignalingResponseDTO> sendSignaling(@Path("id") Long id, @Body SignalingRequestDTO request);

    @GET("calls/{id}/signaling")
    Call<List<SignalingResponseDTO>> getSignaling(@Path("id") Long id);

    @GET("calls/history")
    Call<List<CallResponseDTO>> getCallHistory();
}
