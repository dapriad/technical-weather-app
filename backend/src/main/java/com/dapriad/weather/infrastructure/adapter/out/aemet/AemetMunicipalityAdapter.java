package com.dapriad.weather.infrastructure.adapter.out.aemet;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dapriad.weather.application.exception.MunicipalityProviderException;
import com.dapriad.weather.domain.model.Municipality;
import com.dapriad.weather.domain.port.MunicipalityProvider;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetMunicipalityDto;

public class AemetMunicipalityAdapter implements MunicipalityProvider {

    private static final Logger log = LoggerFactory.getLogger(AemetMunicipalityAdapter.class);

    private final AemetMunicipalityClient client;

    public AemetMunicipalityAdapter(AemetMunicipalityClient client) {
        this.client = client;
    }

    @Override
    public List<Municipality> findAll() {
        try {
            List<AemetMunicipalityDto> dtos = client.fetchMunicipalities();
            return dtos.stream()
                    .map(dto -> new Municipality(dto.id(), dto.nombre(), dto.provincia()))
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

    private MunicipalityProviderException mapClientException(AemetClientException e) {
        return switch (e.reason()) {
            case MISSING_API_KEY -> new MunicipalityProviderException(MunicipalityProviderException.Reason.NOT_CONFIGURED, "AEMET API key is not configured");
            case HTTP_ERROR -> {
                String msg = e.getMessage();
                if (msg != null && msg.contains("API key")) {
                    yield new MunicipalityProviderException(MunicipalityProviderException.Reason.UNAUTHORIZED, "AEMET rejected credentials. Verify the API key.", e);
                }
                if (msg != null && msg.contains("rate limit")) {
                    yield new MunicipalityProviderException(MunicipalityProviderException.Reason.RATE_LIMITED, "AEMET rate limit exceeded. Try again later.", e);
                }
                yield new MunicipalityProviderException(MunicipalityProviderException.Reason.UNAVAILABLE, "AEMET service unavailable", e);
            }
            case TIMEOUT -> new MunicipalityProviderException(MunicipalityProviderException.Reason.TIMEOUT, "AEMET request timed out", e);
            case CONNECT_ERROR -> new MunicipalityProviderException(MunicipalityProviderException.Reason.UNAVAILABLE, "Cannot connect to AEMET", e);
            case PARSING_ERROR -> new MunicipalityProviderException(MunicipalityProviderException.Reason.INVALID_RESPONSE, "AEMET returned an invalid response", e);
            case INVALID_DATA_URL -> new MunicipalityProviderException(MunicipalityProviderException.Reason.INVALID_DATA_URL, "AEMET data URL is invalid", e);
        };
    }
}