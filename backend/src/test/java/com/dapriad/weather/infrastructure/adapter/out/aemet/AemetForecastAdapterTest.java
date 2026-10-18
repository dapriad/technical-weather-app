package com.dapriad.weather.infrastructure.adapter.out.aemet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dapriad.weather.application.exception.WeatherProviderException;
import com.dapriad.weather.domain.model.DailyForecast;
import com.dapriad.weather.domain.model.PrecipitationProbability;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetForecastDayDto;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetForecastDto;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetPrecipitationDto;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetPredictionDto;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetTemperatureDto;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetTemperatureValueDto;

class AemetForecastAdapterTest {

    @Test
    void shouldAverageDatoValues() {
        var client = mock(AemetForecastClient.class);
        when(client.fetchForecast("28079")).thenReturn(List.of(
                forecast(day("2026-10-19",
                        new AemetTemperatureDto(31, 15, List.of(
                                new AemetTemperatureValueDto(16, 6),
                                new AemetTemperatureValueDto(27, 12),
                                new AemetTemperatureValueDto(28, 18),
                                new AemetTemperatureValueDto(20, 24)
                        )),
                        List.of(new AemetPrecipitationDto(5, "00-06"))))
        ));
        var adapter = new AemetForecastAdapter(client);

        List<DailyForecast> result = adapter.findDailyForecasts("28079");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 10, 19));
        assertThat(result.get(0).averageTemperatureCelsius()).isEqualByComparingTo(new BigDecimal("22.75"));
        assertThat(result.get(0).precipitationProbability()).containsExactly(
                new PrecipitationProbability(5, "00-06"));
    }

    @Test
    void shouldFallbackToMaxMinWhenDatoIsEmpty() {
        var client = mock(AemetForecastClient.class);
        when(client.fetchForecast("28079")).thenReturn(List.of(
                forecast(day("2026-10-19",
                        new AemetTemperatureDto(30, 10, Collections.emptyList()),
                        Collections.emptyList()))
        ));
        var adapter = new AemetForecastAdapter(client);

        List<DailyForecast> result = adapter.findDailyForecasts("28079");

        assertThat(result.get(0).averageTemperatureCelsius()).isEqualByComparingTo("20.0");
    }

    @Test
    void shouldThrowWhenForecastResponseIsEmpty() {
        var client = mock(AemetForecastClient.class);
        when(client.fetchForecast("28079")).thenReturn(Collections.emptyList());
        var adapter = new AemetForecastAdapter(client);

        assertThatThrownBy(() -> adapter.findDailyForecasts("28079"))
                .isInstanceOf(WeatherProviderException.class)
                .hasFieldOrPropertyWithValue("reason", WeatherProviderException.Reason.INVALID_RESPONSE);
    }

    @Test
    void shouldThrowWhenTemperatureIsMissing() {
        var client = mock(AemetForecastClient.class);
        when(client.fetchForecast("28079")).thenReturn(List.of(
                forecast(day("2026-10-19", null, Collections.emptyList()))
        ));
        var adapter = new AemetForecastAdapter(client);

        assertThatThrownBy(() -> adapter.findDailyForecasts("28079"))
                .isInstanceOf(WeatherProviderException.class)
                .hasFieldOrPropertyWithValue("reason", WeatherProviderException.Reason.INVALID_RESPONSE);
    }

    @Test
    void shouldTranslateAemetClientException() {
        var client = mock(AemetForecastClient.class);
        when(client.fetchForecast("28079")).thenThrow(
                new AemetClientException(AemetClientException.Reason.RATE_LIMITED, "AEMET rate limit exceeded")
        );
        var adapter = new AemetForecastAdapter(client);

        assertThatThrownBy(() -> adapter.findDailyForecasts("28079"))
                .isInstanceOf(WeatherProviderException.class)
                .hasFieldOrPropertyWithValue("reason", WeatherProviderException.Reason.RATE_LIMITED);
    }

    private AemetForecastDto forecast(AemetForecastDayDto day) {
        return new AemetForecastDto("Madrid", "Madrid", new AemetPredictionDto(List.of(day)));
    }

    private AemetForecastDayDto day(String fecha, AemetTemperatureDto temperatura, List<AemetPrecipitationDto> probabilities) {
        return new AemetForecastDayDto(fecha, temperatura, probabilities);
    }
}