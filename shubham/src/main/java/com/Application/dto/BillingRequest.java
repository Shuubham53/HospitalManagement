package com.Application.dto;

import com.Application.entity.type.PaymentMethod;
import com.Application.entity.type.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BillingRequest {
    private Long appointmentId;
    private Long patientId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
}
