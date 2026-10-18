package com.dapriad.weather.infrastructure.adapter.out.aemet;

import java.util.List;
import java.util.stream.Collectors;

import com.dapriad.weather.application.exception.WeatherProviderException;
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
            throw mapClientException(e);
        }
    }

    @Override
    public List<Municipality> searchByName(String name) {
        List<Municipality> all = findAll();
        return all.stream()
                .filter(m -> m.name() != null && m.name().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    private WeatherProviderException mapClientException(AemetClientException e) {
        return switch (e.reason()) {
            case MISSING_API_KEY -> new WeatherProviderException(WeatherProviderException.Reason.NOT_CONFIGURED, "AEMET API key is not configured");
            case UNAUTHORIZED -> new WeatherProviderException(WeatherProviderException.Reason.UNAUTHORIZED, "AEMET rejected credentials. Verify the API key.", e);
            case RATE_LIMITED -> new WeatherProviderException(WeatherProviderException.Reason.RATE_LIMITED, "AEMET rate limit exceeded. Try again later.", e);
            case NOT_FOUND -> new WeatherProviderException(WeatherProviderException.Reason.NOT_FOUND, "AEMET resource was not found", e);
            case UPSTREAM_ERROR -> new WeatherProviderException(WeatherProviderException.Reason.UNAVAILABLE, "AEMET service unavailable", e);
            case TIMEOUT -> new WeatherProviderException(WeatherProviderException.Reason.TIMEOUT, "AEMET request timed out", e);
            case CONNECT_ERROR -> new WeatherProviderException(WeatherProviderException.Reason.UNAVAILABLE, "Cannot connect to AEMET", e);
            case PARSING_ERROR -> new WeatherProviderException(WeatherProviderException.Reason.INVALID_RESPONSE, "AEMET returned an invalid response", e);
            case INVALID_DATA_URL -> new WeatherProviderException(WeatherProviderException.Reason.INVALID_DATA_URL, "AEMET data URL is invalid", e);
            case HTTP_ERROR -> new WeatherProviderException(WeatherProviderException.Reason.UNAVAILABLE, "AEMET service unavailable", e);
        };
    }
}