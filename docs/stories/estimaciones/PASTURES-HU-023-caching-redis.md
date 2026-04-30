# PASTURES-HU-023: Caching Redis o Local

**ID**: PASTURES-HU-023  
**Tipo**: Estimación / backlog de performance  
**Estado actual**: Revisión documental parcial - Abril 2026

## Objetivo

Evaluar una capa de caché para reducir latencia y carga de lectura en consultas frecuentes del backend.

## Evidencia confirmada hoy en el repositorio

- no se confirmó dependencia Redis en `build.gradle`
- no se confirmaron anotaciones como `@Cacheable` o `@CacheEvict` en el código actual
- no se observó configuración de caché en `application.properties`

## Lectura vigente

Esta HU debe leerse como backlog opcional de performance, no como una capacidad implementada. La versión anterior describía Redis, caché local, TTL e invalidación como si fueran parte del sistema actual.

## Qué sí puede afirmarse

- la necesidad de caching puede aparecer más adelante si sube el volumen de lectura
- el backend todavía puede operar sin esta capa adicional
- la decisión tiene sentido como mejora post-lanzamiento y no como requisito base del estado actual

## Qué no debe afirmarse sin refinamiento nuevo

- que Redis esté integrado
- que exista invalidación automática de caché en escrituras
- que haya métricas reales de hit/miss expuestas
- que exista endpoint administrativo para limpiar caché

## Uso recomendado

Si esta HU vuelve a priorizarse:

1. medir primero el cuello de botella real
2. decidir si conviene caché local, Redis o ninguna capa adicional
3. documentar la estrategia de invalidación antes de implementarla

## Referencias vigentes

- `build.gradle`
- `src/main/resources/application.properties`
- `docs/arquitectura/architecture-cattle-lambda-function.md`