package com.cattle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing).
 * Los orígenes permitidos se configuran por ambiente para mayor seguridad.
 * 
 * Configuración:
 * - Desarrollo: cors.allowed-origins=http://localhost:3000,http://localhost:5173
 * - Producción: Usar variable de entorno CORS_ALLOWED_ORIGINS
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,https://ambitious-tree-021c81b10.4.azurestaticapps.net/}")
    private String allowedOrigins;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(parseOrigins(allowedOrigins))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization", "Content-Type", "X-Request-Id")
                        .allowCredentials(true)
                        .maxAge(maxAge);
            }
        };
    }

    /**
     * Parsea los orígenes permitidos desde la configuración.
     * Soporta múltiples orígenes separados por coma.
     */
    private String[] parseOrigins(String origins) {
        if (origins == null || origins.isBlank()) {
            return new String[]{"http://localhost:3000"};
        }
        return origins.split(",");
    }
}