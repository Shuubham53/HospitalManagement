package com.Application.dto;

import com.Application.entity.type.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AppointmentPatientResponse {


    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    // Doctor info
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    // Appointment details
    private String reason;
    private String prescription;
}
