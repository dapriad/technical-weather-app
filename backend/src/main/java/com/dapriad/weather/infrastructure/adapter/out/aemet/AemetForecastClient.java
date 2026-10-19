package com.dapriad.weather.infrastructure.adapter.out.aemet;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetForecastDto;
import com.fasterxml.jackson.core.type.TypeReference;

public class AemetForecastClient {

    private static final String FORECAST_PATH_PREFIX = "/api/prediccion/especifica/municipio/diaria/";

    private final AemetOpenDataClient dataClient;

    public AemetForecastClient(AemetOpenDataClient dataClient) {
        this.dataClient = dataClient;
    }

    @Cacheable(
            cacheNames = "forecasts",
            key = "#municipalityId",
            unless = "#result == null || #result.isEmpty()")
    public List<AemetForecastDto> fetchForecast(String municipalityId) {
        return dataClient.fetchData(FORECAST_PATH_PREFIX + municipalityId, new TypeReference<List<AemetForecastDto>>() {});
    }
}