package com.Application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private String publicKey;
    private Long billingId;
    private String status;
}