package com.dapriad.weather.infrastructure.adapter.in.rest;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Scaffold", description = "Health check and hello endpoints for the technical test scaffold")
public class HelloController {

	@GetMapping("/health")
	@Operation(summary = "Health check", description = "Returns the backend health status")
	@ApiResponse(responseCode = "200", description = "Service is up")
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}

	@GetMapping("/hello")
	@Operation(summary = "Hello", description = "Returns a greeting message from the Spring Boot backend")
	@ApiResponse(responseCode = "200", description = "Successful response")
	public Map<String, String> hello() {
		return Map.of("message", "Hello from Spring Boot");
	}
}