package com.Application.dto;

import com.Application.entity.type.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class PatientRequest {
    private String first_name;
    private String last_name;
    private String birthDate;
    private String email;
    private String password;
    private Gender gender;
    private String address;
    private String phone;


}
