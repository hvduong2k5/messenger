package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.remote.dto.request.InitiateCallRequestDTO;
import com.midterm.team12345.data.remote.dto.request.SignalingRequestDTO;
import com.midterm.team12345.data.remote.dto.response.CallResponseDTO;
import com.midterm.team12345.data.remote.dto.response.SignalingResponseDTO;
import com.midterm.team12345.utils.Resource;
import java.util.List;

public interface CallRepository {
    LiveData<Resource<CallResponseDTO>> initiateCall(InitiateCallRequestDTO request);
    LiveData<Resource<CallResponseDTO>> answerCall(Long id);
    LiveData<Resource<CallResponseDTO>> rejectCall(Long id);
    LiveData<Resource<CallResponseDTO>> endCall(Long id);
    LiveData<Resource<SignalingResponseDTO>> sendSignaling(Long id, SignalingRequestDTO request);
    LiveData<Resource<List<SignalingResponseDTO>>> getSignaling(Long id);
    LiveData<Resource<List<CallResponseDTO>>> getCallHistory();
}
