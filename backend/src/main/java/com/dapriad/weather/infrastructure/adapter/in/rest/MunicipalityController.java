package com.dapriad.weather.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.dapriad.weather.application.port.in.SearchMunicipalitiesUseCase;
import com.dapriad.weather.domain.model.Municipality;
import com.dapriad.weather.infrastructure.adapter.in.rest.dto.MunicipalityResponse;

@RestController
@RequestMapping("/api/municipalities")
@Tag(name = "Municipalities", description = "Municipality search operations")
public class MunicipalityController {

    private final SearchMunicipalitiesUseCase searchMunicipalitiesUseCase;

    public MunicipalityController(SearchMunicipalitiesUseCase searchMunicipalitiesUseCase) {
        this.searchMunicipalitiesUseCase = searchMunicipalitiesUseCase;
    }

    @GetMapping
    @Operation(summary = "Search municipalities by name", description =
            "Returns municipality records matching the given name. " +
            "Search is case and accent insensitive. If name is omitted, returns the full list.")
    public List<MunicipalityResponse> search(
            @Parameter(description = "Search term for municipality name")
            @RequestParam(required = false) String name) {

        List<Municipality> result = searchMunicipalitiesUseCase.searchByName(name);

        return result.stream()
                .map(m -> new MunicipalityResponse(m.id(), m.name()))
                .collect(Collectors.toList());
    }
}