package com.dapriad.weather.infrastructure.adapter.out.aemet;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetEnvelope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AemetOpenDataClient {

    private static final Logger log = LoggerFactory.getLogger(AemetOpenDataClient.class);
    private static final String AEMET_ALLOWED_HOST = "opendata.aemet.es";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final AemetCallBudget callBudget;

    public AemetOpenDataClient(
            RestClient restClient,
            ObjectMapper objectMapper,
            String apiKey,
            AemetCallBudget callBudget) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.callBudget = callBudget;
    }

    public <T> T fetchData(String path, TypeReference<T> type) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AemetClientException(AemetClientException.Reason.MISSING_API_KEY, "AEMET API key is not configured");
        }

        AemetEnvelope envelope = fetchEnvelope(path);

        if (envelope == null || envelope.datos() == null || envelope.datos().isBlank()) {
            throw new AemetClientException(AemetClientException.Reason.INVALID_DATA_URL, "AEMET response does not contain a data URL");
        }

        if (!isValidDataUrl(envelope.datos())) {
            throw new AemetClientException(AemetClientException.Reason.INVALID_DATA_URL, "AEMET data URL has an invalid or unexpected host");
        }

        return fetchDataUrl(envelope.datos(), type);
    }

    AemetEnvelope fetchEnvelope(String path) {
        callBudget.acquire();

        try {
            log.info("Fetching AEMET envelope from {}", path);

            return restClient.get()
                    .uri(path + "?api_key={apiKey}", apiKey)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN, (req, res) -> {
                        throw new AemetClientException(AemetClientException.Reason.UNAUTHORIZED, "AEMET rejected the API key");
                    })
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new AemetClientException(AemetClientException.Reason.NOT_FOUND, "AEMET resource not found");
                    })
                    .onStatus(status -> status == HttpStatus.TOO_MANY_REQUESTS, (req, res) -> {
                        throw new AemetClientException(AemetClientException.Reason.RATE_LIMITED, "AEMET rate limit exceeded");
                    })
                    .onStatus(status -> status.is5xxServerError(), (req, res) -> {
                        log.error("AEMET returned server error HTTP {}", res.getStatusCode());
                        throw new AemetClientException(AemetClientException.Reason.UPSTREAM_ERROR, "AEMET server error");
                    })
                    .body(AemetEnvelope.class);

        } catch (AemetClientException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new AemetClientException(AemetClientException.Reason.TIMEOUT, "AEMET request timed out", e);
        } catch (HttpClientErrorException e) {
            throw new AemetClientException(AemetClientException.Reason.HTTP_ERROR, "AEMET HTTP error: " + e.getStatusCode(), e);
        } catch (HttpServerErrorException e) {
            throw new AemetClientException(AemetClientException.Reason.UPSTREAM_ERROR, "AEMET server error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new AemetClientException(AemetClientException.Reason.HTTP_ERROR, "Unexpected error fetching AEMET envelope", e);
        }
    }

    <T> T fetchDataUrl(String dataUrl, TypeReference<T> type) {
        callBudget.acquire();

        try {
            log.info("Fetching AEMET data from a data URL");

            String body = restClient.get()
                    .uri(dataUrl)
                    .retrieve()
                    .body(String.class);

            if (body == null || body.isBlank()) {
                throw new AemetClientException(AemetClientException.Reason.PARSING_ERROR, "AEMET data response is empty");
            }

            return objectMapper.readValue(body, type);

        } catch (AemetClientException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new AemetClientException(AemetClientException.Reason.TIMEOUT, "AEMET data request timed out", e);
        } catch (Exception e) {
            throw new AemetClientException(AemetClientException.Reason.PARSING_ERROR, "Failed to parse AEMET data response", e);
        }
    }

    private boolean isValidDataUrl(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            return host.equalsIgnoreCase(AEMET_ALLOWED_HOST) || host.endsWith("." + AEMET_ALLOWED_HOST);
        } catch (Exception e) {
            return false;
        }
    }
}