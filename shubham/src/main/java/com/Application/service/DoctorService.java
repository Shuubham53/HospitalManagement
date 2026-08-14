package com.Application.service;

import com.Application.dto.*;
import com.Application.entity.Appointment;
import com.Application.entity.Doctor;
import com.Application.entity.Patient;
import com.Application.entity.User;
import com.Application.entity.type.AppointmentStatus;
import com.Application.entity.type.Role;
import com.Application.error.DoctorNotFoundException;
import com.Application.error.EmailAlreadyExistException;
import com.Application.repository.AppointmentRepository;
import com.Application.repository.DoctorRepository;
import com.Application.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {

        if(userRepository.existsByUsername(request.getEmail())){
            throw new  EmailAlreadyExistException("User already exist with email: "+request.getEmail());
        }
        User user = User.builder()
                .username(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DOCTOR)
                .build();
        userRepository.save(user);

        Doctor doctor = mapToEntity(request,user);
        doctorRepository.save(doctor);
        return DoctorResponse.builder()
                .id(doctor.getId())
                .first_name(doctor.getFirst_name())
                .last_name(doctor.getLast_name())
                .schedule(doctor.getSchedule())
                .specialization(doctor.getSpecialization())
                .email(doctor.getEmail())
                .build();
    }
    public List<DoctorResponse> getAllDoctor() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<DoctorResponse> responses = doctors.stream()
                .map(this::mapToResponse).toList();
        return responses;
    }

    public DoctorResponse getCurrentDoctorProfile() {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Doctor doctor = currentUser.getDoctor();
        if(doctor == null){
            throw new DoctorNotFoundException("Doctor profile missing");
        }
        return mapToResponse(doctor);
    }
    @Transactional
    public DoctorResponse updateDoctor(DoctorRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Doctor doctor = currentUser.getDoctor();
        if(doctor == null){
            throw new DoctorNotFoundException("Doctor profile missing for updating");
        }
        doctor.setFirst_name(request.getFirst_name());
        doctor.setLast_name(request.getLast_name());
        doctor.setSchedule(request.getSchedule());
        doctor.setSpecialization(request.getSpecialization());

        if(request.getEmail() != null && !request.getEmail().equals(doctor.getEmail())){
           if(userRepository.existsByUsername(request.getEmail())){
               throw new IllegalArgumentException("This email is already in use");
           }
           doctor.setEmail(request.getEmail());
           doctor.getUser().setUsername(request.getEmail());
        }
        doctorRepository.save(doctor);
        return mapToResponse(doctor);
    }

    public void deleteDoctor() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Doctor doctor = currentUser.getDoctor();
        if(doctor == null){
            throw new DoctorNotFoundException("Doctor profile missing for Deletion");
        }
        User user = doctor.getUser();
        doctorRepository.delete(doctor);
        userRepository.delete(user);
    }

    public Doctor mapToEntity(DoctorRequest request,User user){
        Doctor doctor = Doctor.builder()
                .first_name(request.getFirst_name())
                .last_name(request.getLast_name())
                .schedule(request.getSchedule())
                .specialization(request.getSpecialization())
                .email(request.getEmail())
                .user(user)
                .build();
        return doctor;
    }
    public DoctorResponse mapToResponse(Doctor doctor){
        return DoctorResponse.builder()
                .id(doctor.getId())
                .email(doctor.getEmail())
                .first_name(doctor.getFirst_name())
                .last_name(doctor.getLast_name())
                .schedule(doctor.getSchedule())
                .specialization(doctor.getSpecialization())
                .build();
    }


    public List<AppointmentDoctorResponse> getMyAppointments() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Doctor doctor = currentUser.getDoctor();
        if(doctor == null){
            throw new DoctorNotFoundException("Doctor profile missing  cant track appointment history");
        }
        List<Appointment> appointment = appointmentRepository.findByDoctorId(doctor.getId());
        return appointment.stream().map(this::mapToAppointmentResponse).toList();
    }
    public List<DoctorResponse> getDoctorBySpecialization(String name) {

        List<Doctor> doctors = doctorRepository.findBySpecialization(name);
        List<DoctorResponse> responses = doctors.stream()
                .map(this::mapToResponse).toList();
        return responses;
    }

    public AppointmentDoctorResponse mapToAppointmentResponse(Appointment appointment){
        return AppointmentDoctorResponse.builder()
                .id(appointment.getId())
                .appointmentDate(appointment.getAppointment_date())
                .status(appointment.getStatus())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFirst_name()+" "+appointment.getPatient().getLast_name())
                .patientEmail(appointment.getPatient().getEmail())
                .patientPhone(appointment.getPatient().getPhone())
                .reason(appointment.getReason())
                .prescription(appointment.getPrescription())
                .build();
    }

}
