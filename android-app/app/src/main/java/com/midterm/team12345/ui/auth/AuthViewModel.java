package com.midterm.team12345.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.midterm.team12345.data.dto.response.AuthResponseDTO;
import com.midterm.team12345.data.dto.request.LoginRequestDTO;
import com.midterm.team12345.data.dto.request.RegisterRequestDTO;
import com.midterm.team12345.data.repository.AuthRepository;
import com.midterm.team12345.util.Resource;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<AuthResponseDTO>> _loginState = new MutableLiveData<>();
    public LiveData<Resource<AuthResponseDTO>> getLoginState() {
        return _loginState;
    }

    private final MutableLiveData<Resource<AuthResponseDTO>> _registerState = new MutableLiveData<>();
    public LiveData<Resource<AuthResponseDTO>> getRegisterState() {
        return _registerState;
    }

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void login(String username, String password) {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(username, password);
        // Using MediatorLiveData or simply observing the repository's LiveData
        authRepository.login(loginRequestDTO).observeForever(resource -> {
            _loginState.setValue(resource);
        });
    }

    public void register(String username, String email, String password, String fullName) {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(username, email, password);
        authRepository.register(registerRequestDTO).observeForever(resource -> {
            _registerState.setValue(resource);
        });
    }
}
