# Patrón EntityPatch: Cambios Parciales de Estado

## Objetivo

Documentar el uso actual de `EntityPatch` y `PatchApplier` como mecanismo para expresar y aplicar cambios incrementales sobre entidades del dominio de potreros.

## Evidencia revisada

- `src/main/java/com/cattle/events/EntityPatch.java`
- `src/main/java/com/cattle/events/PatchApplier.java`
- `src/main/java/com/cattle/utils/PastureStatusEngine.java`

## Qué es `EntityPatch`

`EntityPatch` es un `record` con dos colecciones:

- `set`: atributos y valores a establecer
- `remove`: atributos a eliminar

Además ofrece una API fluida:

- `EntityPatch.of()`
- `set(key, value)`
- `remove(key)`
- `isEmpty()`

## Qué hace `PatchApplier`

`PatchApplier.applyLocal(Pasture, EntityPatch)` aplica en memoria un patch sobre la entidad `Pasture`.

Campos tratados explícitamente en el código actual:

- `status`
- `substatus`
- `holdUntilIso`
- `lastUseAtIso`
- `gsi2pk`
- `gsi2sk`

También convierte tipos simples y enums para que el patch no dependa de setters directos en el motor de estados.

## Flujo confirmado

1. Un caso de uso construye un `PastureEvent`.
2. `PastureStatusEngine.applyEvent(...)` devuelve un `EntityPatch`.
3. `PatchApplier.applyLocal(...)` refleja esos cambios sobre `Pasture` en memoria.
4. La capa de persistencia del dominio puede usar ese delta para actualizar DynamoDB.

## Qué resuelve este patrón

- evita acoplar el motor de reglas a la mutación directa de la entidad
- hace visible el delta exacto que una regla produce
- facilita auditoría y razonamiento sobre cambios parciales
- permite separar cálculo de persistencia

## Límites actuales observados

- este patrón está claramente soportado en el dominio de potreros
- no implica por sí mismo la existencia de un controlador HTTP específico
- la forma exacta de persistir el patch depende del repositorio y del caso de uso que lo invoque

## Ejemplo conceptual

```java
EntityPatch patch = EntityPatch.of()
    .set("status", "EN_DESCANSO")
    .set("lastUseAtIso", "2026-01-09T12:00:00Z")
    .remove("gsi2pk");

PatchApplier.applyLocal(pasture, patch);
```

## Referencias

- [Visión general de eventos](./events-overview.md)
- [Patrón sealed interface para eventos de potreros](./sealed-interface-pattern.md)
- [Arquitectura de eventos de pastures](../../modelo-negocio/pastures/events-architecture.md)