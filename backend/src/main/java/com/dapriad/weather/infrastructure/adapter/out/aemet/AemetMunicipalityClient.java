package com.dapriad.weather.infrastructure.adapter.out.aemet;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetEnvelope;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetMunicipalityDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AemetMunicipalityClient {

    private static final Logger log = LoggerFactory.getLogger(AemetMunicipalityClient.class);
    private static final String AEMET_ALLOWED_HOST = "opendata.aemet.es";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public AemetMunicipalityClient(RestClient restClient, ObjectMapper objectMapper, String apiKey) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    public List<AemetMunicipalityDto> fetchMunicipalities() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AemetClientException(AemetClientException.Reason.MISSING_API_KEY, "AEMET API key is not configured");
        }

        AemetEnvelope envelope = fetchEnvelope();

        if (envelope.datos() == null || envelope.datos().isBlank()) {
            throw new AemetClientException(AemetClientException.Reason.INVALID_DATA_URL, "AEMET response does not contain a data URL");
        }

        if (!isValidDataUrl(envelope.datos())) {
            throw new AemetClientException(AemetClientException.Reason.INVALID_DATA_URL, "AEMET data URL has an invalid or unexpected host");
        }

        return fetchDataUrl(envelope.datos());
    }

    AemetEnvelope fetchEnvelope() {
        try {
            log.info("Fetching AEMET municipalities envelope from /api/maestro/municipios");

            return restClient.get()
                    .uri("/api/maestro/municipios?api_key={apiKey}", apiKey)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN, (req, res) -> {
                        throw new AemetClientException(AemetClientException.Reason.HTTP_ERROR, "AEMET rejected the API key (HTTP " + res.getStatusCode() + ")");
                    })
                    .onStatus(status -> status == HttpStatus.TOO_MANY_REQUESTS, (req, res) -> {
                        throw new AemetClientException(AemetClientException.Reason.HTTP_ERROR, "AEMET rate limit exceeded");
                    })
                    .onStatus(status -> status.is5xxServerError(), (req, res) -> {
                        log.error("AEMET returned server error HTTP {}", res.getStatusCode());
                        throw new AemetClientException(AemetClientException.Reason.HTTP_ERROR, "AEMET server error");
                    })
                    .body(AemetEnvelope.class);

        } catch (AemetClientException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new AemetClientException(AemetClientException.Reason.TIMEOUT, "AEMET request timed out", e);
        } catch (HttpClientErrorException e) {
            throw new AemetClientException(AemetClientException.Reason.HTTP_ERROR, "AEMET HTTP error: " + e.getStatusCode(), e);
        } catch (HttpServerErrorException e) {
            throw new AemetClientException(AemetClientException.Reason.HTTP_ERROR, "AEMET server error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new AemetClientException(AemetClientException.Reason.HTTP_ERROR, "Unexpected error fetching AEMET envelope", e);
        }
    }

    List<AemetMunicipalityDto> fetchDataUrl(String dataUrl) {
        try {
            log.info("Fetching AEMET municipalities data from /datos ({} characters of URL)", Integer.min(dataUrl.length(), 50));

            String body = restClient.get()
                    .uri(dataUrl)
                    .retrieve()
                    .body(String.class);

            return objectMapper.readValue(body, new TypeReference<List<AemetMunicipalityDto>>() {});

        } catch (ResourceAccessException e) {
            throw new AemetClientException(AemetClientException.Reason.TIMEOUT, "AEMET data request timed out", e);
        } catch (Exception e) {
            throw new AemetClientException(AemetClientException.Reason.PARSING_ERROR, "Failed to parse AEMET data response", e);
        }
    }

    private boolean isValidDataUrl(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);
            String host = uri.getHost();
            if (host == null) return false;
            return host.equalsIgnoreCase(AEMET_ALLOWED_HOST) || host.endsWith("." + AEMET_ALLOWED_HOST);
        } catch (Exception e) {
            return false;
        }
    }
}