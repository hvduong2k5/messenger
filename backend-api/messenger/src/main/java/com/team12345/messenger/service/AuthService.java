package com.team12345.messenger.service;

import com.team12345.messenger.dto.request.ForgotPasswordRequestDTO;
import com.team12345.messenger.dto.request.LoginRequestDTO;
import com.team12345.messenger.dto.request.RegisterRequestDTO;
import com.team12345.messenger.dto.request.ResetPasswordRequestDTO;
import com.team12345.messenger.dto.response.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO register(RegisterRequestDTO registerRequestDTO);

    AuthResponseDTO login(LoginRequestDTO loginRequestDTO);

    void forgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO);

    void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO);

    void logout(Long userId, String token);
}
