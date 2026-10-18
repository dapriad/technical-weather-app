package com.dapriad.weather.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import com.dapriad.weather.application.exception.InvalidMunicipalityIdException;
import com.dapriad.weather.application.exception.WeatherProviderException;
import com.dapriad.weather.application.port.in.GetMunicipalityForecastUseCase;
import com.dapriad.weather.domain.model.DailyForecast;
import com.dapriad.weather.domain.model.MunicipalityForecast;
import com.dapriad.weather.domain.model.MunicipalityIds;
import com.dapriad.weather.domain.model.PrecipitationProbability;
import com.dapriad.weather.domain.model.TemperatureUnit;
import com.dapriad.weather.domain.port.ForecastProvider;

public class GetMunicipalityForecastService implements GetMunicipalityForecastUseCase {

    private static final ZoneId SPAIN_ZONE = ZoneId.of("Europe/Madrid");
    private static final List<String> PRECIPITATION_PERIODS = List.of("00-06", "06-12", "12-18", "18-24");

    private final ForecastProvider forecastProvider;
    private final Clock clock;

    public GetMunicipalityForecastService(ForecastProvider forecastProvider, Clock clock) {
        this.forecastProvider = forecastProvider;
        this.clock = clock;
    }

    @Override
    public MunicipalityForecast getForecast(String municipalityId, TemperatureUnit temperatureUnit) {
        String normalizedId = validateAndNormalize(municipalityId);
        TemperatureUnit unit = temperatureUnit == null ? TemperatureUnit.G_CEL : temperatureUnit;

        List<DailyForecast> forecasts = forecastProvider.findDailyForecasts(normalizedId);
        LocalDate tomorrow = LocalDate.now(clock.withZone(SPAIN_ZONE)).plusDays(1);

        DailyForecast dailyForecast = forecasts.stream()
                .filter(forecast -> tomorrow.equals(forecast.date()))
                .findFirst()
                .orElseThrow(() -> new WeatherProviderException(
                        WeatherProviderException.Reason.NOT_FOUND,
                        "Forecast for tomorrow is not available"));

        return new MunicipalityForecast(
                unit.convertFromCelsius(dailyForecast.averageTemperatureCelsius()),
                unit,
                filterPrecipitationPeriods(dailyForecast.precipitationProbability()));
    }

    private String validateAndNormalize(String municipalityId) {
        String normalized = MunicipalityIds.normalize(municipalityId);
        if (normalized == null || !normalized.matches("\\d{5}")) {
            throw new InvalidMunicipalityIdException("Municipality ID must be a five-digit INE code");
        }
        return normalized;
    }

    private List<PrecipitationProbability> filterPrecipitationPeriods(List<PrecipitationProbability> probabilities) {
        return PRECIPITATION_PERIODS.stream()
                .map(period -> probabilities.stream()
                        .filter(p -> period.equals(p.period()))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }
}