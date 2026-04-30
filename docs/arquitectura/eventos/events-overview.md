# Visión General del Sistema de Eventos

## Objetivo

Este documento resume el estado actual del sistema de eventos observado en el código y aclara cómo conviven los dos patrones principales presentes en el backend.

## Evidencia revisada

- `src/main/java/com/cattle/events/PastureEvent.java`
- `src/main/java/com/cattle/events/OpenEvent.java`
- `src/main/java/com/cattle/events/CloseEvent.java`
- `src/main/java/com/cattle/events/MaintenanceSetEvent.java`
- `src/main/java/com/cattle/events/MaintenanceClearEvent.java`
- `src/main/java/com/cattle/events/EntityPatch.java`
- `src/main/java/com/cattle/events/PatchApplier.java`
- `src/main/java/com/cattle/utils/PastureStatusEngine.java`
- `src/main/java/com/cattle/entities/Event.java`
- `src/main/java/com/cattle/builders/EventBuilder.java`

## Patrón 1: eventos type-safe para transición de estado

### Componentes confirmados

- `PastureEvent`
- `OpenEvent`
- `CloseEvent`
- `MaintenanceSetEvent`
- `MaintenanceClearEvent`

### Propósito

Representar transiciones de negocio del dominio de potreros con tipado fuerte en tiempo de compilación.

### Consumo confirmado

`PastureStatusEngine.applyEvent(...)` procesa un `PastureEvent` y devuelve un `EntityPatch` con los cambios a aplicar y persistir.

### Resultado típico

- cambio de `status`
- cambio de `substatus`
- cambios de atributos como `holdUntil`, `lastUseAtIso`, `gsi2pk` o `gsi2sk`

## Patrón 2: eventos persistibles de actividad

### Componentes confirmados

- `com.cattle.entities.Event`
- `com.cattle.builders.EventBuilder`

### Propósito

Modelar eventos persistibles con claves DynamoDB, atributos de negocio y soporte para el índice secundario `gsi1-type-date`.

### Qué sí está confirmado

- `Event` es un `@DynamoDbBean` con `pk`, `sk`, `gsi1pk`, `gsi1sk`, `eventType`, `eventAt`, `animals`, `residualCm` y `user`.
- `EventBuilder` valida campos requeridos y construye instancias de `Event`.

### Qué no queda cerrado solo con esta evidencia

- la tabla exacta donde se persisten todos los eventos
- el flujo de repositorio o servicio que persiste sistemáticamente estos `Event`
- la exposición HTTP o el pipeline completo de auditoría asociado a cada evento de dominio

Por eso este patrón debe documentarse como presente en el código, pero no como circuito operacional completo salvo que se contraste con más evidencia.

## Patrón 3: patches de cambio parcial

### Componentes confirmados

- `EntityPatch`
- `PatchApplier`

### Propósito

Separar la decisión de negocio sobre qué cambia de la aplicación concreta de esos cambios sobre una entidad en memoria o persistencia.

### Flujo confirmado

1. Un `PastureEvent` entra a `PastureStatusEngine`.
2. El motor devuelve un `EntityPatch` con operaciones `set` y `remove`.
3. `PatchApplier.applyLocal(...)` traduce ese patch a mutaciones sobre `Pasture`.

## Relación entre los patrones

Los patrones no compiten; resuelven problemas distintos:

- `PastureEvent` expresa la intención de negocio.
- `EntityPatch` expresa el delta de estado.
- `Event` expresa un objeto persistible para historial o auditoría cuando corresponda.

## Riesgos y gaps documentales

- Parte de la documentación histórica presentó `Event` como si su infraestructura de persistencia estuviera completamente cerrada; eso debe confirmarse caso por caso.
- El dominio mejor soportado por evidencia dentro de `events/` es potreros.
- Si se quiere documentar un flujo operacional completo de auditoría, hace falta revisar repositorios, tablas y puntos de escritura efectivos.

## Lectura recomendada

1. Leer este overview.
2. Revisar `sealed-interface-pattern.md` para transición de estados de potreros.
3. Revisar `entity-patch-pattern.md` para el mecanismo de deltas.
4. Revisar `generic-events-builder.md` para el modelo persistible `Event`.