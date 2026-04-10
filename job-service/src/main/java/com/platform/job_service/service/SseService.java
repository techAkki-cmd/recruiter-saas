package com.platform.job_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {

    // Thread-safe map to hold open connections for each recruiter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String recruiterEmail) {
        // Create an emitter with a 30-minute timeout
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.put(recruiterEmail, emitter);

        // Remove the emitter when the frontend closes the tab or the connection drops
        emitter.onCompletion(() -> emitters.remove(recruiterEmail));
        emitter.onTimeout(() -> emitters.remove(recruiterEmail));
        emitter.onError((e) -> emitters.remove(recruiterEmail));

        try {
            // Send a dummy event to establish the connection
            emitter.send(SseEmitter.event().name("INIT").data("Connected successfully"));
        } catch (IOException e) {
            emitters.remove(recruiterEmail);
        }

        return emitter;
    }

    public void notifyRecruiter(String recruiterEmail, Long applicationId, String status) {
        SseEmitter emitter = emitters.get(recruiterEmail);
        if (emitter != null) {
            try {
                // Push a JSON-like string (or an actual DTO) to the frontend
                String message = String.format("{\"applicationId\": %d, \"status\": \"%s\"}", applicationId, status);
                emitter.send(SseEmitter.event().name("AI_UPDATE").data(message));
            } catch (IOException e) {
                emitters.remove(recruiterEmail);
            }
        }
    }
}