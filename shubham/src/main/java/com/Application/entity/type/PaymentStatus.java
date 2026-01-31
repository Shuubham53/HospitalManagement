package com.Application.entity.type;

public enum PaymentStatus {
        UNPAID,     // Bill generated
        CREATED,    // Stripe payment intent created
        PENDING,    // Payment processing
        PAID,       // Payment verified
        FAILED,     // Payment attempt failed
        CANCELLED,
        REFUNDED
}
