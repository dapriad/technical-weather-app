package com.dapriad.weather.shared.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dapriad.weather.application.exception.WeatherProviderException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(WeatherProviderException.class)
    public ProblemDetail handleWeatherProviderException(WeatherProviderException ex) {
        HttpStatus status = resolveStatus(ex.reason());

        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(mapTitle(ex));
        problem.setDetail(mapDetail(ex));
        problem.setProperty("code", ex.reason().name());
        problem.setInstance(null);

        return problem;
    }

    private HttpStatus resolveStatus(WeatherProviderException.Reason reason) {
        return switch (reason) {
            case NOT_CONFIGURED -> HttpStatus.SERVICE_UNAVAILABLE;
            case RATE_LIMITED -> HttpStatus.SERVICE_UNAVAILABLE;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case INVALID_RESPONSE, INVALID_DATA_URL, UNAUTHORIZED -> HttpStatus.BAD_GATEWAY;
        };
    }

    private String mapTitle(WeatherProviderException ex) {
        return switch (ex.reason()) {
            case NOT_CONFIGURED -> "AEMET API key is missing";
            case UNAUTHORIZED -> "AEMET credentials rejected";
            case RATE_LIMITED -> "AEMET rate limit exceeded";
            case NOT_FOUND -> "AEMET resource not found";
            case UNAVAILABLE -> "AEMET service unavailable";
            case TIMEOUT -> "AEMET request timed out";
            case INVALID_RESPONSE -> "AEMET returned invalid data";
            case INVALID_DATA_URL -> "AEMET data URL invalid";
        };
    }

    private String mapDetail(WeatherProviderException ex) {
        return switch (ex.reason()) {
            case NOT_CONFIGURED -> "Weather data is not available. Check the AEMET_API_KEY configuration.";
            case UNAUTHORIZED -> "AEMET rejected the API key. Verify the key is valid and not expired.";
            case RATE_LIMITED -> "AEMET rate limit exceeded. Please try again later.";
            case NOT_FOUND -> "The requested weather resource was not found.";
            case UNAVAILABLE -> "Weather data is temporarily unavailable. Please try again later.";
            case TIMEOUT -> "AEMET request timed out. Please try again later.";
            case INVALID_RESPONSE -> "AEMET returned an unexpected response format. Please try again later.";
            case INVALID_DATA_URL -> "AEMET data URL is invalid. Please try again later.";
        };
    }
}