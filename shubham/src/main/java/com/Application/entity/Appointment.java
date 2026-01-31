package com.Application.entity;

import com.Application.entity.type.AppointmentStatus;
import com.Application.entity.type.AppointmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime appointment_date;
    private String reason;
    private String prescription;

    @Enumerated(EnumType.STRING)
    private AppointmentType appointmentType;
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private LocalDateTime startTime; // appointment start
    private LocalDateTime endTime;   // appointment end

    @ManyToOne()
    @JoinColumn(name = "patient_id",nullable = false)
    private Patient patient;

    @ManyToOne()
    @JoinColumn(name = "doctor_id",nullable = false)
    private Doctor doctor;



}
