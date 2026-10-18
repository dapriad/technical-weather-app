package com.dapriad.weather.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dapriad.weather.application.port.in.SearchMunicipalitiesUseCase;
import com.dapriad.weather.application.service.SearchMunicipalitiesService;
import com.dapriad.weather.domain.port.MunicipalityProvider;
import com.dapriad.weather.infrastructure.adapter.out.aemet.AemetMunicipalityAdapter;
import com.dapriad.weather.infrastructure.adapter.out.aemet.AemetMunicipalityClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ApplicationConfig {

    @Bean
    SearchMunicipalitiesUseCase searchMunicipalitiesUseCase(MunicipalityProvider municipalityProvider) {
        return new SearchMunicipalitiesService(municipalityProvider);
    }

    @Bean
    AemetMunicipalityClient aemetMunicipalityClient(
            org.springframework.web.client.RestClient aemetRestClient,
            AemetProperties properties) {
        return new AemetMunicipalityClient(aemetRestClient, new ObjectMapper(), properties.getKey());
    }

    @Bean
    MunicipalityProvider municipalityProvider(AemetMunicipalityClient client) {
        return new AemetMunicipalityAdapter(client);
    }
}