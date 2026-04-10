package com.platform.job_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeProcessMessage {
    private Long applicationId;
    private String s3Url;;
}