package com.Application.controller;

import com.Application.dto.AppointmentPatientResponse;
import com.Application.dto.PatientRequest;
import com.Application.dto.PatientResponse;
import com.Application.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    @PostMapping("/patient/register")
    public ResponseEntity<PatientResponse> createPatient(@RequestBody PatientRequest patientRequest){
        PatientResponse response = patientService.createPatient(patientRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientResponse>> getAllPatient(){
        List<PatientResponse> responses = patientService.getAllPatient();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/patient")
    public ResponseEntity<PatientResponse> getCurrentPatient(){
        PatientResponse responses = patientService.getCurrentPatient();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/patient")
    public ResponseEntity<PatientResponse> updatePatient(@RequestBody PatientRequest request){
        PatientResponse response = patientService.updatePatient(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/patient/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/patient/appointment")
    public ResponseEntity<List<AppointmentPatientResponse>> getMyAppointments(){
        List<AppointmentPatientResponse> responses = patientService.getMyAppointments();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long patientId){
        PatientResponse response = patientService.getPatientById(patientId);
        return ResponseEntity.ok(response);
    }





}
