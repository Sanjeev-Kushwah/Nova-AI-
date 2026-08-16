package com.example.novaai.service;

import com.example.novaai.dto.AuthResponse;
import com.example.novaai.dto.LoginRequest;
import com.example.novaai.dto.RefreshRequest;
import com.example.novaai.dto.RegisterRequest;
import com.example.novaai.dto.UserResponse;
import com.example.novaai.entity.User;
import com.example.novaai.exception.AppException;
import com.example.novaai.exception.ConflictException;
import com.example.novaai.exception.ResourceNotFoundException;
import com.example.novaai.exception.UnauthorizedException;
import com.example.novaai.mapper.UserMapper;
import com.example.novaai.repository.UserRepository;
import com.example.novaai.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = User.builder()
            .email(request.email())
            .name(request.name())
            .passwordHash(passwordEncoder.encode(request.password()))
            .build();
        user = userRepository.save(user);

        log.info("User registered: id={}, email={}", user.getId(), user.getEmail());

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        log.info("User logged in: id={}", user.getId());

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        return userMapper.toResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        if (!"refresh".equals(jwtTokenProvider.extractTokenType(refreshToken))) {
            throw new UnauthorizedException("Invalid refresh token type");
        }

        UUID userId = jwtTokenProvider.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        UserResponse userResponse = userMapper.toResponse(user);
        return AuthResponse.of(accessToken, refreshToken, userResponse);
    }
}
