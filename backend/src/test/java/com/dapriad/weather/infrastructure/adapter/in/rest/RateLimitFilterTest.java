package com.dapriad.weather.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.dapriad.weather.infrastructure.config.RateLimitProperties;

class RateLimitFilterTest {

    private RateLimitProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setApiRequestsPerMinute(2);
        properties.setApiBurst(2);
        properties.setGlobalRequestsPerMinute(100);
        properties.setGlobalBurst(100);
        properties.setSwaggerRequestsPerMinute(10);
        properties.setSwaggerBurst(10);
        properties.setGlobalSwaggerRequestsPerMinute(100);
        properties.setGlobalSwaggerBurst(100);
    }

    @Test
    void shouldRejectRequestsAfterPerIpLimit() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties);

        assertThat(call(filter, "/api/municipalities", "1.2.3.4")).isEqualTo(200);
        assertThat(call(filter, "/api/municipalities", "1.2.3.4")).isEqualTo(200);

        MockHttpServletResponse response = callWithResponse(filter, "/api/municipalities", "1.2.3.4");

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("60");
    }

    @Test
    void shouldNotLimitHealthEndpoint() throws Exception {
        properties.setApiRequestsPerMinute(0);
        properties.setApiBurst(0);
        RateLimitFilter filter = new RateLimitFilter(properties);

        assertThat(call(filter, "/api/health", "1.2.3.4")).isEqualTo(200);
        assertThat(call(filter, "/api/health", "1.2.3.4")).isEqualTo(200);
    }

    @Test
    void shouldUseFirstForwardedForAddress() throws Exception {
        properties.setApiRequestsPerMinute(1);
        properties.setApiBurst(1);
        RateLimitFilter filter = new RateLimitFilter(properties);

        assertThat(call(filter, "/api/municipalities", "10.0.0.1", "9.9.9.9, 10.0.0.2")).isEqualTo(200);
        assertThat(call(filter, "/api/municipalities", "10.0.0.1", "9.9.9.9, 10.0.0.2")).isEqualTo(429);
        assertThat(call(filter, "/api/municipalities", "10.0.0.1", "8.8.8.8")).isEqualTo(200);
    }

    @Test
    void shouldApplyGlobalLimit() throws Exception {
        properties.setApiRequestsPerMinute(100);
        properties.setApiBurst(100);
        properties.setGlobalRequestsPerMinute(1);
        properties.setGlobalBurst(1);
        RateLimitFilter filter = new RateLimitFilter(properties);

        assertThat(call(filter, "/api/municipalities", "1.2.3.4")).isEqualTo(200);
        assertThat(call(filter, "/api/municipalities", "5.6.7.8")).isEqualTo(429);
    }

    @Test
    void shouldLimitSwaggerSeparately() throws Exception {
        properties.setSwaggerRequestsPerMinute(1);
        properties.setSwaggerBurst(1);
        RateLimitFilter filter = new RateLimitFilter(properties);

        assertThat(call(filter, "/swagger-ui/index.html", "1.2.3.4")).isEqualTo(200);
        assertThat(call(filter, "/swagger-ui/index.html", "1.2.3.4")).isEqualTo(429);
        assertThat(call(filter, "/api/municipalities", "1.2.3.4")).isEqualTo(200);
    }

    private int call(RateLimitFilter filter, String path, String remoteAddress) throws Exception {
        return callWithResponse(filter, path, remoteAddress).getStatus();
    }

    private int call(RateLimitFilter filter, String path, String remoteAddress, String forwardedFor) throws Exception {
        return callWithResponse(filter, path, remoteAddress, forwardedFor).getStatus();
    }

    private MockHttpServletResponse callWithResponse(
            RateLimitFilter filter,
            String path,
            String remoteAddress) throws Exception {
        return callWithResponse(filter, path, remoteAddress, null);
    }

    private MockHttpServletResponse callWithResponse(
            RateLimitFilter filter,
            String path,
            String remoteAddress,
            String forwardedFor) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setRemoteAddr(remoteAddress);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
