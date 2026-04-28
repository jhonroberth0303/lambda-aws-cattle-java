# PASTURES-HU-012: Historial de Eventos de Potreros

**ID**: PASTURES-HU-012  
**Tipo**: Estimación / backlog funcional  
**Estado actual**: Revisión documental parcial - Abril 2026

## Objetivo

Contar con una superficie de consulta para revisar historial de eventos de potreros, filtros temporales y trazabilidad operativa.

## Lectura vigente

Este documento no debe leerse como funcionalidad implementada y cerrada. Su versión previa describía un diseño detallado de API y clases que no quedaron confirmados en el código actual.

## Desalineaciones detectadas en la versión histórica

- describía un `PastureEventController`
- proponía `GET /farms/{farmId}/pastures/{pastureId}/events` como endpoint vigente
- asumía paginación, filtros y contrato de respuesta ya materializados
- trataba el circuito de historial como una capacidad operacional cerrada

## Evidencia confirmada hoy en el repositorio

- existe `PastureController` en `src/main/java/com/cattle/controller/PastureController.java`
- existe el dominio `PastureEvent` y sus variantes type-safe
- existe `Event` como modelo persistible
- existe `EventBuilder`
- la documentación arquitectónica de eventos marca explícitamente que la exposición HTTP y el pipeline completo de persistencia no quedan cerrados solo con la evidencia actual

## Qué sí puede afirmarse

- el dominio de eventos de potreros está presente en el código
- hay bases de modelado para historial o auditoría
- sigue teniendo sentido como backlog una consulta de historial por potrero

## Qué no debe afirmarse sin refinamiento nuevo

- que ya existe un controlador REST dedicado a historial de eventos
- que el endpoint y su paginación estén implementados
- que la persistencia de todos los eventos esté cerrada extremo a extremo

## Uso recomendado

Si esta HU vuelve a priorizarse:

1. verificar primero el flujo real de escritura de `Event`
2. decidir si el historial saldrá de eventos persistibles, de auditoría o de ambos
3. definir contrato HTTP a partir de evidencia nueva, no desde este texto histórico

## Referencias vigentes

- `docs/arquitectura/eventos/events-overview.md`
- `docs/arquitectura/eventos/generic-events-builder.md`
- `docs/arquitectura/architecture-cattle-lambda-function.md`