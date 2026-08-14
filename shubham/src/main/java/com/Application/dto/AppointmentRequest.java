package com.Application.dto;

import com.Application.entity.Doctor;
import com.Application.entity.Patient;
import com.Application.entity.type.AppointmentStatus;
import com.Application.entity.type.AppointmentType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentRequest {
    private Long doctorId;
    private LocalDateTime appointment_date;
    private String reason;
    private AppointmentType appointmentType;
}
