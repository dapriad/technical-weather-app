package com.dapriad.weather.infrastructure.adapter.out.aemet.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AemetPredictionDto(@JsonProperty("dia") List<AemetForecastDayDto> days) {}