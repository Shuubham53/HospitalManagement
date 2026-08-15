package com.Application.service;

import com.Application.dto.*;
import com.Application.entity.Appointment;
import com.Application.entity.Doctor;
import com.Application.entity.Patient;
import com.Application.entity.User;
import com.Application.entity.type.AppointmentStatus;
import com.Application.entity.type.Role;
import com.Application.error.AppointmentNotFoundException;
import com.Application.error.BusinessRuleViolationException;
import com.Application.error.DoctorNotFoundException;
import com.Application.error.PatientNotFoundException;
import com.Application.repository.AppointmentRepository;
import com.Application.repository.DoctorRepository;
import com.Application.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final int  DEFAULT_APPOINTMENT_MINUTES = 30;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final BillingService billingService;

    @Value("${hospital.open.time}")
    private LocalTime hospitalOpenTime;
    @Value("${hospital.close.time}")
    private LocalTime hospitalCloseTime;


    @Transactional
    public AppointmentResponse bookApplication(AppointmentRequest request) {



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User curentLoggedUser = (User)authentication.getPrincipal();
        Patient patient = curentLoggedUser.getPatient();


        if(patient == null){
            throw new PatientNotFoundException("Patient not found for current user");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId()).orElseThrow(()->
                new DoctorNotFoundException("Doctor Not Found with Id "+request.getDoctorId()+" for Booking appointment"));

        LocalDateTime startTime = request.getAppointment_date();
        LocalDateTime endTime = request.getAppointment_date().plusMinutes(DEFAULT_APPOINTMENT_MINUTES);

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(
                    "Cannot book an appointment in the past"
            );
        }

        if(startTime.toLocalTime().isBefore(hospitalOpenTime) || endTime.toLocalTime().isAfter(hospitalCloseTime)){
            throw new BusinessRuleViolationException("You cannot book appointment in this hours");
        }

        boolean exists = appointmentRepository.existsOverlappingAppointment(
                doctor.getId(),
                AppointmentStatus.CANCELLED,
                startTime,
                endTime
        );

        if (exists) {
            throw new BusinessRuleViolationException(
                    "Doctor is not available at the requested time"
            );
        }

        Appointment appointment = Appointment.builder()
                .appointment_date(request.getAppointment_date())
                .status(AppointmentStatus.SCHEDULED)
                .reason(request.getReason())
                .prescription(null)
                .patient(patient)
                .doctor(doctor)
                .startTime(startTime)
                .endTime(endTime)
                .appointmentType(request.getAppointmentType())
                .build();

        appointmentRepository.save(appointment);

        return mapToAppointmentResponse(appointment);
    }

    public AppointmentResponse confirmAppointment(Long appointmentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentLoggedUser = (User) authentication.getPrincipal();


        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(()->
                new AppointmentNotFoundException("Appointment Not found with id "+appointmentId+ " to Confirm the Appointment"));
        Long doctorId = currentLoggedUser.getDoctor().getId();

        if(!doctorId.equals(appointment.getDoctor().getId())){
            throw new BusinessRuleViolationException("Unauthorized .. only doctor with id "+appointment.getDoctor().getId()+" can confirm appointment");
        }
        if(appointment.getStatus() != AppointmentStatus.SCHEDULED){
            throw new BusinessRuleViolationException("Only SCHEDULED appointment can be CONFIRMED but this is "+appointment.getStatus());
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
        return mapToAppointmentResponse(appointment);
    }
    @Transactional
    public AppointmentResponse cancelAppointment(CancellationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User curentLoggedUser = (User)authentication.getPrincipal();

        Long currentUserId = curentLoggedUser.getId();

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId()).orElseThrow(() ->
                new AppointmentNotFoundException("Appointment not found with id "+request.getAppointmentId()+" to Cancel appointment"));

        boolean isPatient = curentLoggedUser.getRole() == Role.PATIENT && appointment.getPatient().getUser().getId().equals(currentUserId);
        boolean isDoctor = curentLoggedUser.getRole() == Role.DOCTOR && appointment.getDoctor().getUser().getId().equals(currentUserId);

        if(!isPatient && !isDoctor){
            throw new BusinessRuleViolationException("Unauthorized: you cannot cancel this appointment");
        }
        if(appointment.getStatus() == AppointmentStatus.COMPLETED){
            throw new BusinessRuleViolationException("cannot cancel complete appointment");
        }
        if(appointment.getStatus() == AppointmentStatus.CANCELLED){
            throw new BusinessRuleViolationException("Appointment has been already Cancelled");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime appointmentTime = appointment.getAppointment_date();
        long hoursUntilAppointment = ChronoUnit.HOURS.between(now,appointmentTime);
        if(hoursUntilAppointment < 24){
            throw new BusinessRuleViolationException("Cannot cancel Appointment within 24 hours");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        String cancelledBy = isPatient ? "Patient" : "Doctor";
        log.info("Appointment is Cancelled by {}",cancelledBy);
        appointmentRepository.save(appointment);
        return mapToAppointmentResponse(appointment);

    }
    @Transactional
    public AppointmentResponse completeAppointment(CompleteRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User curentLoggedUser = (User) authentication.getPrincipal();

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId()).orElseThrow(() ->
                new AppointmentNotFoundException("Appointment not found with id "+request.getAppointmentId()));
        if(!curentLoggedUser.getDoctor().getId().equals(appointment.getDoctor().getId())){
            throw new BusinessRuleViolationException("Unauthorized ... you cant complete appointment");
        }


        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessRuleViolationException(
                    "Can only complete CONFIRMED appointments. Current status: " + appointment.getStatus()
            );
        }
        if(LocalDateTime.now().isBefore(appointment.getEndTime())){
            throw new BusinessRuleViolationException( "Cannot complete appointment before it ends");
        }

        appointment.setPrescription(request.getPrescription());
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        billingService.generateBill(appointment);
        return mapToAppointmentResponse(appointment);
    }

    public AppointmentResponse mapToAppointmentResponse(Appointment appointment){
        long duration = Duration.between(appointment.getStartTime(),appointment.getEndTime()).toMinutes();
         return  AppointmentResponse.builder()
                 .id(appointment.getId())
                 .reason(appointment.getReason())
                 .status(appointment.getStatus())
                 .prescription(appointment.getPrescription())
                 .patientId(appointment.getPatient().getId())
                 .doctorId(appointment.getDoctor().getId())
                 .prescription(appointment.getPrescription())
                 .appointment_date(appointment.getAppointment_date())
                 .appointmentType(appointment.getAppointmentType())
                 .durationTime(duration)
                 .build();

    }


    public List<AppointmentResponse> getAllAppointments() {
        List<Appointment>appointments = appointmentRepository.findAll();
        return appointments.stream()
                .map(this::mapToAppointmentResponse).toList();
    }

    public List<AppointmentResponse> getAppointmentsByPatientId(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findAllByPatientId(patientId);
        return appointments.stream().map(this::mapToAppointmentResponse).toList();
    }

    public AppointmentResponse getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() ->
                new AppointmentNotFoundException("Appointment not found with given id "+appointmentId));
        return mapToAppointmentResponse(appointment);
    }

    public List<AppointmentResponse> getAllAppointmentByDoctorId(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        return appointments.stream().map(this::mapToAppointmentResponse).toList();
    }

    public List<AppointmentResponse> getAppointmentByStatus(String status) {
        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
        List<Appointment> appointments = appointmentRepository.findByStatus(appointmentStatus);
        return appointments.stream().map(this::mapToAppointmentResponse).toList();
    }

}
