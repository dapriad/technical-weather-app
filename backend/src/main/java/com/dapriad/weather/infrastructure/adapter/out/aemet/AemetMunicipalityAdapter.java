package com.dapriad.weather.infrastructure.adapter.out.aemet;

import java.util.List;
import java.util.stream.Collectors;

import com.dapriad.weather.domain.model.Municipality;
import com.dapriad.weather.domain.model.MunicipalityIds;
import com.dapriad.weather.domain.port.MunicipalityProvider;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetMunicipalityDto;

public class AemetMunicipalityAdapter implements MunicipalityProvider {

    private final AemetMunicipalityClient client;

    public AemetMunicipalityAdapter(AemetMunicipalityClient client) {
        this.client = client;
    }

    @Override
    public List<Municipality> findAll() {
        try {
            List<AemetMunicipalityDto> dtos = client.fetchMunicipalities();
            return dtos.stream()
                    .map(dto -> new Municipality(MunicipalityIds.normalize(dto.id()), dto.nombre(), dto.provincia()))
                    .collect(Collectors.toList());
        } catch (AemetClientException e) {
            throw AemetClientExceptionMapper.toWeatherProviderException(e);
        }
    }
}