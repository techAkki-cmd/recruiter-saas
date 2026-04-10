package com.platform.job_service.dto;

import java.util.List;

public record AiScoreResponse(
        Integer matchScore,
        String summary,
        List<String> matchedSkills,
        List<String> missingSkills
) {}