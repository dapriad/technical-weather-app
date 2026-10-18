package com.dapriad.weather.domain.port;

import java.util.List;

import com.dapriad.weather.domain.model.DailyForecast;

public interface ForecastProvider {

    List<DailyForecast> findDailyForecasts(String municipalityId);
}