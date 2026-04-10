package com.platform.job_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jobServiceOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                // 🔥 Routes traffic through the Gateway
                .addServersItem(new Server().url("http://localhost:8080").description("API Gateway"))
                .info(new Info()
                        .title("Job Service API") // <-- DIFFERENT TITLE
                        .version("1.0")
                        .description("API documentation for Resume Processing and AI Search.")) // <-- DIFFERENT DESCRIPTION
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}