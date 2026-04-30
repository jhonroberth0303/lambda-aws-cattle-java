# Sistema Genérico: Event.java + EventBuilder.java

## Objetivo

Documentar el modelo `Event` y su `EventBuilder` tal como existen hoy en el código, separando la estructura persistible confirmada de los detalles operativos que todavía requieren evidencia adicional.

## Evidencia revisada

- `src/main/java/com/cattle/entities/Event.java`
- `src/main/java/com/cattle/builders/EventBuilder.java`
- `src/test/java/com/cattle/entities/EventTest.java`
- `src/test/java/com/cattle/builders/EventBuilderTest.java`

## Qué es `Event`

`Event` es un `@DynamoDbBean` pensado para representar eventos persistibles de actividad o auditoría.

Campos confirmados:

- `pk`
- `sk`
- `gsi1pk`
- `gsi1sk`
- `eventType`
- `eventAt`
- `animals`
- `residualCm`
- `user`

También define el índice secundario `gsi1-type-date` mediante anotaciones del Enhanced Client.

## Qué hace `EventBuilder`

`EventBuilder` encapsula la construcción del evento y aplica validaciones mínimas:

- exige `pk`
- exige `sk`
- exige `eventType`
- exige `eventAt`
- completa `gsi1pk` y `gsi1sk` si no se especifican
- provee el helper `defaultsForGrazingEnd()`

## Uso conceptual

Este patrón sirve cuando el dominio necesita un objeto persistible de histórico o auditoría separado del mecanismo de transición de estado.

Ejemplo de construcción:

```java
Event event = new EventBuilder()
    .pk("farm#F001#pasture#P-01")
    .sk("eventAt#2025-09-24T10:00:00Z#GRAZING_END")
    .eventType("GRAZING_END")
    .eventAt("2025-09-24T10:00:00Z")
    .animals(15)
    .residualCm(8)
    .user("juan.perez@farm.com")
    .defaultsForGrazingEnd()
    .build();
```

## Qué sí está confirmado

- existe el modelo persistible
- existe el builder con validaciones
- existe soporte para una clave principal compuesta y un GSI por tipo y fecha

## Qué no queda confirmado solo con este documento

- la tabla concreta donde se guardan estos eventos en todos los ambientes
- el repositorio que los persiste de forma consistente
- el endpoint o caso de uso que dispara su persistencia en cada dominio

Por eso no debe asumirse automáticamente una `TABLE_EVENTS` operativa solo porque el modelo existe.

## Relación con otros patrones

- `PastureEvent` expresa intención de cambio de estado.
- `EntityPatch` expresa el delta a aplicar.
- `Event` expresa un artefacto persistible para histórico cuando el dominio lo necesita.

## Referencias

- [Visión general de eventos](./events-overview.md)
- [Patrón EntityPatch](./entity-patch-pattern.md)
- [Arquitectura de eventos de pastures](../../modelo-negocio/pastures/events-architecture.md)