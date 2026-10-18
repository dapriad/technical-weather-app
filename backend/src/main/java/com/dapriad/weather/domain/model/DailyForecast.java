package com.dapriad.weather.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyForecast(
        LocalDate date,
        BigDecimal averageTemperatureCelsius,
        List<PrecipitationProbability> precipitationProbability) {

    public DailyForecast {
        precipitationProbability = List.copyOf(precipitationProbability);
    }
}