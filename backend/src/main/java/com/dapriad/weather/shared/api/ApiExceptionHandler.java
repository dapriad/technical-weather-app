package com.dapriad.weather.shared.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dapriad.weather.application.exception.MunicipalityProviderException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MunicipalityProviderException.class)
    public ProblemDetail handleMunicipalityProviderException(MunicipalityProviderException ex) {
        HttpStatus status = resolveStatus(ex.reason());

        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(mapTitle(ex));
        problem.setDetail(mapDetail(ex));
        problem.setProperty("code", ex.reason().name());
        problem.setInstance(null);

        return problem;
    }

    private HttpStatus resolveStatus(MunicipalityProviderException.Reason reason) {
        return switch (reason) {
            case NOT_CONFIGURED -> HttpStatus.SERVICE_UNAVAILABLE;
            case RATE_LIMITED -> HttpStatus.SERVICE_UNAVAILABLE;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case INVALID_RESPONSE, INVALID_DATA_URL -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.BAD_GATEWAY;
        };
    }

    private String mapTitle(MunicipalityProviderException ex) {
        return switch (ex.reason()) {
            case NOT_CONFIGURED -> "AEMET API key is missing";
            case UNAUTHORIZED -> "AEMET credentials rejected";
            case RATE_LIMITED -> "AEMET rate limit exceeded";
            case UNAVAILABLE -> "AEMET service unavailable";
            case TIMEOUT -> "AEMET request timed out";
            case INVALID_RESPONSE -> "AEMET returned invalid data";
            case INVALID_DATA_URL -> "AEMET data URL invalid";
        };
    }

    private String mapDetail(MunicipalityProviderException ex) {
        return switch (ex.reason()) {
            case NOT_CONFIGURED -> "Municipality data is not available. Check the AEMET_API_KEY configuration.";
            case UNAUTHORIZED -> "AEMET rejected the API key. Verify the key is valid and not expired.";
            case RATE_LIMITED -> "AEMET rate limit exceeded. Please try again later.";
            case UNAVAILABLE -> "Municipality data is temporarily unavailable. Please try again later.";
            case TIMEOUT -> "AEMET request timed out. Please try again later.";
            case INVALID_RESPONSE -> "AEMET returned an unexpected response format. Please try again later.";
            case INVALID_DATA_URL -> "AEMET data URL is invalid. Please try again later.";
        };
    }
}