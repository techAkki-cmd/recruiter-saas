package com.platform.recruiter_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication
@EnableDiscoveryClient

@OpenAPIDefinition(servers = {@Server(url = "http://13.61.182.103", description = "AWS Gateway")})
public class RecruiterServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecruiterServiceApplication.class, args);
	}

}
