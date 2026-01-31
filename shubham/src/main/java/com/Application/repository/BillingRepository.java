package com.Application.repository;

import com.Application.entity.Appointment;
import com.Application.entity.Billing;
import com.Application.entity.type.AppointmentStatus;
import com.Application.entity.type.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillingRepository extends JpaRepository<Billing,Long> {
    Optional<Billing> findByReferenceNumber(String referenceNumber);
    boolean existsByAppointment(Appointment appointment);
    Optional<Billing> findByAppointment(Appointment appointment);
    List<Billing> findByPatientId(Long patientId);
    List<Billing> findByStatus(PaymentStatus status);
}
