package com.platform.job_service.controller;

import com.platform.job_service.dto.ApplicationDetailsResponse;
import com.platform.job_service.dto.JobRequest;
import com.platform.job_service.dto.JobResponse;
import com.platform.job_service.dto.SearchIntent;
import com.platform.job_service.service.JobService;
import com.platform.job_service.service.RecruiterAgentService;
import com.platform.job_service.service.SseService;
import com.platform.job_service.service.impl.AiScoringServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.ai.document.Document;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final AiScoringServiceImpl aiScoringService;
    private final SseService sseService;
    private final RecruiterAgentService agentService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest request, Authentication authentication) {
        String recruiterEmail = (String) authentication.getPrincipal();
        return ResponseEntity.ok(jobService.createJob(request, recruiterEmail));
    }

    @PostMapping(value = "/{jobId}/resumes/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Candidate Resume", description = "Uploads a PDF resume for a specific job, extracts data via AI, and links it to the recruiter.")
    public ResponseEntity<String> uploadResume(
            @Parameter(description = "The ID of the job posting") @PathVariable Long jobId,
            @Parameter(description = "Full name of the candidate") @RequestParam("name") String candidateName,
            @Parameter(description = "Email address of the candidate") @RequestParam("email") String candidateEmail,
            @Parameter(description = "Candidate's resume (PDF format)") @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) Authentication authentication) {

        String recruiterEmail = (String) authentication.getPrincipal();

        return ResponseEntity.ok(jobService.uploadResume(jobId, file, candidateName, candidateEmail, recruiterEmail));
    }

    @PostMapping(value = "/{jobId}/resumes/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk Upload Candidate Resumes", description = "Uploads multiple PDF/Image resumes for a specific job, extracting data via AI for each.")
    public ResponseEntity<?> bulkUploadResumes(
            @Parameter(description = "The ID of the job posting") @PathVariable Long jobId,
            @Parameter(description = "List of candidate names (in same order as files)") @RequestParam("names") List<String> candidateNames,
            @Parameter(description = "List of candidate emails (in same order as files)") @RequestParam("emails") List<String> candidateEmails,

            @Parameter(description = "Candidate resumes (Select multiple files)")
            @RequestPart("files") List<MultipartFile> files,

            @Parameter(hidden = true) Authentication authentication) {

        if (files.size() != candidateNames.size() || files.size() != candidateEmails.size()) {
            return ResponseEntity.badRequest().body("Error: The number of files, names, and emails must exactly match.");
        }

        String recruiterEmail = (String) authentication.getPrincipal();

        List<String> responses = jobService.bulkUploadResumes(jobId, files, candidateNames, candidateEmails, recruiterEmail);

        return ResponseEntity.ok(Map.of(
                "status", "Success",
                "message", "Successfully queued " + files.size() + " resumes for background processing!",
                "details", responses
        ));
    }



    @PostMapping("/applications/{applicationId}/analyze")
    public ResponseEntity<com.platform.job_service.dto.AiScoreResponse> analyzeApplication(
            @PathVariable Long applicationId,
            Authentication authentication) {

        String recruiterEmail = (String) authentication.getPrincipal();
        return ResponseEntity.ok(aiScoringService.analyzeResume(applicationId, recruiterEmail));
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApplicationDetailsResponse> getApplicationDetails(
            @PathVariable Long applicationId,
            Authentication authentication) {

        String recruiterEmail = (String) authentication.getPrincipal();

        ApplicationDetailsResponse response = jobService.getApplicationDetails(applicationId, recruiterEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/notifications/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(org.springframework.security.core.Authentication authentication) {
        String recruiterEmail = (String) authentication.getPrincipal();
        return sseService.subscribe(recruiterEmail);
    }

    @GetMapping("/search-resumes")
    public ResponseEntity<List<Document>> searchResumes(
            @RequestParam String query,
            @RequestParam(required = false) String candidateName) {

        List<Document> results = jobService.searchResumes(query, candidateName);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/smart-search")
    public ResponseEntity<?> smartSearch(@RequestParam String prompt) {

        SearchIntent intent = agentService.extractSearchIntent(prompt);

        List<Document> results = jobService.searchResumes(
                intent.skillsAndKeywords(),
                intent.exactCandidateName()
        );

        String aiAnswer = agentService.synthesizeAnswer(prompt, results);

        return ResponseEntity.ok(Map.of(
                "aiUnderstanding", intent,
                "aiSummary", aiAnswer,
                "matchingResumes", results
        ));
    }
}