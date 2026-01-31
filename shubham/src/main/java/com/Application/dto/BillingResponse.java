package com.Application.dto;

import com.Application.entity.type.PaymentMethod;
import com.Application.entity.type.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BillingResponse {

        private Long id;

        private BigDecimal amount;

        private LocalDateTime billingDate;

        private PaymentStatus status;

        private PaymentMethod paymentMethod;

        private String referenceNumber;

        private Long patientId;
}
