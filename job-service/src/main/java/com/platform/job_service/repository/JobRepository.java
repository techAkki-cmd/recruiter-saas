package com.platform.job_service.repository;

import com.platform.job_service.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Find all jobs posted by a specific recruiter
    List<Job> findByRecruiterEmail(String email);
}