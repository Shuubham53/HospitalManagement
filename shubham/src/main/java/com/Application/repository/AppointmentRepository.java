package com.Application.repository;

import com.Application.entity.Appointment;
import com.Application.entity.type.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {


    List<Appointment> findAllByPatientId(Long id);

    List<Appointment> findByDoctorId(Long id);

    List<Appointment> findByStatus(AppointmentStatus status);
}
