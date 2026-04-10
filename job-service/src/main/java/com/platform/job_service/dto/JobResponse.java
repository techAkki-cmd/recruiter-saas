package com.platform.job_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private List<String> skillsRequired;
    private Integer minExperience;
    private Integer maxExperience;
    private String location;
    private String recruiterEmail;
    private LocalDateTime createdAt;
}