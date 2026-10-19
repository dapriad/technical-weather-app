package com.dapriad.weather.infrastructure.adapter.out.aemet;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetMunicipalityDto;
import com.fasterxml.jackson.core.type.TypeReference;

public class AemetMunicipalityClient {

    private static final String MUNICIPALITIES_PATH = "/api/maestro/municipios";

    private final AemetOpenDataClient dataClient;

    public AemetMunicipalityClient(AemetOpenDataClient dataClient) {
        this.dataClient = dataClient;
    }

    @Cacheable(
            cacheNames = "municipalities",
            key = "'all'",
            unless = "#result == null || #result.isEmpty()")
    public List<AemetMunicipalityDto> fetchMunicipalities() {
        return dataClient.fetchData(MUNICIPALITIES_PATH, new TypeReference<List<AemetMunicipalityDto>>() {});
    }
}