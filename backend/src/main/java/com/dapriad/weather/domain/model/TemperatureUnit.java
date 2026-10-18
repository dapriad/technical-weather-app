package com.dapriad.weather.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum TemperatureUnit {
    G_CEL,
    G_FAH;

    public BigDecimal convertFromCelsius(BigDecimal celsius) {
        if (celsius == null) {
            return null;
        }

        if (this == G_CEL) {
            return celsius.setScale(1, RoundingMode.HALF_UP);
        }

        BigDecimal fahrenheit = celsius.multiply(BigDecimal.valueOf(9))
                .divide(BigDecimal.valueOf(5), 10, RoundingMode.HALF_UP)
                .add(BigDecimal.valueOf(32));

        return fahrenheit.setScale(1, RoundingMode.HALF_UP);
    }
}