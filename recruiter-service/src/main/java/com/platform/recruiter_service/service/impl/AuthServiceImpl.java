package com.platform.recruiter_service.service.impl;

import com.platform.recruiter_service.dto.AuthRequest;
import com.platform.recruiter_service.dto.AuthResponse;
import com.platform.recruiter_service.dto.RegisterRequest;
import com.platform.recruiter_service.entity.Recruiter;
import com.platform.recruiter_service.repository.RecruiterRepository;
import com.platform.recruiter_service.security.JwtUtils;
import com.platform.recruiter_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RecruiterRepository recruiterRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (recruiterRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new recruiter account and hash the password
        Recruiter recruiter = Recruiter.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .companyName(request.getCompanyName())
                .build();

        recruiterRepository.save(recruiter);

        // Auto-login after registration
        return login(new AuthRequest(request.getEmail(), request.getPassword()));
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        Recruiter recruiter = recruiterRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .token(jwt)
                .email(recruiter.getEmail())
                .fullName(recruiter.getFullName())
                .companyName(recruiter.getCompanyName())
                .build();
    }
}