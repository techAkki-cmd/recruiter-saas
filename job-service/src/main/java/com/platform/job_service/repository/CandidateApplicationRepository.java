package com.platform.job_service.repository;

import com.platform.job_service.entity.CandidateApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, Long> {
    List<CandidateApplication> findByJobId(Long jobId);
}