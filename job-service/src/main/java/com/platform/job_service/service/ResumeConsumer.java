package com.platform.job_service.service;

import com.platform.job_service.dto.AiScoreResponse;
import com.platform.job_service.dto.ResumeProcessMessage;
import com.platform.job_service.entity.CandidateApplication;
import com.platform.job_service.entity.Job;
import com.platform.job_service.repository.CandidateApplicationRepository;
import com.platform.job_service.service.impl.AiScoringServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeConsumer {

    private final CandidateApplicationRepository applicationRepository;
    private final OcrService ocrService;
    private final AiScoringServiceImpl aiScoringService; // 🔥 Changed to Interface
    private final SseService sseService;
    private final VectorStore vectorStore;
    private final S3Client s3Client; // 🔥 Inject S3 to download the file

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    // Listen to the queue dynamically from properties
    @RabbitListener(queues = "${rabbitmq.queue.resume.name:resume_processing_queue}")
    @Transactional
    public void processResume(ResumeProcessMessage message) {
        log.info("⬇️ WORKER THREAD: Picked up task for Application ID: {}", message.getApplicationId());

        CandidateApplication application = applicationRepository.findById(message.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        Job job = application.getJob();
        File tempFile = null;

        try {
            // 1. 🔥 DOWNLOAD FROM S3
            log.info("☁️ WORKER THREAD: Downloading resume from S3...");
            String s3Url = message.getS3Url();
            String fileName = s3Url.substring(s3Url.lastIndexOf("/") + 1);

            // 🔥 FIX: Dynamically grab the file extension instead of hardcoding .pdf
            String extension = ".pdf"; // Default fallback
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex); // e.g., ".jpg" or ".png"
            }

            // Create temp file with the CORRECT extension
            tempFile = File.createTempFile("worker-resume-", extension);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            byte[] s3FileData = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
            Files.write(tempFile.toPath(), s3FileData);

            // 2. EXTRACT TEXT
            String extractedText;
            try (FileInputStream fis = new FileInputStream(tempFile)) {
                Tika tika = new Tika();
                extractedText = tika.parseToString(fis).trim();

                if (extractedText.length() < 50) {
                    log.warn("⚠️ WORKER THREAD: Tika failed to extract meaningful text. Engaging Local OCR...");
                    extractedText = ocrService.extractTextLocally(tempFile);
                }
            }

            // 3. AI SCORING
            log.info("🧠 WORKER THREAD: Sending extracted text to Gemini...");
            AiScoreResponse aiResult = aiScoringService.scoreRawText(job, extractedText);

            // 4. UPDATE DATABASE
            application.setResumeText(extractedText);
            application.setAiMatchScore(aiResult.matchScore());
            application.setAiSummary(aiResult.summary());
            application.setStatus("COMPLETED");
            applicationRepository.save(application);

            log.info("✅ WORKER THREAD: Successfully parsed and scored Application ID: {}", message.getApplicationId());

            // 5. RAG INGESTION
            try {
                log.info("🪓 WORKER THREAD: Chunking text and generating embeddings...");

                TokenTextSplitter splitter = new TokenTextSplitter();
                Document doc = new Document(
                        extractedText,
                        Map.of(
                                "applicationId", application.getId(),
                                "candidateName", application.getCandidateName(),
                                "jobTitle", job.getTitle()
                        )
                );

                List<Document> chunks = splitter.apply(List.of(doc));
                vectorStore.add(chunks);

                // Warning: In Docker, this local file is lost on container restart!
                if (vectorStore instanceof SimpleVectorStore simpleStore) {
                    simpleStore.save(new File("local_resume_vectors.json"));
                }

                log.info("🧠 WORKER THREAD: Resume embedded and saved to Vector Database!");

            } catch (Exception e) {
                log.error("⚠️ WORKER THREAD: Failed to create embeddings, but application was saved.", e);
            }

            // 6. NOTIFY FRONTEND
            sseService.notifyRecruiter(job.getRecruiterEmail(), application.getId(), "COMPLETED");

        } catch (Exception e) {
            log.error("❌ WORKER THREAD: Failed to process resume for App ID: {}", message.getApplicationId(), e);
            application.setStatus("FAILED");
            applicationRepository.save(application);
            sseService.notifyRecruiter(job.getRecruiterEmail(), application.getId(), "FAILED");
        } finally {
            // Always clean up the local S3 download!
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (Exception e) {
                    log.warn("⚠️ WORKER THREAD: Failed to delete temp file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
}