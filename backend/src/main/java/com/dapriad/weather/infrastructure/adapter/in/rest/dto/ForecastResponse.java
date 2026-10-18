package com.dapriad.weather.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.dapriad.weather.domain.model.MunicipalityForecast;

@Schema(description = "Daily forecast for a municipality")
public record ForecastResponse(
        @Schema(description = "Average temperature rounded to one decimal", example = "22.8")
        BigDecimal averageTemperature,
        @Schema(description = "Temperature unit used in the response", allowableValues = {"G_CEL", "G_FAH"}, example = "G_CEL")
        String temperatureUnit,
        @Schema(description = "Six-hour precipitation probability breakdown")
        List<PrecipitationProbabilityResponse> precipitationProbability) {

    public static ForecastResponse from(MunicipalityForecast forecast) {
        return new ForecastResponse(
                forecast.averageTemperature(),
                forecast.temperatureUnit().name(),
                forecast.precipitationProbability().stream()
                        .map(p -> new PrecipitationProbabilityResponse(p.probability(), p.period()))
                        .toList()
        );
    }
}