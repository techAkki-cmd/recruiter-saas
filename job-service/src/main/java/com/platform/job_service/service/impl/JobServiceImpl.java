package com.platform.job_service.service.impl;

import com.platform.job_service.dto.ApplicationDetailsResponse;
import com.platform.job_service.dto.JobRequest;
import com.platform.job_service.dto.JobResponse;
import com.platform.job_service.dto.ResumeProcessMessage;
import com.platform.job_service.entity.CandidateApplication;
import com.platform.job_service.entity.Job;
import com.platform.job_service.repository.CandidateApplicationRepository;
import com.platform.job_service.repository.JobRepository;
import com.platform.job_service.service.FileStorageService;
import com.platform.job_service.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // 🔥 Corrected Import
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CandidateApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;
    private final RabbitTemplate rabbitTemplate;
    private final VectorStore vectorStore;

    @Value("${rabbitmq.exchange.resume.name:resume_exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing.resume.key:resume_routing_key}")
    private String routingKey;

    @Override
    public JobResponse createJob(JobRequest request, String recruiterEmail) {

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .skillsRequired(request.getSkillsRequired())
                .minExperience(request.getMinExperience())
                .maxExperience(request.getMaxExperience())
                .location(request.getLocation())
                .recruiterEmail(recruiterEmail)
                .build();

        Job savedJob = jobRepository.save(job);

        return JobResponse.builder()
                .id(savedJob.getId())
                .title(savedJob.getTitle())
                .description(savedJob.getDescription())
                .skillsRequired(savedJob.getSkillsRequired())
                .minExperience(savedJob.getMinExperience())
                .maxExperience(savedJob.getMaxExperience())
                .location(savedJob.getLocation())
                .recruiterEmail(savedJob.getRecruiterEmail())
                .createdAt(savedJob.getCreatedAt())
                .build();
    }

    @Override
    public String uploadResume(Long jobId, MultipartFile file, String candidateName, String candidateEmail, String recruiterEmail) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        if (!job.getRecruiterEmail().equals(recruiterEmail)) {
            throw new IllegalStateException("Unauthorized: You can only upload resumes to your own job postings.");
        }

        try {
            // 1. Upload to S3 - This is now our single source of truth
            String s3Url = fileStorageService.uploadFileToS3(file, candidateName);

            // 2. Save the initial application as "PROCESSING"
            CandidateApplication application = CandidateApplication.builder()
                    .job(job)
                    .candidateName(candidateName)
                    .candidateEmail(candidateEmail)
                    .resumeText("Processing in background...")
                    .resumeS3Url(s3Url)
                    .status("PROCESSING")
                    .build();

            CandidateApplication savedApplication = applicationRepository.save(application);

            // 3. Send the message to RabbitMQ using the S3 URL!
            ResumeProcessMessage message = new ResumeProcessMessage(savedApplication.getId(), s3Url);

            // 🔥 Corrected to use the injected dynamic properties
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);

            log.info("Resume queued for processing via S3 URL. Candidate: {}, Application ID: {}", candidateName, savedApplication.getId());
            return "Resume accepted for background processing! Candidate: " + candidateName;

        } catch (Exception e) {
            log.error("Unexpected error uploading resume for candidate: {}", candidateName, e);
            throw new RuntimeException("Failed to upload resume: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> bulkUploadResumes(Long jobId, List<MultipartFile> files, List<String> names, List<String> emails, String recruiterEmail) {
        List<String> responses = new ArrayList<>();

        // Loop through all uploaded files and reuse your existing logic!
        for (int i = 0; i < files.size(); i++) {
            try {
                String response = uploadResume(jobId, files.get(i), names.get(i), emails.get(i), recruiterEmail);
                responses.add("File " + (i+1) + " (" + files.get(i).getOriginalFilename() + "): " + response);
            } catch (Exception e) {
                log.error("Failed to queue file: {}", files.get(i).getOriginalFilename(), e);
                responses.add("File " + (i+1) + " FAILED: " + e.getMessage());
            }
        }

        return responses;
    }

    @Override
    public ApplicationDetailsResponse getApplicationDetails(Long applicationId, String recruiterEmail) {
        CandidateApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getJob().getRecruiterEmail().equals(recruiterEmail)) {
            throw new IllegalStateException("Unauthorized: You do not own this application.");
        }

        return new ApplicationDetailsResponse(
                application.getId(),
                application.getCandidateName(),
                application.getCandidateEmail(),
                application.getStatus(),
                application.getAiMatchScore(),
                application.getAiSummary(),
                application.getResumeS3Url()
        );
    }

    @Override
    public List<Document> searchResumes(String query, String candidateName) {
        log.info("Performing Semantic AI Search for query: {} | Name Filter: {}", query, candidateName);

        SearchRequest.Builder requestBuilder = SearchRequest.builder()
                .query(query)
                .topK(5); // Still returns top 5 best matches

        // If the recruiter provided a name, filter by the metadata we saved earlier!
        if (candidateName != null && !candidateName.isEmpty()) {
            requestBuilder.filterExpression("candidateName == '" + candidateName + "'");
        }

        return vectorStore.similaritySearch(requestBuilder.build());
    }
}