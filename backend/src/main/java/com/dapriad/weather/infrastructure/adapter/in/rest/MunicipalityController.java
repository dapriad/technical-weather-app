package com.dapriad.weather.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.stream.Collectors;

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

import com.dapriad.weather.application.port.in.SearchMunicipalitiesUseCase;
import com.dapriad.weather.domain.model.Municipality;
import com.dapriad.weather.infrastructure.adapter.in.rest.dto.MunicipalityResponse;

@RestController
@RequestMapping("/api/municipalities")
@Tag(name = "Municipalities", description = "Municipality search operations backed by AEMET OpenData")
public class MunicipalityController {

    private final SearchMunicipalitiesUseCase searchMunicipalitiesUseCase;

    public MunicipalityController(SearchMunicipalitiesUseCase searchMunicipalitiesUseCase) {
        this.searchMunicipalitiesUseCase = searchMunicipalitiesUseCase;
    }

    @GetMapping
    @Operation(summary = "Search municipalities by name", description =
            "Returns municipality records matching the given name. " +
            "Search is case and accent insensitive. If name is omitted, returns the full list. " +
            "The municipality catalog is cached in the backend for 24 hours.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Municipalities found (may be empty)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MunicipalityResponse.class))),
            @ApiResponse(responseCode = "503", description = "AEMET service unavailable or API key not configured",
                    content = @Content(mediaType = "application/problem+json")),
            @ApiResponse(responseCode = "504", description = "AEMET request timed out",
                    content = @Content(mediaType = "application/problem+json")),
            @ApiResponse(responseCode = "502", description = "AEMET returned an invalid response or rejected credentials",
                    content = @Content(mediaType = "application/problem+json"))
    })
    public List<MunicipalityResponse> search(
            @Parameter(description = "Search term for municipality name. Case and accent insensitive. Omit to return all.")
            @RequestParam(required = false) String name) {

        List<Municipality> result = searchMunicipalitiesUseCase.searchByName(name);

        return result.stream()
                .map(m -> new MunicipalityResponse(m.id(), m.name()))
                .collect(Collectors.toList());
    }
}