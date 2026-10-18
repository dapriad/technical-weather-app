package com.dapriad.weather.infrastructure.adapter.out.aemet;

import java.util.List;

import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetMunicipalityDto;
import com.fasterxml.jackson.core.type.TypeReference;

public class AemetMunicipalityClient {

    private static final String MUNICIPALITIES_PATH = "/api/maestro/municipios";

    private final AemetOpenDataClient dataClient;

    public AemetMunicipalityClient(AemetOpenDataClient dataClient) {
        this.dataClient = dataClient;
    }

    public List<AemetMunicipalityDto> fetchMunicipalities() {
        return dataClient.fetchData(MUNICIPALITIES_PATH, new TypeReference<List<AemetMunicipalityDto>>() {});
    }
}