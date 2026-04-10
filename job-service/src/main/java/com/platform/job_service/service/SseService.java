package com.platform.job_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String recruiterEmail) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.put(recruiterEmail, emitter);

        emitter.onCompletion(() -> emitters.remove(recruiterEmail));
        emitter.onTimeout(() -> emitters.remove(recruiterEmail));
        emitter.onError((e) -> emitters.remove(recruiterEmail));

        try {
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
                String message = String.format("{\"applicationId\": %d, \"status\": \"%s\"}", applicationId, status);
                emitter.send(SseEmitter.event().name("AI_UPDATE").data(message));
            } catch (IOException e) {
                emitters.remove(recruiterEmail);
            }
        }
    }
}