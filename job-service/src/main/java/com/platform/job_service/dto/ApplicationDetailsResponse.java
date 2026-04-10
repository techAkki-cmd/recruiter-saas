package com.platform.job_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDetailsResponse {
    private Long id;
    private String candidateName;
    private String candidateEmail;
    private String status; // PROCESSING, COMPLETED, or FAILED
    private Integer aiMatchScore;
    private String aiSummary;
    private String resumeS3Url;
}