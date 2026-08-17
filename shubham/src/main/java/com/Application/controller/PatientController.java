package com.Application.controller;

import com.Application.dto.AppointmentPatientResponse;
import com.Application.dto.PatientRequest;
import com.Application.dto.PatientResponse;
import com.Application.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;


    @PostMapping("/patient/register")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest patientRequest){
        PatientResponse response = patientService.createPatient(patientRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/patients")
    public ResponseEntity<List<PatientResponse>> getAllPatient(){
        List<PatientResponse> responses = patientService.getAllPatient();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient")
    public ResponseEntity<PatientResponse> getCurrentPatient(){
        PatientResponse responses = patientService.getCurrentPatient();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/patient")
    public ResponseEntity<PatientResponse> updatePatient(@Valid @RequestBody PatientRequest request){
        PatientResponse response = patientService.updatePatient(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @DeleteMapping("/patient/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/appointment")
    public ResponseEntity<List<AppointmentPatientResponse>> getMyAppointments(){
        List<AppointmentPatientResponse> responses = patientService.getMyAppointments();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long patientId){
        PatientResponse response = patientService.getPatientById(patientId);
        return ResponseEntity.ok(response);
    }





}
