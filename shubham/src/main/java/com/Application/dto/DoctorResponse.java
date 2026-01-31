package com.Application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DoctorResponse {
    private Long id;
    private String first_name;
    private String last_name;
    private String email;
    private String specialization;
    private String schedule;
}
