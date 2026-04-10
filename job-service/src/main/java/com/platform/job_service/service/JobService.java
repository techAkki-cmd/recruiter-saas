package com.platform.job_service.service;

import com.platform.job_service.dto.ApplicationDetailsResponse;
import com.platform.job_service.dto.JobRequest;
import com.platform.job_service.dto.JobResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import java.util.List;

public interface JobService {
    JobResponse createJob(JobRequest request, String recruiterEmail);
    String uploadResume(Long jobId, MultipartFile file, String candidateName, String candidateEmail, String recruiterEmail);
    ApplicationDetailsResponse getApplicationDetails(Long applicationId, String recruiterEmail);
    List<Document> searchResumes(String query, String candidateName);
    List<String> bulkUploadResumes(Long jobId, List<MultipartFile> files, List<String> names, List<String> emails, String recruiterEmail);
}