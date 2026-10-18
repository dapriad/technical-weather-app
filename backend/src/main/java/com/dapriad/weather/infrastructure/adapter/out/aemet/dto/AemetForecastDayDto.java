package com.dapriad.weather.infrastructure.adapter.out.aemet.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AemetForecastDayDto(
        String fecha,
        AemetTemperatureDto temperatura,
        List<AemetPrecipitationDto> probPrecipitacion) {}