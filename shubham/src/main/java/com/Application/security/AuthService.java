package com.Application.security;

import com.Application.dto.UserRequest;
import com.Application.dto.UserResponse;
import com.Application.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;

    public UserResponse login(UserRequest userRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword()));
            User user = (User) authentication.getPrincipal();


            String accessToken = authUtil.generateAccessToken(user.getUsername(), user.getId());

            String refreshToken = authUtil.generateRefreshToken(user.getUsername(), user.getId());

            return UserResponse.builder()
                    .userId(user.getId())
                    .jwtToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for username: {}",
                    userRequest.getUsername());
            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }
    }
}