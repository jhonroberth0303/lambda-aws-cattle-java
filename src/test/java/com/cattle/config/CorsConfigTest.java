package com.cattle.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CorsConfig
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Config
 */
@Tag("unit")
@Tag("config")
class CorsConfigTest {

    @Test
    void corsConfigurer_returnsWebMvcConfigurer() {
        // Arrange
        CorsConfig corsConfig = new CorsConfig();

        // Act
        WebMvcConfigurer configurer = corsConfig.corsConfigurer();

        // Assert
        assertNotNull(configurer);
    }

    @Test
    void corsConfigurer_addCorsMappings_configuresAllPaths() {
        // Arrange
        CorsConfig corsConfig = new CorsConfig();
        WebMvcConfigurer configurer = corsConfig.corsConfigurer();
        CorsRegistry registry = mock(CorsRegistry.class, RETURNS_DEEP_STUBS);

        // Act
        configurer.addCorsMappings(registry);

        // Assert
        verify(registry).addMapping("/**");
    }

    @Test
    void corsConfigurer_isNotNull() {
        // Arrange
        CorsConfig corsConfig = new CorsConfig();

        // Act
        WebMvcConfigurer result = corsConfig.corsConfigurer();

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof WebMvcConfigurer);
    }
}
