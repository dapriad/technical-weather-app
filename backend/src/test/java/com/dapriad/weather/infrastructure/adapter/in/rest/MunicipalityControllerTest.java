package com.dapriad.weather.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dapriad.weather.application.port.in.SearchMunicipalitiesUseCase;
import com.dapriad.weather.domain.model.Municipality;

@SpringBootTest
@AutoConfigureMockMvc
class MunicipalityControllerTest {

    @MockitoBean
    private SearchMunicipalitiesUseCase searchMunicipalitiesUseCase;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnMunicipalities() throws Exception {
        when(searchMunicipalitiesUseCase.searchByName("Madrid"))
                .thenReturn(List.of(
                        new Municipality("28079", "Madrid", "Madrid"),
                        new Municipality("28077", "Madridino", "Madrid")
                ));

        mockMvc.perform(get("/api/municipalities").param("name", "Madrid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("28079"))
                .andExpect(jsonPath("$[0].name").value("Madrid"));
    }

    @Test
    void shouldReturnEmptyWhenNoResults() throws Exception {
        when(searchMunicipalitiesUseCase.searchByName("NoExist"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/municipalities").param("name", "NoExist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnAllWhenNameIsNull() throws Exception {
        when(searchMunicipalitiesUseCase.searchByName(null))
                .thenReturn(List.of(
                        new Municipality("28079", "Madrid", "Madrid")
                ));

        mockMvc.perform(get("/api/municipalities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturn503WhenProviderFails() throws Exception {
        when(searchMunicipalitiesUseCase.searchByName("Madrid"))
                .thenThrow(new com.dapriad.weather.application.exception.MunicipalityProviderException(
                        com.dapriad.weather.application.exception.MunicipalityProviderException.Reason.UNAVAILABLE,
                        "No data available"));

        mockMvc.perform(get("/api/municipalities").param("name", "Madrid"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("UNAVAILABLE"));
    }

    @Test
    void shouldReturn502ForUnauthorized() throws Exception {
        when(searchMunicipalitiesUseCase.searchByName("Madrid"))
                .thenThrow(new com.dapriad.weather.application.exception.MunicipalityProviderException(
                        com.dapriad.weather.application.exception.MunicipalityProviderException.Reason.UNAUTHORIZED,
                        "Bad credentials"));

        mockMvc.perform(get("/api/municipalities").param("name", "Madrid"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }
}