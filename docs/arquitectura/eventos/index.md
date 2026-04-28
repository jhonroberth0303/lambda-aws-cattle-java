# Índice de Eventos - lambda-aws-cattle-java

Este directorio documenta el sistema de eventos y patches usado principalmente en el dominio de potreros, además de artefactos de soporte relacionados con eventos persistibles.

## Documento de entrada recomendado

- [Visión general de eventos](./events-overview.md)

## Artefactos del directorio

- `events-overview.md`: panorama actual del sistema de eventos con distinción entre lo confirmado y los gaps.
- `sealed-interface-pattern.md`: detalle del patrón `PastureEvent` y eventos type-safe.
- `generic-events-builder.md`: detalle del modelo `Event` y `EventBuilder` persistibles.
- `entity-patch-pattern.md`: patrón de cambios parciales con `EntityPatch` y `PatchApplier`.

## Lo confirmado en código

- Existe `PastureEvent` como `sealed interface` con `OpenEvent`, `CloseEvent`, `MaintenanceSetEvent` y `MaintenanceClearEvent`.
- Existe `EntityPatch` como record mutable por conveniencia de construcción y `PatchApplier` para aplicar cambios en memoria sobre `Pasture`.
- Existe `Event` como entidad DynamoDB y `EventBuilder` como builder explícito.

## Precaución de lectura

No todos los documentos de detalle prueban por sí solos que exista hoy una infraestructura completa de persistencia o publicación para cada evento descrito. Cuando una decisión dependa de tablas, endpoints o pipelines concretos, debe contrastarse además con el código y la configuración del backend.