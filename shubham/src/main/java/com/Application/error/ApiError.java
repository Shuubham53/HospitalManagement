package com.Application.error;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Builder
@Getter
public class ApiError {
    LocalDateTime timestamp;
    int status;
    String error;
    String message;
}
