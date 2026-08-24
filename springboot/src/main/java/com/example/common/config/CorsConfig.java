package com.example.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS configuration.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 1 Allow origins.
        corsConfiguration.addAllowedHeader("*"); // 2 Allow request headers.
        corsConfiguration.addAllowedMethod("*"); // 3 Allow HTTP methods.
        source.registerCorsConfiguration("/**", corsConfiguration); // 4 Register CORS rules for all endpoints.
        return new CorsFilter(source);
    }
}