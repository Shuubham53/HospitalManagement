package com.Application.controller;

import com.Application.dto.UserRequest;
import com.Application.dto.UserResponse;
import com.Application.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @PostMapping("/auth/login")
    public ResponseEntity<UserResponse> login(@RequestBody UserRequest userRequest) {
        UserResponse response = authService.login(userRequest);
        return ResponseEntity.ok(response);
    }
}
