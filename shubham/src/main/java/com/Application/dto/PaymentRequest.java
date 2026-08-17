package com.Application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Bill ID is required")
    @Positive(message = "Bill ID must be positive")
    private Long billId;
}