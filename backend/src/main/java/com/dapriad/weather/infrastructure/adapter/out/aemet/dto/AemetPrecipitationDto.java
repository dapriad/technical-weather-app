package com.dapriad.weather.infrastructure.adapter.out.aemet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AemetPrecipitationDto(Integer value, String periodo) {}