package com.example.novaai.controller;

import com.example.novaai.dto.ApiResponse;
import com.example.novaai.dto.AuthResponse;
import com.example.novaai.dto.LoginRequest;
import com.example.novaai.dto.RefreshRequest;
import com.example.novaai.dto.RegisterRequest;
import com.example.novaai.dto.UserResponse;
import com.example.novaai.security.SecurityUtils;
import com.example.novaai.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success(
            authService.getCurrentUser(SecurityUtils.getCurrentUserId())
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh an access token using a refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout (stateless — client discards tokens)")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT is stateless; the client simply discards the tokens.
        // In a production system you might add a token blocklist in Redis.
        return ResponseEntity.ok(ApiResponse.success());
    }
}
