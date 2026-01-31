package com.Application.dto;

import com.Application.entity.type.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientResponse {
    private Long id;
    private String first_name;
    private String last_name;
    private String birthDate;
    private String email;
    private Gender gender;
    private String address;
    private String phone;
}
