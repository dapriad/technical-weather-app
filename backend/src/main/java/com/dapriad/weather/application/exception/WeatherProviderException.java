package com.dapriad.weather.application.exception;

public class WeatherProviderException extends RuntimeException {

    public enum Reason {
        NOT_CONFIGURED,
        UNAUTHORIZED,
        RATE_LIMITED,
        NOT_FOUND,
        UNAVAILABLE,
        TIMEOUT,
        INVALID_RESPONSE,
        INVALID_DATA_URL,
        BUDGET_EXHAUSTED
    }

    private final Reason reason;

    public WeatherProviderException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public WeatherProviderException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}