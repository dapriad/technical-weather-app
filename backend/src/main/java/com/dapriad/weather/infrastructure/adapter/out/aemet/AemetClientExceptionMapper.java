package com.dapriad.weather.infrastructure.adapter.out.aemet;

import com.dapriad.weather.application.exception.WeatherProviderException;

public final class AemetClientExceptionMapper {

    private AemetClientExceptionMapper() {
    }

    public static WeatherProviderException toWeatherProviderException(AemetClientException e) {
        return switch (e.reason()) {
            case MISSING_API_KEY -> new WeatherProviderException(
                    WeatherProviderException.Reason.NOT_CONFIGURED,
                    "AEMET API key is not configured");
            case UNAUTHORIZED -> new WeatherProviderException(
                    WeatherProviderException.Reason.UNAUTHORIZED,
                    "AEMET rejected credentials. Verify the API key.", e);
            case RATE_LIMITED -> new WeatherProviderException(
                    WeatherProviderException.Reason.RATE_LIMITED,
                    "AEMET rate limit exceeded. Try again later.", e);
            case NOT_FOUND -> new WeatherProviderException(
                    WeatherProviderException.Reason.NOT_FOUND,
                    "AEMET resource was not found", e);
            case UPSTREAM_ERROR -> new WeatherProviderException(
                    WeatherProviderException.Reason.UNAVAILABLE,
                    "AEMET service unavailable", e);
            case TIMEOUT -> new WeatherProviderException(
                    WeatherProviderException.Reason.TIMEOUT,
                    "AEMET request timed out", e);
            case CONNECT_ERROR -> new WeatherProviderException(
                    WeatherProviderException.Reason.UNAVAILABLE,
                    "Cannot connect to AEMET", e);
            case PARSING_ERROR -> new WeatherProviderException(
                    WeatherProviderException.Reason.INVALID_RESPONSE,
                    "AEMET returned an invalid response", e);
            case INVALID_DATA_URL -> new WeatherProviderException(
                    WeatherProviderException.Reason.INVALID_DATA_URL,
                    "AEMET data URL is invalid", e);
            case HTTP_ERROR -> new WeatherProviderException(
                    WeatherProviderException.Reason.UNAVAILABLE,
                    "AEMET service unavailable", e);
        };
    }
}