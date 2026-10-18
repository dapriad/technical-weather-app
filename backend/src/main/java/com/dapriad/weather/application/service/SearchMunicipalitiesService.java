package com.dapriad.weather.application.service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

import com.dapriad.weather.application.port.in.SearchMunicipalitiesUseCase;
import com.dapriad.weather.domain.model.Municipality;
import com.dapriad.weather.domain.port.MunicipalityProvider;

public class SearchMunicipalitiesService implements SearchMunicipalitiesUseCase {

    private final MunicipalityProvider municipalityProvider;

    public SearchMunicipalitiesService(MunicipalityProvider municipalityProvider) {
        this.municipalityProvider = municipalityProvider;
    }

    @Override
    public List<Municipality> searchByName(String name) {
        if (name == null) {
            return municipalityProvider.findAll();
        }
        String trimmed = StringUtils.trimWhitespace(name);
        if (trimmed.isBlank()) {
            return municipalityProvider.findAll();
        }
        String normalizedQuery = normalize(trimmed.toLowerCase(Locale.ROOT));
        List<Municipality> all = municipalityProvider.findAll();
        return all.stream()
                .filter(m -> normalize(m.name().toLowerCase(Locale.ROOT)).contains(normalizedQuery))
                .collect(Collectors.toList());
    }

    private String normalize(String value) {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ");
    }
}