package com.team12345.messenger.controller;

import com.team12345.messenger.dto.request.LoginRequestDTO;
import com.team12345.messenger.dto.request.RegisterRequestDTO;
import com.team12345.messenger.dto.response.AuthResponseDTO;
import com.team12345.messenger.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        AuthResponseDTO authResponseDTO = authService.register(registerRequestDTO);
        return ResponseEntity.ok(authResponseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        AuthResponseDTO authResponseDTO = authService.login(loginRequestDTO);
        return ResponseEntity.ok(authResponseDTO);
    }
}
