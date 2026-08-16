package com.Application.service;

import com.Application.dto.AppointmentPatientResponse;
import com.Application.dto.PatientRequest;
import com.Application.dto.PatientResponse;
import com.Application.entity.Appointment;
import com.Application.entity.Patient;
import com.Application.entity.User;
import com.Application.entity.type.Role;
import com.Application.error.BusinessRuleViolationException;
import com.Application.error.PatientNotFoundException;
import com.Application.repository.AppointmentRepository;
import com.Application.repository.PatientRepository;
import com.Application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        if (userRepository.existsByUsername(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = User.builder()
                .username(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PATIENT)
                .build();
        userRepository.save(user);

        Patient patient = toEntity(request,user);
        patientRepository.save(patient);

        return toDto(patient);
    }

    public PatientResponse getPatientById(Long patientId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentLoggedUser = (User) authentication.getPrincipal();

       if(currentLoggedUser.getRole() != Role.ADMIN){

           if(currentLoggedUser.getRole() !=  Role.PATIENT || currentLoggedUser.getPatient() == null || !currentLoggedUser.getPatient().getId().equals(patientId)){
               throw new BusinessRuleViolationException
                       ("Only patient with id "+patientId+" can access");
           }
       }
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: "+patientId));
        return toDto(patient);

    }
    @Cacheable(value = "patients")
    public List<PatientResponse> getAllPatient() {
        User currentLoggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(currentLoggedUser.getRole() != Role.ADMIN){
            throw new BusinessRuleViolationException("Unauthorize.. only admin can access");
        }
        log.info("fetching all patients from database");
        List<Patient> patients = patientRepository.findByUser_ActiveTrue();
        List<PatientResponse> responses = patients.stream()
                .map(this::toDto).toList();
        return responses;
    }

    public PatientResponse getCurrentPatient() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(currentUser.getRole() != Role.PATIENT){
            throw new BusinessRuleViolationException("Unauthorize.. only patient can access");
        }
        Patient patient = currentUser.getPatient();
        if (patient == null) throw new PatientNotFoundException("Patient profile missing");
        PatientResponse response = toDto(patient);
        return response;
    }
    @Transactional
    public PatientResponse updatePatient(PatientRequest request) {
        User currentUser =  (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(currentUser.getRole() != Role.PATIENT){
            throw new BusinessRuleViolationException("Unauthorize.. only patient can update");
        }

        Patient patient = currentUser.getPatient();

        if(patient == null) throw new PatientNotFoundException("Patient profile missing to update ");

        patient.setAddress(request.getAddress());
        patient.setFirst_name(request.getFirst_name());
        patient.setLast_name(request.getLast_name());
        patient.setPhone(request.getPhone());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());

        if(request.getEmail() != null && !request.getEmail().equals(patient.getUser().getUsername())){
            if(userRepository.existsByUsername(request.getEmail())){
                throw new IllegalArgumentException("This Email is already in Use");
            }
            patient.setEmail(request.getEmail());
            patient.getUser().setUsername(request.getEmail());
        }
        patientRepository.save(patient);

        return toDto(patient);
    }

    @Transactional
    public void deletePatient(Long id) {
        User currentUser =  (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(currentUser.getRole() != Role.ADMIN){
            if(currentUser.getRole() != Role.PATIENT){
                throw new BusinessRuleViolationException("Unauthorize.. you don't have access to delete patient");
            }
            if(currentUser.getPatient() == null || !currentUser.getPatient().getId().equals(id)){
                throw new BusinessRuleViolationException("Only patient with id "+id+" can delete ");
            }
        }

        Patient patient = patientRepository.findById(id).orElseThrow(() ->
                new PatientNotFoundException("Patient Not found for deletion"));
        User user = patient.getUser();
        user.setActive(false);
        userRepository.save(user);
    }

    public List<AppointmentPatientResponse> getMyAppointments() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long patientId = currentUser.getPatient().getId();
        List<Appointment> appointments =
                appointmentRepository.findAllByPatientId(patientId);
        List<AppointmentPatientResponse> responses = appointments.stream()
                .map(this::toAppointmentDto)
                .toList();
        return responses;
    }

    public AppointmentPatientResponse toAppointmentDto(Appointment appointment){
       AppointmentPatientResponse response = AppointmentPatientResponse.builder()
               .id(appointment.getId())
               .appointmentDate(appointment.getAppointment_date())
               .doctorSpecialization(appointment.getDoctor().getSpecialization())
               .doctorId(appointment.getDoctor().getId())
               .reason(appointment.getReason())
               .prescription(appointment.getPrescription())
               .doctorName(appointment.getDoctor().getFirst_name()+" "+appointment.getDoctor().getLast_name())
               .status(appointment.getStatus())
               .build();
       return response;
    }

    public Patient toEntity(PatientRequest request,User user){
        return Patient.builder()
                .first_name(request.getFirst_name())
                .last_name(request.getLast_name())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .address(request.getAddress())
                .phone(request.getPhone())
                .user(user)
                .email(request.getEmail())
                .build();
    }
    public PatientResponse toDto(Patient patient){
        return PatientResponse.builder()
                .id(patient.getId())
                .first_name(patient.getFirst_name())
                .last_name(patient.getLast_name())
                .email(patient.getEmail())
                .birthDate(patient.getBirthDate())
                .address(patient.getAddress())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .build();
    }



}
