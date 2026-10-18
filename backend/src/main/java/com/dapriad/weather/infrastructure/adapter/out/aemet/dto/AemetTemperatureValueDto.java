package com.dapriad.weather.infrastructure.adapter.out.aemet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AemetTemperatureValueDto(Integer value, Integer hora) {}