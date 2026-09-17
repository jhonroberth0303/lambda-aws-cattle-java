package com.cattle.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Centraliza los nombres de tabla DynamoDB, antes leídos vía System.getenv() disperso
 * en cada repositorio. Se alimenta de las variables de entorno TABLE_* mapeadas en
 * application.properties bajo el prefijo "tables".
 */
@Component
@ConfigurationProperties(prefix = "tables")
@Getter
@Setter
public class TablesConfig {
    private String bovines;
    private String pasture;
    private String farmMilking;
    private String counters;
    private String plan;
    private String events;
    private String siteSettings;
    private String notifications;
}
