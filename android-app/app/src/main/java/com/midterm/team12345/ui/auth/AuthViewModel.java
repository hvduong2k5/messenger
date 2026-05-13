package com.midterm.team12345.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.midterm.team12345.data.dto.AuthResponse;
import com.midterm.team12345.data.dto.LoginRequest;
import com.midterm.team12345.data.dto.RegisterRequest;
import com.midterm.team12345.data.repository.AuthRepository;
import com.midterm.team12345.util.Resource;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<AuthResponse>> _loginState = new MutableLiveData<>();
    public LiveData<Resource<AuthResponse>> getLoginState() {
        return _loginState;
    }

    private final MutableLiveData<Resource<AuthResponse>> _registerState = new MutableLiveData<>();
    public LiveData<Resource<AuthResponse>> getRegisterState() {
        return _registerState;
    }

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        // Using MediatorLiveData or simply observing the repository's LiveData
        authRepository.login(loginRequest).observeForever(resource -> {
            _loginState.setValue(resource);
        });
    }

    public void register(String username, String email, String password, String fullName) {
        RegisterRequest registerRequest = new RegisterRequest(username, email, password, fullName);
        authRepository.register(registerRequest).observeForever(resource -> {
            _registerState.setValue(resource);
        });
    }
}
