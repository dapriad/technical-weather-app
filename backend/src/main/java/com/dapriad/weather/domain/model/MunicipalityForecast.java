package com.dapriad.weather.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record MunicipalityForecast(
        BigDecimal averageTemperature,
        TemperatureUnit temperatureUnit,
        List<PrecipitationProbability> precipitationProbability) {

    public MunicipalityForecast {
        precipitationProbability = List.copyOf(precipitationProbability);
    }
}