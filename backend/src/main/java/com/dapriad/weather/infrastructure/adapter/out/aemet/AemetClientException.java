package com.dapriad.weather.infrastructure.adapter.out.aemet;

public class AemetClientException extends RuntimeException {

    public enum Reason {
        MISSING_API_KEY,
        UNAUTHORIZED,
        RATE_LIMITED,
        NOT_FOUND,
        UPSTREAM_ERROR,
        HTTP_ERROR,
        TIMEOUT,
        CONNECT_ERROR,
        PARSING_ERROR,
        INVALID_DATA_URL,
        BUDGET_EXHAUSTED
    }

    private final Reason reason;

    public AemetClientException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public AemetClientException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}