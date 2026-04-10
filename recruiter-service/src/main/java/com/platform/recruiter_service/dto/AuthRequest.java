package com.platform.recruiter_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String email;
    private String password;
}