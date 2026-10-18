package com.dapriad.weather.application.exception;

public class InvalidMunicipalityIdException extends RuntimeException {

    public InvalidMunicipalityIdException(String message) {
        super(message);
    }
}