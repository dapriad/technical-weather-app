package com.dapriad.weather.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dapriad.weather.application.exception.InvalidMunicipalityIdException;
import com.dapriad.weather.application.exception.WeatherProviderException;
import com.dapriad.weather.domain.model.DailyForecast;
import com.dapriad.weather.domain.model.MunicipalityForecast;
import com.dapriad.weather.domain.model.PrecipitationProbability;
import com.dapriad.weather.domain.model.TemperatureUnit;
import com.dapriad.weather.domain.port.ForecastProvider;

class GetMunicipalityForecastServiceTest {

    private ForecastProvider forecastProvider;
    private GetMunicipalityForecastService service;

    @BeforeEach
    void setUp() {
        forecastProvider = mock(ForecastProvider.class);
        Clock clock = Clock.fixed(Instant.parse("2026-10-18T10:00:00Z"), ZoneOffset.UTC);
        service = new GetMunicipalityForecastService(forecastProvider, clock);
    }

    @Test
    void shouldReturnTomorrowForecastWithDefaultUnit() {
        when(forecastProvider.findDailyForecasts("28079")).thenReturn(List.of(
                forecast(LocalDate.of(2026, 10, 18), new BigDecimal("20.0")),
                forecast(LocalDate.of(2026, 10, 19), new BigDecimal("22.8"))
        ));

        MunicipalityForecast result = service.getForecast("28079", null);

        assertThat(result.temperatureUnit()).isEqualTo(TemperatureUnit.G_CEL);
        assertThat(result.averageTemperature()).isEqualByComparingTo("22.8");
    }

    @Test
    void shouldConvertToFahrenheit() {
        when(forecastProvider.findDailyForecasts("28079")).thenReturn(List.of(
                forecast(LocalDate.of(2026, 10, 19), new BigDecimal("22.8"))
        ));

        MunicipalityForecast result = service.getForecast("28079", TemperatureUnit.G_FAH);

        assertThat(result.temperatureUnit()).isEqualTo(TemperatureUnit.G_FAH);
        assertThat(result.averageTemperature()).isEqualByComparingTo("73.0");
    }

    @Test
    void shouldAcceptIdWithPrefix() {
        when(forecastProvider.findDailyForecasts("28079")).thenReturn(List.of(
                forecast(LocalDate.of(2026, 10, 19), new BigDecimal("22.8"))
        ));

        MunicipalityForecast result = service.getForecast("id28079", TemperatureUnit.G_CEL);

        assertThat(result.averageTemperature()).isEqualByComparingTo("22.8");
    }

    @Test
    void shouldFilterAndOrderPrecipitationPeriods() {
        when(forecastProvider.findDailyForecasts("28079")).thenReturn(List.of(
                new DailyForecast(
                        LocalDate.of(2026, 10, 19),
                        new BigDecimal("22.8"),
                        List.of(
                                new PrecipitationProbability(5, "12-24"),
                                new PrecipitationProbability(0, "00-24"),
                                new PrecipitationProbability(10, "06-12"),
                                new PrecipitationProbability(0, "00-06"),
                                new PrecipitationProbability(5, "12-18"),
                                new PrecipitationProbability(0, "18-24")
                        )
                )
        ));

        MunicipalityForecast result = service.getForecast("28079", TemperatureUnit.G_CEL);

        assertThat(result.precipitationProbability()).containsExactly(
                new PrecipitationProbability(0, "00-06"),
                new PrecipitationProbability(10, "06-12"),
                new PrecipitationProbability(5, "12-18"),
                new PrecipitationProbability(0, "18-24")
        );
    }

    @Test
    void shouldThrowWhenMunicipalityIdIsInvalid() {
        assertThatThrownBy(() -> service.getForecast("123", TemperatureUnit.G_CEL))
                .isInstanceOf(InvalidMunicipalityIdException.class);
    }

    @Test
    void shouldThrowWhenTomorrowForecastIsMissing() {
        when(forecastProvider.findDailyForecasts("28079")).thenReturn(List.of(
                forecast(LocalDate.of(2026, 10, 18), new BigDecimal("20.0"))
        ));

        assertThatThrownBy(() -> service.getForecast("28079", TemperatureUnit.G_CEL))
                .isInstanceOf(WeatherProviderException.class)
                .hasFieldOrPropertyWithValue("reason", WeatherProviderException.Reason.NOT_FOUND);
    }

    private DailyForecast forecast(LocalDate date, BigDecimal averageTemperatureCelsius) {
        return new DailyForecast(
                date,
                averageTemperatureCelsius,
                List.of(
                        new PrecipitationProbability(0, "00-06"),
                        new PrecipitationProbability(0, "06-12"),
                        new PrecipitationProbability(0, "12-18"),
                        new PrecipitationProbability(0, "18-24")
                )
        );
    }
}