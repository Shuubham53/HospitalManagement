package com.Application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancellationRequest {

    @NotNull(message = "Appointment ID is required")
    @Positive(message = "Appointment ID must be positive")
    private Long appointmentId;
}