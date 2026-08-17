package com.Application.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundRequest {

    @NotBlank(message = "Payment intent ID is required")
    private String paymentIntentId;

    @Pattern(
            regexp = "requested_by_customer|duplicate|fraudulent",
            message = "Reason must be requested_by_customer, duplicate, or fraudulent"
    )
    private String reason;
}