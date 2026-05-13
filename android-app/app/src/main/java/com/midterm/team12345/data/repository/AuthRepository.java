package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;

import com.midterm.team12345.data.dto.AuthResponse;
import com.midterm.team12345.data.dto.LoginRequest;
import com.midterm.team12345.data.dto.RegisterRequest;
import com.midterm.team12345.util.Resource;

public interface AuthRepository {
    LiveData<Resource<AuthResponse>> login(LoginRequest loginRequest);
    LiveData<Resource<AuthResponse>> register(RegisterRequest registerRequest);
}
