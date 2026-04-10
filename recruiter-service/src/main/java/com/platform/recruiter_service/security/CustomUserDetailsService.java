package com.platform.recruiter_service.security;

import com.platform.recruiter_service.entity.Recruiter;
import com.platform.recruiter_service.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final RecruiterRepository recruiterRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Recruiter recruiter = recruiterRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Recruiter Not Found with email: " + email));

        return new User(
                recruiter.getEmail(),
                recruiter.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(recruiter.getRole()))
        );
    }
}