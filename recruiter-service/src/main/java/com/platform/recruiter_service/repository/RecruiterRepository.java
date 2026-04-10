package com.platform.recruiter_service.repository;

import com.platform.recruiter_service.entity.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {

    // Spring Data JPA magically writes the SQL for this based on the method name
    Optional<Recruiter> findByEmail(String email);

    // Useful for checking if an account already exists during registration
    Boolean existsByEmail(String email);
}