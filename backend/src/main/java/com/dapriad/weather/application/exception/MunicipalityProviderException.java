package com.dapriad.weather.application.exception;

public class MunicipalityProviderException extends RuntimeException {

    public enum Reason {
        NOT_CONFIGURED,
        UNAUTHORIZED,
        RATE_LIMITED,
        UNAVAILABLE,
        TIMEOUT,
        INVALID_RESPONSE,
        INVALID_DATA_URL
    }

    private final Reason reason;

    public MunicipalityProviderException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public MunicipalityProviderException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}