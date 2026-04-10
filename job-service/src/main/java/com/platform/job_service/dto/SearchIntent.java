package com.platform.job_service.dto;


public record SearchIntent(
        String skillsAndKeywords,
        String exactCandidateName
) {}