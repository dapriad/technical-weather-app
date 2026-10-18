package com.dapriad.weather.infrastructure.adapter.in.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dapriad.weather.application.exception.InvalidMunicipalityIdException;
import com.dapriad.weather.application.exception.WeatherProviderException;
import com.dapriad.weather.application.port.in.GetMunicipalityForecastUseCase;
import com.dapriad.weather.domain.model.MunicipalityForecast;
import com.dapriad.weather.domain.model.PrecipitationProbability;
import com.dapriad.weather.domain.model.TemperatureUnit;

@SpringBootTest
@AutoConfigureMockMvc
class ForecastControllerTest {

    @MockitoBean
    private GetMunicipalityForecastUseCase getMunicipalityForecastUseCase;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnForecastWithDefaultUnit() throws Exception {
        when(getMunicipalityForecastUseCase.getForecast("28079", TemperatureUnit.G_CEL))
                .thenReturn(new MunicipalityForecast(
                        new BigDecimal("22.8"),
                        TemperatureUnit.G_CEL,
                        List.of(
                                new PrecipitationProbability(0, "00-06"),
                                new PrecipitationProbability(10, "06-12")
                        )
                ));

        mockMvc.perform(get("/api/forecast").param("municipalityId", "28079"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageTemperature").value(22.8))
                .andExpect(jsonPath("$.temperatureUnit").value("G_CEL"))
                .andExpect(jsonPath("$.precipitationProbability[0].probability").value(0))
                .andExpect(jsonPath("$.precipitationProbability[0].period").value("00-06"));
    }

    @Test
    void shouldReturnForecastInFahrenheit() throws Exception {
        when(getMunicipalityForecastUseCase.getForecast("28079", TemperatureUnit.G_FAH))
                .thenReturn(new MunicipalityForecast(
                        new BigDecimal("73.0"),
                        TemperatureUnit.G_FAH,
                        List.of(new PrecipitationProbability(5, "12-18"))
                ));

        mockMvc.perform(get("/api/forecast")
                        .param("municipalityId", "28079")
                        .param("temperatureUnit", "G_FAH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageTemperature").value(73.0))
                .andExpect(jsonPath("$.temperatureUnit").value("G_FAH"));
    }

    @Test
    void shouldReturn400ForInvalidMunicipalityId() throws Exception {
        when(getMunicipalityForecastUseCase.getForecast("123", TemperatureUnit.G_CEL))
                .thenThrow(new InvalidMunicipalityIdException("Municipality ID must be a five-digit INE code"));

        mockMvc.perform(get("/api/forecast").param("municipalityId", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MUNICIPALITY_ID"));
    }

    @Test
    void shouldReturn400ForInvalidTemperatureUnit() throws Exception {
        mockMvc.perform(get("/api/forecast")
                        .param("municipalityId", "28079")
                        .param("temperatureUnit", "BAD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_TEMPERATURE_UNIT"));
    }

    @Test
    void shouldReturn404ForMissingForecast() throws Exception {
        when(getMunicipalityForecastUseCase.getForecast("28079", TemperatureUnit.G_CEL))
                .thenThrow(new WeatherProviderException(WeatherProviderException.Reason.NOT_FOUND, "No forecast"));

        mockMvc.perform(get("/api/forecast").param("municipalityId", "28079"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldReturn503ForRateLimited() throws Exception {
        when(getMunicipalityForecastUseCase.getForecast("28079", TemperatureUnit.G_CEL))
                .thenThrow(new WeatherProviderException(WeatherProviderException.Reason.RATE_LIMITED, "Rate limited"));

        mockMvc.perform(get("/api/forecast").param("municipalityId", "28079"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }
}