package com.Application.controller;

import com.Application.dto.RefreshTokenRequestDto;
import com.Application.dto.UserRequest;
import com.Application.dto.UserResponse;
import com.Application.entity.User;
import com.Application.error.BusinessRuleViolationException;
import com.Application.repository.UserRepository;
import com.Application.security.AuthService;
import com.Application.security.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;

    @PostMapping("/auth/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserRequest userRequest) {
        UserResponse response = authService.login(userRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<UserResponse> refreshToken(
           @Valid @RequestBody RefreshTokenRequestDto request) {

        String refreshToken = request.getRefreshToken();

        if (!authUtil.isRefreshToken(refreshToken)) {
            throw new BusinessRuleViolationException(
                    "Invalid refresh token"
            );
        }
        String username = authUtil.extractUsernameFromToken(refreshToken);

        User user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found or account is inactive"));

        String newAccessToken = authUtil.generateAccessToken(user.getUsername(), user.getId());

        return ResponseEntity.ok(UserResponse.builder().userId(user.getId())
                .jwtToken(newAccessToken).refreshToken(refreshToken)
                .build()
        );
    }
}
