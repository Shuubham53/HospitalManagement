package com.Application.repository;

import com.Application.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient,Long> {

    List<Patient> findByUser_ActiveTrue();
    Optional<Patient> findByIdAndUser_ActiveTrue(Long id);
}
