package com.Application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompleteRequest {

    @NotNull(message = "Appointment ID is required")
    @Positive(message = "Appointment ID must be positive")
    private Long appointmentId;

    @NotBlank(message = "Prescription is required")
    @Size(max = 2000, message = "Prescription cannot exceed 2000 characters")
    private String prescription;
}