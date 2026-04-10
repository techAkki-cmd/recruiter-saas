package com.platform.job_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String headerAuth = request.getHeader("Authorization");
        System.out.println("\n--- INCOMING REQUEST TO JOB SERVICE ---");
        System.out.println("HEADER AUTH: " + headerAuth);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7).trim();
            System.out.println("EXTRACTED TOKEN: " + token);

            try {
                Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
                String email = Jwts.parserBuilder().setSigningKey(key).build()
                        .parseClaimsJws(token).getBody().getSubject();

                System.out.println("SUCCESS! TOKEN VALID. USER EMAIL: " + email);

                // 1. Give the user a dummy role to satisfy strict security managers
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null,
                                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_RECRUITER")));

                // 2. Use the modern Spring 6 Context Setting approach
                org.springframework.security.core.context.SecurityContext context =
                        SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

            } catch (Exception e) {
                System.out.println("!!! JWT VALIDATION FAILED !!!");
                System.out.println("ERROR MESSAGE: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("NO VALID BEARER TOKEN FOUND IN REQUEST!");
        }

        System.out.println("---------------------------------------\n");
        filterChain.doFilter(request, response);
    }
}