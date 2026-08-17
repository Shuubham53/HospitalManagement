package com.Application.controller;

import com.Application.dto.*;
import com.Application.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/doctor/register")
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorRequest request) {
        DoctorResponse response = doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctor() {
        List<DoctorResponse> responses = doctorService.getAllDoctor();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor")
    public ResponseEntity<DoctorResponse> getCurrentDoctorProfile() {
        DoctorResponse responses = doctorService.getCurrentDoctorProfile();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/doctor")
    public ResponseEntity<DoctorResponse> updateDoctor(@Valid @RequestBody DoctorRequest request) {
        DoctorResponse response = doctorService.updateDoctor(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @DeleteMapping("/doctor/{doctorId}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long doctorId) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/appointments")
    public ResponseEntity<List<AppointmentDoctorResponse>> getMyAppointments() {
        List<AppointmentDoctorResponse> responses = doctorService.getMyAppointments();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/doctor/specialization/{name}")
    public ResponseEntity<List<DoctorResponse>> getDoctorsBySpecialization(@PathVariable String name) {
        List<DoctorResponse> responses = doctorService.getDoctorBySpecialization(name);
        return ResponseEntity.ok(responses);
    }
}