package com.dapriad.weather.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Municipality data returned by the search endpoint")
public record MunicipalityResponse(String id, String name) {}