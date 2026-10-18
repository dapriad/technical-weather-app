package com.dapriad.weather.infrastructure.adapter.out.aemet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.dapriad.weather.domain.model.DailyForecast;
import com.dapriad.weather.domain.model.PrecipitationProbability;
import com.dapriad.weather.domain.port.ForecastProvider;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetForecastDayDto;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetForecastDto;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetPrecipitationDto;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetTemperatureDto;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetTemperatureValueDto;

public class AemetForecastAdapter implements ForecastProvider {

    private final AemetForecastClient client;

    public AemetForecastAdapter(AemetForecastClient client) {
        this.client = client;
    }

    @Override
    public List<DailyForecast> findDailyForecasts(String municipalityId) {
        try {
            List<AemetForecastDto> forecasts = client.fetchForecast(municipalityId);

            if (forecasts == null || forecasts.isEmpty()) {
                throw new AemetClientException(AemetClientException.Reason.PARSING_ERROR, "AEMET forecast response is empty");
            }

            AemetForecastDto forecast = forecasts.get(0);

            if (forecast.prediccion() == null || forecast.prediccion().days() == null || forecast.prediccion().days().isEmpty()) {
                throw new AemetClientException(AemetClientException.Reason.PARSING_ERROR, "AEMET forecast response has no days");
            }

            return forecast.prediccion().days().stream()
                    .map(this::toDailyForecast)
                    .toList();

        } catch (AemetClientException e) {
            throw AemetClientExceptionMapper.toWeatherProviderException(e);
        }
    }

    private DailyForecast toDailyForecast(AemetForecastDayDto day) {
        LocalDate date = parseDate(day.fecha());
        BigDecimal average = calculateAverage(day.temperatura());
        List<PrecipitationProbability> probabilities = mapPrecipitations(day.probPrecipitacion());
        return new DailyForecast(date, average, probabilities);
    }

    private LocalDate parseDate(String fecha) {
        if (fecha == null || fecha.length() < 10) {
            throw new AemetClientException(AemetClientException.Reason.PARSING_ERROR, "AEMET forecast day has no valid date");
        }
        return LocalDate.parse(fecha.substring(0, 10));
    }

    private BigDecimal calculateAverage(AemetTemperatureDto temperatura) {
        if (temperatura == null) {
            throw new AemetClientException(AemetClientException.Reason.PARSING_ERROR, "AEMET forecast day has no temperature");
        }

        List<Integer> values = temperatura.dato() == null ? List.of() : temperatura.dato().stream()
                .map(AemetTemperatureValueDto::value)
                .filter(Objects::nonNull)
                .toList();

        if (!values.isEmpty()) {
            BigDecimal sum = values.stream()
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return sum.divide(BigDecimal.valueOf(values.size()), 10, RoundingMode.HALF_UP);
        }

        if (temperatura.maxima() != null && temperatura.minima() != null) {
            return BigDecimal.valueOf(temperatura.maxima() + temperatura.minima())
                    .divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
        }

        throw new AemetClientException(AemetClientException.Reason.PARSING_ERROR, "AEMET forecast day has no usable temperature values");
    }

    private List<PrecipitationProbability> mapPrecipitations(List<AemetPrecipitationDto> probabilities) {
        if (probabilities == null) {
            return Collections.emptyList();
        }

        return probabilities.stream()
                .filter(p -> p.value() != null && p.periodo() != null)
                .map(p -> new PrecipitationProbability(p.value(), p.periodo()))
                .toList();
    }
}