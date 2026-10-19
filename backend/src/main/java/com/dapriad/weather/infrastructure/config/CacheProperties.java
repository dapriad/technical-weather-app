package com.dapriad.weather.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private Duration municipalitiesTtl = Duration.ofHours(24);
    private Duration forecastsTtl = Duration.ofMinutes(30);
    private long forecastsMaxSize = 300;

    public Duration getMunicipalitiesTtl() {
        return municipalitiesTtl;
    }

    public void setMunicipalitiesTtl(Duration municipalitiesTtl) {
        this.municipalitiesTtl = municipalitiesTtl;
    }

    public Duration getForecastsTtl() {
        return forecastsTtl;
    }

    public void setForecastsTtl(Duration forecastsTtl) {
        this.forecastsTtl = forecastsTtl;
    }

    public long getForecastsMaxSize() {
        return forecastsMaxSize;
    }

    public void setForecastsMaxSize(long forecastsMaxSize) {
        this.forecastsMaxSize = forecastsMaxSize;
    }
}
