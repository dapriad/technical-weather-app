package com.dapriad.weather.infrastructure.adapter.in.rest;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dapriad.weather.infrastructure.config.RateLimitProperties;
import com.dapriad.weather.shared.ratelimit.TokenBucket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int RETRY_AFTER_SECONDS = 60;

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Cache<String, TokenBucket> apiBuckets;
    private final Cache<String, TokenBucket> swaggerBuckets;
    private final TokenBucket globalApiBucket;
    private final TokenBucket globalSwaggerBucket;

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.apiBuckets = newBucketCache();
        this.swaggerBuckets = newBucketCache();
        this.globalApiBucket = new TokenBucket(
                properties.getGlobalBurst(),
                properties.getGlobalRequestsPerMinute());
        this.globalSwaggerBucket = new TokenBucket(
                properties.getGlobalSwaggerBurst(),
                properties.getGlobalSwaggerRequestsPerMinute());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (!properties.isEnabled() || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (isHealth(path)) {
            return true;
        }

        return !isApi(path) && !isSwagger(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean swagger = isSwagger(path);
        int requestsPerMinute = swagger
                ? properties.getSwaggerRequestsPerMinute()
                : properties.getApiRequestsPerMinute();
        int burst = swagger ? properties.getSwaggerBurst() : properties.getApiBurst();
        TokenBucket globalBucket = swagger ? globalSwaggerBucket : globalApiBucket;
        Cache<String, TokenBucket> buckets = swagger ? swaggerBuckets : apiBuckets;

        String clientIp = resolveClientIp(request);
        String key = (swagger ? "swagger:" : "api:") + clientIp;
        TokenBucket clientBucket = buckets.get(key,
                ignored -> new TokenBucket(burst, requestsPerMinute));

        if (clientBucket.tryConsume() && globalBucket.tryConsume()) {
            filterChain.doFilter(request, response);
            return;
        }

        writeTooManyRequests(response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (properties.isTrustForwardedHeaders()) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(forwardedFor)) {
                return forwardedFor.split(",")[0].trim();
            }

            String realIp = request.getHeader("X-Real-IP");
            if (StringUtils.hasText(realIp)) {
                return realIp.trim();
            }
        }

        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(RETRY_AFTER_SECONDS));

        Map<String, Object> problem = Map.of(
                "type", "about:blank",
                "title", "Too many requests",
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "detail", "Rate limit exceeded. Please try again later.",
                "code", "TOO_MANY_REQUESTS");

        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    private Cache<String, TokenBucket> newBucketCache() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .build();
    }

    private boolean isApi(String path) {
        return path.startsWith("/api/");
    }

    private boolean isSwagger(String path) {
        return path.equals("/v3/api-docs");
    }

    private boolean isHealth(String path) {
        return path.equals("/api/health");
    }
}
