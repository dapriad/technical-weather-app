package com.dapriad.weather.infrastructure.adapter.out.aemet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AemetMunicipalityDto(String id, String nombre, String provincia) {}