package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;

import com.midterm.team12345.data.dto.response.AuthResponseDTO;
import com.midterm.team12345.data.dto.request.LoginRequestDTO;
import com.midterm.team12345.data.dto.request.RegisterRequestDTO;
import com.midterm.team12345.util.Resource;

public interface AuthRepository {
    LiveData<Resource<AuthResponseDTO>> login(LoginRequestDTO loginRequestDTO);
    LiveData<Resource<AuthResponseDTO>> register(RegisterRequestDTO registerRequestDTO);
}
