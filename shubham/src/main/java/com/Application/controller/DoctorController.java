package com.Application.controller;

import com.Application.dto.*;
import com.Application.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;
    @PostMapping("/doctor/register")
    public ResponseEntity<DoctorResponse> createDoctor(@RequestBody DoctorRequest request){
        DoctorResponse response = doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctor(){
        List<DoctorResponse> responses = doctorService.getAllDoctor();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/doctor")
    public ResponseEntity<DoctorResponse> getCurrentDoctorProfile(){
        DoctorResponse responses = doctorService.getCurrentDoctorProfile();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/doctor")
    public ResponseEntity<DoctorResponse> updateDoctor(@RequestBody DoctorRequest request){
        DoctorResponse response = doctorService.updateDoctor(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/doctor/{doctorId}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long doctorId) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/doctor/appointments")
    public ResponseEntity<List<AppointmentDoctorResponse>> getMyAppointments(){
        List<AppointmentDoctorResponse> responses = doctorService.getMyAppointments();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/doctor/specialization/{name}")
    public ResponseEntity<List<DoctorResponse>> getDoctorsBySpecialization(@PathVariable String name){
        List<DoctorResponse> responses = doctorService.getDoctorBySpecialization(name);
        return ResponseEntity.ok(responses);
    }
}
