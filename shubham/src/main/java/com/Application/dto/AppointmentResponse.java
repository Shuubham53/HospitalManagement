package com.Application.dto;

import com.Application.entity.type.AppointmentStatus;
import com.Application.entity.type.AppointmentType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class AppointmentResponse {
    private Long id;
    private LocalDateTime appointment_date;
    private Long patientId;
    private Long doctorId;
    private String reason;
    private String prescription;
    private double durationTime;
    private AppointmentStatus status;
    private AppointmentType appointmentType;



}
