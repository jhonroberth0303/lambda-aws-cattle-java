package com.cattle.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API REST.
 * Acceso a la documentación:
 * - Swagger UI: /swagger-ui.html
 * - OpenAPI JSON: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI cattleOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Servidor actual"),
                        new Server()
                                .url("https://44xpamzadd.execute-api.us-east-1.amazonaws.com/Prod")
                                .description("AWS Lambda - Desarrollo")
                ))
                .tags(List.of(
                        new Tag()
                                .name("Bovinos")
                                .description("Operaciones de gestión de bovinos (vacas, toros, terneros)"),
                        new Tag()
                                .name("Ordeño")
                                .description("Registro y consulta de datos de ordeño"),
                        new Tag()
                                .name("Potreros")
                                .description("Gestión de potreros y rotación de pasturas"),
                        new Tag()
                                .name("Chatbot")
                                .description("Asistente virtual con IA (Amazon Bedrock)"),
                        new Tag()
                                .name("Health")
                                .description("Endpoints de verificación de salud")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Cattle Management API")
                .description("""
                        API REST para gestión integral de ganado bovino.
                        
                        ## Funcionalidades principales:
                        - **Bovinos**: CRUD completo de animales
                        - **Ordeño**: Registro de producción de leche
                        - **Potreros**: Gestión de rotación y estado de pasturas
                        - **Chatbot IA**: Asistente inteligente con Amazon Bedrock
                        
                        ## Autenticación
                        Próximamente: JWT Bearer Token (HU-BEDROCK-003)
                        """)
                .version(appVersion)
                .contact(new Contact()
                        .name("Equipo Cattle")
                        .email("soporte@cattle.com")
                        .url("https://github.com/cattle-project"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
