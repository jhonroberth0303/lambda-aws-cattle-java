package com.cattle.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración general de la aplicación, antes leída vía System.getenv("APP_TIMEZONE")
 * disperso en varios servicios.
 */
@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private String timezone = "America/Bogota";
}
