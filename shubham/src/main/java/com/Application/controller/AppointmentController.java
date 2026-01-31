package com.Application.controller;

import com.Application.dto.*;
import com.Application.entity.type.AppointmentStatus;
import com.Application.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;
    /// Admin
    @PostMapping("/appointment")
    public ResponseEntity<AppointmentResponse> bookAppointment(@RequestBody AppointmentRequest request){
        AppointmentResponse response = appointmentService.bookApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /// DOCTOR
    @PutMapping("/doctor/{doctorId}/appointment/{appointmentId}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(@PathVariable Long doctorId,@PathVariable Long appointmentId){
        AppointmentResponse appointmentResponse = appointmentService.confirmAppointment(appointmentId,doctorId);
        return ResponseEntity.ok(appointmentResponse);
    }

    /// Doctor / ADMIN / Patient
    @PutMapping("/appointment/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@RequestBody CancellationRequest request){
        AppointmentResponse response = appointmentService.cancelAppointment(request);
        return ResponseEntity.ok(response);
    }

    /// Doctor
    @PutMapping("/appointment/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@RequestBody CompleteRequest request){
        AppointmentResponse response = appointmentService.completeAppointment(request);
        return ResponseEntity.ok(response);
    }

    /// ADMIN
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>>getAllAppointments(){
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }
    /// Admin
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long appointmentId){
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
    }
    /// ADMIN
    @GetMapping("/appointment/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointmentByPatientId(@PathVariable Long patientId){
        List<AppointmentResponse> responses = appointmentService.getAppointmentsByPatientId(patientId);
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/appointment/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointmentByDoctorId(@PathVariable Long doctorId){
        List<AppointmentResponse> responses = appointmentService.getAllAppointmentByDoctorId(doctorId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/appointment/status/{status}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentByStatus(@PathVariable String status){
        return ResponseEntity.ok(appointmentService.getAppointmentByStatus(status));
    }
}
