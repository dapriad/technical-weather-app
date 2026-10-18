package com.dapriad.weather.application.port.in;

import com.dapriad.weather.domain.model.MunicipalityForecast;
import com.dapriad.weather.domain.model.TemperatureUnit;

public interface GetMunicipalityForecastUseCase {

    MunicipalityForecast getForecast(String municipalityId, TemperatureUnit temperatureUnit);
}