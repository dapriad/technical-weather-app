package com.dapriad.weather.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.dapriad.weather.application.port.in.GetMunicipalityForecastUseCase;
import com.dapriad.weather.application.port.in.SearchMunicipalitiesUseCase;
import com.dapriad.weather.application.service.GetMunicipalityForecastService;
import com.dapriad.weather.application.service.SearchMunicipalitiesService;
import com.dapriad.weather.domain.port.ForecastProvider;
import com.dapriad.weather.domain.port.MunicipalityProvider;
import com.dapriad.weather.infrastructure.adapter.out.aemet.AemetForecastAdapter;
import com.dapriad.weather.infrastructure.adapter.out.aemet.AemetForecastClient;
import com.dapriad.weather.infrastructure.adapter.out.aemet.AemetMunicipalityAdapter;
import com.dapriad.weather.infrastructure.adapter.out.aemet.AemetMunicipalityClient;
import com.dapriad.weather.infrastructure.adapter.out.aemet.AemetOpenDataClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ApplicationConfig {

    @Bean
    SearchMunicipalitiesUseCase searchMunicipalitiesUseCase(MunicipalityProvider municipalityProvider) {
        return new SearchMunicipalitiesService(municipalityProvider);
    }

    @Bean
    GetMunicipalityForecastUseCase getMunicipalityForecastUseCase(ForecastProvider forecastProvider, Clock clock) {
        return new GetMunicipalityForecastService(forecastProvider, clock);
    }

    @Bean
    Clock weatherClock() {
        return Clock.systemUTC();
    }

    @Bean
    AemetOpenDataClient aemetOpenDataClient(RestClient aemetRestClient, AemetProperties properties) {
        return new AemetOpenDataClient(aemetRestClient, new ObjectMapper(), properties.getKey());
    }

    @Bean
    AemetMunicipalityClient aemetMunicipalityClient(AemetOpenDataClient dataClient) {
        return new AemetMunicipalityClient(dataClient);
    }

    @Bean
    AemetForecastClient aemetForecastClient(AemetOpenDataClient dataClient) {
        return new AemetForecastClient(dataClient);
    }

    @Bean
    MunicipalityProvider municipalityProvider(AemetMunicipalityClient client) {
        return new AemetMunicipalityAdapter(client);
    }

    @Bean
    ForecastProvider forecastProvider(AemetForecastClient client) {
        return new AemetForecastAdapter(client);
    }
}