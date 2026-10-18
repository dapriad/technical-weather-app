package com.dapriad.weather.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Precipitation probability for a six-hour period")
public record PrecipitationProbabilityResponse(
        @Schema(description = "Probability percentage", example = "5")
        int probability,
        @Schema(description = "Six-hour period", example = "00-06")
        String period) {}