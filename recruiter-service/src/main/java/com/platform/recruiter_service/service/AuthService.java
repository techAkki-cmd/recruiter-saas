package com.platform.recruiter_service.service;

import com.platform.recruiter_service.dto.AuthRequest;
import com.platform.recruiter_service.dto.AuthResponse;
import com.platform.recruiter_service.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
}