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
public class AppointmentDoctorResponse {

    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime appointmentDate;

    private AppointmentStatus status;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String patientPhone;
    private String reason;
    private String prescription;
}
