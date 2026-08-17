package com.Application.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmRequest {

    @NotBlank(message = "Payment intent ID is required")
    private String paymentIntentId;
}