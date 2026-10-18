package com.dapriad.weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI technicalWeatherOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Technical Weather API")
				.version("0.0.1-SNAPSHOT")
				.description("Technical test API scaffold exposing status and greeting endpoints."));
	}
}
