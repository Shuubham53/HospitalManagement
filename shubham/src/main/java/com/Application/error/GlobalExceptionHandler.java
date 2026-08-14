package com.Application.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ApiError> handlePatientNotFoundException(PatientNotFoundException exception){
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .error("Patient Not Found")
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }
    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ApiError> handleDoctorNotFoundException(DoctorNotFoundException exception){
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .error("Doctor Not Found")
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }
    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ApiError> handleAppointmentNotFoundException(AppointmentNotFoundException exception){
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .error("Appointment Not Found")
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiError> handleBusinessRuleViolation(BusinessRuleViolationException exception) {

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .error("Business Rule Violation")
                .message(exception.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException exception) {

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .error("Bad Credentials")
                .message(exception.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }
    @ExceptionHandler(BillNotFoundException.class)
    public ResponseEntity<ApiError> handleBillNotFoundException(BillNotFoundException exception) {

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .error("Bill Not Found")
                .message(exception.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourcesNotFoundException(ResourceNotFoundException exception) {

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .error("Not Found")
                .message(exception.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExistException(EmailAlreadyExistException exception){
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .error("Email Conflict")
                .message(exception.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .build();
        return new ResponseEntity<>(apiError,HttpStatus.CONFLICT);
    }


}

