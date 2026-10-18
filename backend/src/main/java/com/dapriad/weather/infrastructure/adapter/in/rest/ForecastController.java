package com.dapriad.weather.infrastructure.adapter.in.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.dapriad.weather.application.port.in.GetMunicipalityForecastUseCase;
import com.dapriad.weather.domain.model.MunicipalityForecast;
import com.dapriad.weather.domain.model.TemperatureUnit;
import com.dapriad.weather.infrastructure.adapter.in.rest.dto.ForecastResponse;

@RestController
@RequestMapping("/api")
@Tag(name = "Forecast", description = "Daily municipality forecast backed by AEMET OpenData")
public class ForecastController {

    private final GetMunicipalityForecastUseCase getMunicipalityForecastUseCase;

    public ForecastController(GetMunicipalityForecastUseCase getMunicipalityForecastUseCase) {
        this.getMunicipalityForecastUseCase = getMunicipalityForecastUseCase;
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get tomorrow forecast for a municipality", description =
            "Returns the normalized daily forecast for the day after today in Europe/Madrid. " +
            "The municipality ID accepts both the INE code and the AEMET maestro `id` prefix. " +
            "Each request calls AEMET twice (envelope + data URL). No caching in this iteration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Forecast retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ForecastResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid municipality ID or temperature unit",
                    content = @Content(mediaType = "application/problem+json")),
            @ApiResponse(responseCode = "404", description = "Municipality or tomorrow forecast not found",
                    content = @Content(mediaType = "application/problem+json")),
            @ApiResponse(responseCode = "503", description = "AEMET service unavailable or rate limited",
                    content = @Content(mediaType = "application/problem+json")),
            @ApiResponse(responseCode = "504", description = "AEMET request timed out",
                    content = @Content(mediaType = "application/problem+json")),
            @ApiResponse(responseCode = "502", description = "AEMET returned an invalid response or rejected credentials",
                    content = @Content(mediaType = "application/problem+json"))
    })
    public ForecastResponse forecast(
            @Parameter(description = "Municipality INE code. Accepts 28079 or id28079.", example = "28079")
            @RequestParam String municipalityId,
            @Parameter(description = "Temperature unit. Optional, defaults to G_CEL.",
                    schema = @Schema(allowableValues = {"G_CEL", "G_FAH"}, defaultValue = "G_CEL"))
            @RequestParam(required = false, defaultValue = "G_CEL") TemperatureUnit temperatureUnit) {

        MunicipalityForecast forecast = getMunicipalityForecastUseCase.getForecast(municipalityId, temperatureUnit);
        return ForecastResponse.from(forecast);
    }
}