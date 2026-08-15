package com.Application.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundRequest {
    private String paymentIntentId;
    private String reason; // Optional: "requested_by_customer", "duplicate", "fraudulent"
}