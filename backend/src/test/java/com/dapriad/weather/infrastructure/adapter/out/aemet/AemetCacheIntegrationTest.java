package com.dapriad.weather.infrastructure.adapter.out.aemet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetForecastDto;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetMunicipalityDto;

@SpringBootTest
class AemetCacheIntegrationTest {

    @MockitoBean
    private AemetOpenDataClient dataClient;

    @Autowired
    private AemetMunicipalityClient municipalityClient;

    @Autowired
    private AemetForecastClient forecastClient;

    @Test
    void shouldCacheMunicipalityCatalog() {
        when(dataClient.<List<AemetMunicipalityDto>>fetchData(eq("/api/maestro/municipios"), any()))
                .thenReturn(List.of(new AemetMunicipalityDto("id28079", "Madrid", "Madrid")));

        municipalityClient.fetchMunicipalities();
        municipalityClient.fetchMunicipalities();

        verify(dataClient, times(1)).fetchData(eq("/api/maestro/municipios"), any());
    }

    @Test
    void shouldCacheForecastPerMunicipality() {
        when(dataClient.<List<AemetForecastDto>>fetchData(
                eq("/api/prediccion/especifica/municipio/diaria/28079"), any()))
                .thenReturn(List.of(new AemetForecastDto("Madrid", "Madrid", null)));

        forecastClient.fetchForecast("28079");
        forecastClient.fetchForecast("28079");

        verify(dataClient, times(1)).fetchData(
                eq("/api/prediccion/especifica/municipio/diaria/28079"), any());
    }
}
