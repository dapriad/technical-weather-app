package com.dapriad.weather.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Municipality data returned by the AEMET-backed search endpoint")
public record MunicipalityResponse(
        @Schema(description = "Municipality code (preserves leading zeros)", example = "28079")
        String id,
        @Schema(description = "Municipality name in Spanish", example = "Madrid")
        String name) {}