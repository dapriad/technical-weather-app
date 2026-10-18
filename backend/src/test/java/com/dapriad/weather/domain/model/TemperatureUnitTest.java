package com.dapriad.weather.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class TemperatureUnitTest {

    @Test
    void shouldRoundCelsiusToOneDecimal() {
        BigDecimal result = TemperatureUnit.G_CEL.convertFromCelsius(new BigDecimal("22.75"));

        assertThat(result).isEqualByComparingTo("22.8");
    }

    @Test
    void shouldConvertCelsiusToFahrenheit() {
        BigDecimal result = TemperatureUnit.G_FAH.convertFromCelsius(new BigDecimal("22.75"));

        assertThat(result).isEqualByComparingTo("73.0");
    }

    @Test
    void shouldKeepZeroCelsius() {
        BigDecimal result = TemperatureUnit.G_CEL.convertFromCelsius(BigDecimal.ZERO);

        assertThat(result).isEqualByComparingTo("0.0");
    }

    @Test
    void shouldConvertFreezingPoint() {
        BigDecimal result = TemperatureUnit.G_FAH.convertFromCelsius(BigDecimal.ZERO);

        assertThat(result).isEqualByComparingTo("32.0");
    }
}