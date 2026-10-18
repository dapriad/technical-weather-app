package com.dapriad.weather.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.dapriad.weather.application.port.in.SearchMunicipalitiesUseCase;
import com.dapriad.weather.domain.model.Municipality;
import com.dapriad.weather.domain.port.MunicipalityProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SearchMunicipalitiesServiceTest {

    private SearchMunicipalitiesService service;

    @Mock
    private MunicipalityProvider municipalityProvider;

    @BeforeEach
    void setUp() {
        service = new SearchMunicipalitiesService(municipalityProvider);
    }

    @Test
    void shouldReturnAllWhenNameIsNull() {
        List<Municipality> mockList = List.of(
            new Municipality("28079", "Madrid", "Madrid"),
            new Municipality("07040", "Palma", "Illes Balears")
        );
        org.mockito.Mockito.when(municipalityProvider.findAll()).thenReturn(mockList);

        List<Municipality> result = service.searchByName(null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Madrid");
    }

    @Test
    void shouldReturnAllWhenNameIsEmpty() {
        List<Municipality> mockList = List.of(
            new Municipality("28079", "Madrid", "Madrid")
        );
        org.mockito.Mockito.when(municipalityProvider.findAll()).thenReturn(mockList);

        List<Municipality> result = service.searchByName("  ");

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldSearchCaseInsensitive() {
        List<Municipality> mockList = List.of(
            new Municipality("28079", "Madrid", "Madrid"),
            new Municipality("15012", "A Coruña", "A Coruña"),
            new Municipality("07040", "Palma", "Illes Balears")
        );
        org.mockito.Mockito.when(municipalityProvider.findAll()).thenReturn(mockList);

        List<Municipality> result = service.searchByName("madrid");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Madrid");
    }

    @Test
    void shouldSearchAccentInsensitive() {
        List<Municipality> mockList = List.of(
            new Municipality("29067", "Málaga", "Málaga"),
            new Municipality("28079", "Madrid", "Madrid"),
            new Municipality("11001", "Cádiz", "Cádiz")
        );
        org.mockito.Mockito.when(municipalityProvider.findAll()).thenReturn(mockList);

        List<Municipality> result = service.searchByName("malaga");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Málaga");
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        List<Municipality> mockList = List.of(
            new Municipality("28079", "Madrid", "Madrid")
        );
        org.mockito.Mockito.when(municipalityProvider.findAll()).thenReturn(mockList);

        List<Municipality> result = service.searchByName("Zaragoza");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSearchSubstringInName() {
        List<Municipality> mockList = List.of(
            new Municipality("28079", "Madrid", "Madrid"),
            new Municipality("47025", "Villadolid", "Valladolid"),
            new Municipality("07040", "Palma de Mallorca", "Illes Balears")
        );
        org.mockito.Mockito.when(municipalityProvider.findAll()).thenReturn(mockList);

        List<Municipality> result = service.searchByName("palma");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Palma de Mallorca");
    }
}