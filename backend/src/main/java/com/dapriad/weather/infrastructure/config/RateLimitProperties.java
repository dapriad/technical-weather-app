package com.dapriad.weather.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private int apiRequestsPerMinute = 20;
    private int apiBurst = 5;
    private int globalRequestsPerMinute = 60;
    private int globalBurst = 20;
    private int swaggerRequestsPerMinute = 60;
    private int swaggerBurst = 20;
    private int globalSwaggerRequestsPerMinute = 120;
    private int globalSwaggerBurst = 40;
    private boolean trustForwardedHeaders = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getApiRequestsPerMinute() {
        return apiRequestsPerMinute;
    }

    public void setApiRequestsPerMinute(int apiRequestsPerMinute) {
        this.apiRequestsPerMinute = apiRequestsPerMinute;
    }

    public int getApiBurst() {
        return apiBurst;
    }

    public void setApiBurst(int apiBurst) {
        this.apiBurst = apiBurst;
    }

    public int getGlobalRequestsPerMinute() {
        return globalRequestsPerMinute;
    }

    public void setGlobalRequestsPerMinute(int globalRequestsPerMinute) {
        this.globalRequestsPerMinute = globalRequestsPerMinute;
    }

    public int getGlobalBurst() {
        return globalBurst;
    }

    public void setGlobalBurst(int globalBurst) {
        this.globalBurst = globalBurst;
    }

    public int getSwaggerRequestsPerMinute() {
        return swaggerRequestsPerMinute;
    }

    public void setSwaggerRequestsPerMinute(int swaggerRequestsPerMinute) {
        this.swaggerRequestsPerMinute = swaggerRequestsPerMinute;
    }

    public int getSwaggerBurst() {
        return swaggerBurst;
    }

    public void setSwaggerBurst(int swaggerBurst) {
        this.swaggerBurst = swaggerBurst;
    }

    public int getGlobalSwaggerRequestsPerMinute() {
        return globalSwaggerRequestsPerMinute;
    }

    public void setGlobalSwaggerRequestsPerMinute(int globalSwaggerRequestsPerMinute) {
        this.globalSwaggerRequestsPerMinute = globalSwaggerRequestsPerMinute;
    }

    public int getGlobalSwaggerBurst() {
        return globalSwaggerBurst;
    }

    public void setGlobalSwaggerBurst(int globalSwaggerBurst) {
        this.globalSwaggerBurst = globalSwaggerBurst;
    }

    public boolean isTrustForwardedHeaders() {
        return trustForwardedHeaders;
    }

    public void setTrustForwardedHeaders(boolean trustForwardedHeaders) {
        this.trustForwardedHeaders = trustForwardedHeaders;
    }
}
