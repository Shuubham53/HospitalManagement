package com.Application.repository;

import com.Application.entity.Appointment;
import com.Application.entity.type.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {


    List<Appointment> findAllByPatientId(Long id);

    List<Appointment> findByDoctorId(Long id);

    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("""
    SELECT COUNT(a) > 0
    FROM Appointment a
    WHERE a.doctor.id = :doctorId
      AND a.status <> :cancelledStatus
      AND a.startTime < :newEndTime
      AND a.endTime > :newStartTime
""")
    boolean existsOverlappingAppointment(
            @Param("doctorId") Long doctorId,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime
    );
}
