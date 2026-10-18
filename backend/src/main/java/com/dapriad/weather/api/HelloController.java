package com.dapriad.weather.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}

	@GetMapping("/hello")
	public Map<String, String> hello() {
		return Map.of("message", "Hello from Spring Boot");
	}
}
