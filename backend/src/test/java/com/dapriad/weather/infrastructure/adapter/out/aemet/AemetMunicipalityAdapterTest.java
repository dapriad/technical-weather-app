package com.dapriad.weather.infrastructure.adapter.out.aemet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dapriad.weather.application.exception.WeatherProviderException;
import com.dapriad.weather.domain.model.Municipality;
import com.dapriad.weather.infrastructure.adapter.out.aemet.dto.AemetMunicipalityDto;

class AemetMunicipalityAdapterTest {

    @Test
    void shouldMapAemetDtoToDomain() {
        var mockClient = mock(AemetMunicipalityClient.class);
        when(mockClient.fetchMunicipalities()).thenReturn(
            List.of(
                new AemetMunicipalityDto("id28079", "Madrid", "Madrid"),
                new AemetMunicipalityDto(null, "Test", null)
            )
        );
        var adapter = new AemetMunicipalityAdapter(mockClient);

        List<Municipality> result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("28079");
        assertThat(result.get(0).name()).isEqualTo("Madrid");
        assertThat(result.get(1).id()).isNull();
        assertThat(result.get(1).name()).isEqualTo("Test");
    }

    @Test
    void shouldReturnEmptyListWhenAemetReturnsEmpty() {
        var mockClient = mock(AemetMunicipalityClient.class);
        when(mockClient.fetchMunicipalities()).thenReturn(Collections.emptyList());
        var adapter = new AemetMunicipalityAdapter(mockClient);

        List<Municipality> result = adapter.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldTranslateMissingKeyException() {
        var mockClient = mock(AemetMunicipalityClient.class);
        when(mockClient.fetchMunicipalities()).thenThrow(
            new AemetClientException(AemetClientException.Reason.MISSING_API_KEY, "Key is missing")
        );
        var adapter = new AemetMunicipalityAdapter(mockClient);

        assertThatThrownBy(() -> adapter.findAll())
            .isInstanceOf(WeatherProviderException.class)
            .hasFieldOrPropertyWithValue("reason", WeatherProviderException.Reason.NOT_CONFIGURED);
    }

    @Test
    void shouldTranslateUnauthorizedException() {
        var mockClient = mock(AemetMunicipalityClient.class);
        when(mockClient.fetchMunicipalities()).thenThrow(
            new AemetClientException(AemetClientException.Reason.UNAUTHORIZED, "AEMET rejected the API key")
        );
        var adapter = new AemetMunicipalityAdapter(mockClient);

        assertThatThrownBy(() -> adapter.findAll())
            .isInstanceOf(WeatherProviderException.class)
            .hasFieldOrPropertyWithValue("reason", WeatherProviderException.Reason.UNAUTHORIZED);
    }

    @Test
    void shouldTranslateNotFoundException() {
        var mockClient = mock(AemetMunicipalityClient.class);
        when(mockClient.fetchMunicipalities()).thenThrow(
            new AemetClientException(AemetClientException.Reason.NOT_FOUND, "AEMET resource not found")
        );
        var adapter = new AemetMunicipalityAdapter(mockClient);

        assertThatThrownBy(() -> adapter.findAll())
            .isInstanceOf(WeatherProviderException.class)
            .hasFieldOrPropertyWithValue("reason", WeatherProviderException.Reason.NOT_FOUND);
    }

    @Test
    void shouldTranslateRateLimitedException() {
        var mockClient = mock(AemetMunicipalityClient.class);
        when(mockClient.fetchMunicipalities()).thenThrow(
            new AemetClientException(AemetClientException.Reason.RATE_LIMITED, "AEMET rate limit exceeded")
        );
        var adapter = new AemetMunicipalityAdapter(mockClient);

        assertThatThrownBy(() -> adapter.findAll())
            .isInstanceOf(WeatherProviderException.class)
            .hasFieldOrPropertyWithValue("reason", WeatherProviderException.Reason.RATE_LIMITED);
    }

    @Test
    void shouldTranslateTimeoutException() {
        var mockClient = mock(AemetMunicipalityClient.class);
        when(mockClient.fetchMunicipalities()).thenThrow(
            new AemetClientException(AemetClientException.Reason.TIMEOUT, "AEMET request timed out")
        );
        var adapter = new AemetMunicipalityAdapter(mockClient);

        assertThatThrownBy(() -> adapter.findAll())
            .isInstanceOf(WeatherProviderException.class)
            .hasFieldOrPropertyWithValue("reason", WeatherProviderException.Reason.TIMEOUT);
    }

    @Test
    void shouldTranslateInvalidResponseException() {
        var mockClient = mock(AemetMunicipalityClient.class);
        when(mockClient.fetchMunicipalities()).thenThrow(
            new AemetClientException(AemetClientException.Reason.PARSING_ERROR, "Failed to parse response")
        );
        var adapter = new AemetMunicipalityAdapter(mockClient);

        assertThatThrownBy(() -> adapter.findAll())
            .isInstanceOf(WeatherProviderException.class)
            .hasFieldOrPropertyWithValue("reason", WeatherProviderException.Reason.INVALID_RESPONSE);
    }
}