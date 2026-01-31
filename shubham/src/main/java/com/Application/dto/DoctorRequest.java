package com.Application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorRequest {
    private String first_name;
    private String last_name;
    private String email;
    private String specialization;
    private String schedule;
    private String password;
}
