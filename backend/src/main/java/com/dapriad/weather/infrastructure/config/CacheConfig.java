package com.dapriad.weather.infrastructure.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    @Bean
    CacheManager cacheManager(CacheProperties properties) {
        CaffeineCache municipalities = new CaffeineCache("municipalities",
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(properties.getMunicipalitiesTtl())
                        .recordStats()
                        .build());

        CaffeineCache forecasts = new CaffeineCache("forecasts",
                Caffeine.newBuilder()
                        .maximumSize(properties.getForecastsMaxSize())
                        .expireAfterWrite(properties.getForecastsTtl())
                        .recordStats()
                        .build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(municipalities, forecasts));
        return manager;
    }
}
