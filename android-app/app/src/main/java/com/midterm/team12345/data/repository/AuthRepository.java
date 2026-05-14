package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;

import com.midterm.team12345.data.dto.response.AuthResponse;
import com.midterm.team12345.data.dto.request.LoginRequest;
import com.midterm.team12345.data.dto.request.RegisterRequest;
import com.midterm.team12345.util.Resource;

public interface AuthRepository {
    LiveData<Resource<AuthResponse>> login(LoginRequest loginRequest);
    LiveData<Resource<AuthResponse>> register(RegisterRequest registerRequest);
}
