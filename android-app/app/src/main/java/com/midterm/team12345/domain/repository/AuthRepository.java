package com.midterm.team12345.domain.repository;

import androidx.lifecycle.LiveData;

import com.midterm.team12345.data.remote.dto.response.AuthResponseDTO;
import com.midterm.team12345.data.remote.dto.request.LoginRequestDTO;
import com.midterm.team12345.data.remote.dto.request.RegisterRequestDTO;
import com.midterm.team12345.utils.Resource;

public interface AuthRepository {
    LiveData<Resource<AuthResponseDTO>> login(LoginRequestDTO loginRequestDTO);
    LiveData<Resource<AuthResponseDTO>> register(RegisterRequestDTO registerRequestDTO);
}
