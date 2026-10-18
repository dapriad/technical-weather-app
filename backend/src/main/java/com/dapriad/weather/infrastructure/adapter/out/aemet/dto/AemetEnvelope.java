package com.dapriad.weather.infrastructure.adapter.out.aemet.dto;

public record AemetEnvelope(String estado, String descripcion, String datos, String metadatos) {}