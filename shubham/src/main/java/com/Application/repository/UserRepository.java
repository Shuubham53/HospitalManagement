package com.Application.repository;

import com.Application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
     Optional<User>  findByUsername(String email);
    boolean existsByUsername(String email);
}
