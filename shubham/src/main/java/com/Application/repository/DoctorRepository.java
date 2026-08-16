package com.Application.repository;

import com.Application.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository  extends JpaRepository<Doctor,Long> {
    List<Doctor> findByUser_ActiveTrue();
    List<Doctor> findBySpecializationAndUser_ActiveTrue(String name);
    Optional<Doctor> findByIdAndUser_ActiveTrue(Long id);
}
