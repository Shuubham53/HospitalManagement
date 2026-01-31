package com.Application.dto;

import lombok.Data;

@Data
public class CompleteRequest {
    private Long doctorId;
    private Long appointmentId;
    private String prescription;
}
