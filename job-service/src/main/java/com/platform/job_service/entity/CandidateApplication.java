package com.platform.job_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private String candidateName;

    @Column(nullable = false)
    private String candidateEmail;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String resumeText;

    @Column(name = "resume_s3_url")
    private String resumeS3Url;

    @Column(nullable = false)
    private String status = "PROCESSING";

    private Integer aiMatchScore;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime appliedAt;


}