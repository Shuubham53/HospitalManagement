package com.Application.security;

import com.Application.dto.UserRequest;
import com.Application.dto.UserResponse;
import com.Application.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    public UserResponse login(UserRequest userRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userRequest.getUsername(),userRequest.getPassword()));
            User user = (User) authentication.getPrincipal();
            String token = authUtil.generateAccessToken(user.getUsername(),user.getId());
            return UserResponse.builder()
                    .userId(user.getId())
                    .jwtToken(token)
                    .build();
        } catch (BadCredentialsException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new BadCredentialsException("Invalid email or password", e);
        } catch (Exception e) {
            System.out.println("Error type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            throw new IllegalArgumentException("Authentication error: " + e.getMessage(), e);
        }
    }
}
