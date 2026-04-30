# PASTURES-HU-017: Documentación OpenAPI y Swagger

**ID**: PASTURES-HU-017  
**Tipo**: Estimación / documentación técnica  
**Estado actual**: Parcialmente implementada y revisada - Abril 2026

## Objetivo

Mantener documentación interactiva y trazable de la API del backend para facilitar consumo, revisión y validación de contratos.

## Evidencia confirmada en código

- existe dependencia `org.springdoc:springdoc-openapi-starter-webmvc-ui` en `build.gradle`
- existe `src/main/java/com/cattle/config/OpenApiConfig.java`
- existe configuración SpringDoc en `src/main/resources/application.properties`
- la seguridad permite acceso público a `/swagger-ui/**`, `/swagger-ui.html` y `/v3/api-docs`
- `PastureController` ya usa anotaciones OpenAPI como `@Operation`, `@ApiResponse` y `@Tag`

## Corrección documental importante

La versión anterior mezclaba una propuesta de implementación con el estado actual del proyecto. En particular:

- hablaba de editar `pom.xml`, pero el proyecto usa Gradle
- presentaba varios endpoints de pastures como si ya estuvieran implementados y documentados
- acoplaba esta HU a otras HUs no confirmadas como si formaran un bloque ya existente

## Qué sí está vigente hoy

- SpringDoc está integrado
- Swagger UI está configurado en `/swagger-ui.html`
- OpenAPI JSON está configurado en `/v3/api-docs`
- existe una base real de documentación anotada en controladores actuales

## Qué sigue abierto o requiere matiz

- no todos los endpoints históricos mencionados en esta HU están presentes en el backend actual
- la cobertura documental debe evaluarse controlador por controlador
- cualquier lista de endpoints documentados debe contrastarse con los controladores reales vigentes

## Uso recomendado

Tomar esta HU como artefacto de mejora continua de documentación, no como checklist ya cerrado. Si se quiere medir avance real, conviene revisar:

1. `OpenApiConfig.java`
2. `application.properties`
3. controladores anotados actualmente
4. acceso efectivo a `/swagger-ui.html` y `/v3/api-docs`

## Referencias vigentes

- `docs/arquitectura/architecture-cattle-lambda-function.md`
- `README.md`
- `src/main/java/com/cattle/config/OpenApiConfig.java`