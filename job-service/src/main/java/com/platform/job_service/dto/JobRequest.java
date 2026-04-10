package com.platform.job_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRequest {
    private String title;
    private String description;
    private List<String> skillsRequired;
    private Integer minExperience;
    private Integer maxExperience;
    private String location;
}