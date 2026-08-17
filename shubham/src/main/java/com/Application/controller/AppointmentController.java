package com.Application.controller;

import com.Application.dto.*;
import com.Application.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/appointment")
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.bookApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/appointment/{appointmentId}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(@PathVariable Long appointmentId) {
        AppointmentResponse appointmentResponse = appointmentService.confirmAppointment(appointmentId);
        return ResponseEntity.ok(appointmentResponse);
    }

    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @PutMapping("/appointment/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@Valid @RequestBody CancellationRequest request) {
        AppointmentResponse response =
                appointmentService.cancelAppointment(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/appointment/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@Valid @RequestBody CompleteRequest request) {
        AppointmentResponse response = appointmentService.completeAppointment(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT', 'DOCTOR')")
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @GetMapping("/appointment/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointmentByPatientId(@PathVariable Long patientId) {
        List<AppointmentResponse> responses = appointmentService.getAppointmentsByPatientId(patientId);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping("/appointment/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointmentByDoctorId(@PathVariable Long doctorId) {
        List<AppointmentResponse> responses = appointmentService.getAllAppointmentByDoctorId(doctorId);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/appointment/status/{status}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentByStatus(@PathVariable String status) {
        return ResponseEntity.ok(appointmentService.getAppointmentByStatus(status));
    }
}