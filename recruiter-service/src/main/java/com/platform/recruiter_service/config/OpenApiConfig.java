package com.platform.recruiter_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server; // 🔥 ADD THIS IMPORT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recruiterServiceOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                // 🔥 ADD THIS LINE: Forces Swagger to route traffic through the Gateway
                .addServersItem(new Server().url("http://localhost:8080").description("API Gateway"))
                .info(new Info()
                        .title("Recruiter Auth API")
                        .version("1.0")
                        .description("API documentation for Recruiter Registration and Login."))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}