package com.neocamp.api_agendamento.controller;

import com.neocamp.api_agendamento.domain.dto.request.LoginRequestDTO;
import com.neocamp.api_agendamento.domain.dto.request.RegisterRequestDTO;
import com.neocamp.api_agendamento.domain.dto.response.LoginResponseDTO;
import com.neocamp.api_agendamento.domain.dto.response.LogoutResponseDTO;
import com.neocamp.api_agendamento.domain.dto.response.RegisterResponseDTO;
import com.neocamp.api_agendamento.service.UserRegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRegistrationService userRegistrationService;

    public AuthController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = userRegistrationService.loginUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        RegisterResponseDTO response = userRegistrationService.registerUser(registerRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout() {
        LogoutResponseDTO response = userRegistrationService.logoutUser();
        return ResponseEntity.ok(response);
    }
}
