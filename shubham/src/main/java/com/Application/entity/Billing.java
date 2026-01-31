package com.Application.entity;

import com.Application.entity.type.PaymentMethod;
import com.Application.entity.type.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Billing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;
    private LocalDateTime billing_date;
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;


    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(unique = true)
    private String referenceNumber;


    @ManyToOne()
    @JoinColumn(name = "patient_id",nullable = false)
    private Patient patient;

    @OneToOne
    @JoinColumn(name = "appointment_id",nullable = false)
    private Appointment appointment;

}
