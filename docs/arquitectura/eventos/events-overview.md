# 📌 Visión General: Sistemas de Eventos en Cattle

**Fecha**: 2026-01-09

## 🎯 Objetivo

Comparar y documentar los dos sistemas principales de eventos del proyecto Cattle, explicar cuándo usar cada uno, y proporcionar ejemplos prácticos.

---

## 📚 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Sistema 1: Event.java (Genérico)](#sistema-1-eventjava-genérico)
3. [Sistema 2: PastureEvent (Type-Safe)](#sistema-2-pastureevent-type-safe)
4. [Tabla Comparativa](#tabla-comparativa)
5. [Casos de Uso](#casos-de-uso)
6. [Recomendaciones](#recomendaciones)

---

## Introducción

El proyecto Cattle implementa **dos patrones de eventos** para satisfacer necesidades distintas:

1. **Event.java**: Registrar actividades (histórico, persistido).
2. **PastureEvent**: Cambiar estado de entidades (transaccional, transiente).

Ambos son **válidos y necesarios**, dependiendo del caso de uso.

---

## Sistema 1: Event.java (Genérico)

### 📋 Descripción

POJO Bean + Builder Pattern para registrar **eventos de actividades** que ocurren en la granja.

### 🗄️ Estructura

```java
package com.cattle.entities;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class Event {
    
    // ---- Claves ----
    private String pk;      // farm#F001#pasture#P-01
    private String sk;      // eventAt#2025-09-24T10:00:00Z#GRAZING_END
    
    // ---- GSI ----
    private String gsi1pk;  // farm#F001#type#GRAZING_END
    private String gsi1sk;  // 2025-09-24T10:00:00Z
    
    // ---- Atributos ----
    private String eventType;      // GRAZING_END, FEED_CHANGE, etc.
    private String eventAt;        // ISO timestamp
    private Integer animals;       // Cantidad de animales
    private Integer residualCm;    // Altura residual del pasto
    private String user;           // Usuario que registró
}
```

### 🔨 EventBuilder.java

```java
@Getter
@Setter
@Builder
public class EventBuilder {
    private String pk;
    private String sk;
    private String gsi1pk;
    private String gsi1sk;
    private String eventType;
    private String eventAt;
    private Integer animals;
    private Integer residualCm;
    private String user;
    private String lotId;
    
    public EventBuilder defaultsForGrazingEnd() {
        this.eventType = this.eventType != null ? this.eventType : "GRAZING_END";
        return this;
    }
    
    public Event build() {
        // Validaciones y defaults
        requireNonBlank(pk, "pk");
        requireNonBlank(eventType, "eventType");
        
        if (gsi1pk == null && eventType != null) {
            this.gsi1pk = "farm#UNKNOWN#type#" + eventType;
        }
        
        return Event.builder()
            .pk(pk).sk(sk)
            .gsi1pk(gsi1pk).gsi1sk(gsi1sk)
            .eventType(eventType)
            .eventAt(eventAt)
            .animals(animals)
            .residualCm(residualCm)
            .user(user)
            .build();
    }
}
```

### 📋 Patrón Builder

```java
// Crear evento usando builder
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

### 🔑 Claves DynamoDB

```
PK: farm#F001#pasture#P-01
SK: eventAt#2025-09-24T10:00:00Z#GRAZING_END

GSI1PK: farm#F001#type#GRAZING_END
GSI1SK: 2025-09-24T10:00:00Z
```

**Permite**:
- Query por finca y tipo: `farm#F001#type#*`
- Query por tipo y fecha: ordenado por timestamp

### 💾 Persistencia

Se guarda en tabla `TABLE_EVENTS` (o tabla específica de dominio).

### 📊 Caso de Uso Ejemplo

```
1. Usuario cierra potrero (CloseEvent en PastureEvent)
   ↓
2. Sistema registra evento GRAZING_END en Event
   ↓
3. Event se persiste en DynamoDB
   ↓
4. Genera histórico auditable de actividades
```

---

## Sistema 2: PastureEvent (Type-Safe)

### 📋 Descripción

Sealed interface + records para **cambiar estado** de potreros de manera type-safe.

### 🏗️ Jerarquía

```java
public sealed interface PastureEvent 
    permits OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent {
    
    EventType type();
    String user();
}
```

### 📌 4 Implementaciones

#### OpenEvent
```java
public record OpenEvent(
    String user,
    String lotId,
    Integer animals
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.OPEN; }
}
```

**Transición**: EN_DESCANSO/DISPONIBLE → EN_USO

#### CloseEvent
```java
public record CloseEvent(
    String user,
    String lotId,
    Integer animals,
    Integer residualCm
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.CLOSE; }
}
```

**Transición**: EN_USO → EN_DESCANSO

#### MaintenanceSetEvent
```java
public record MaintenanceSetEvent(
    String user,
    PastureSubstatus substatus,
    String holdUntil
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.MAINTENANCE_SET; }
}
```

**Transición**: Cualquiera → MANTENIMIENTO

#### MaintenanceClearEvent
```java
public record MaintenanceClearEvent(
    String user
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.MAINTENANCE_CLEAR; }
}
```

**Transición**: MANTENIMIENTO → EN_DESCANSO/DISPONIBLE

### 🔄 Consumo en StatusEngine

```java
public EntityPatch applyEvent(Pasture pasture, Plan plan, PastureEvent ev) {
    EntityPatch patch = EntityPatch.of();
    
    // Switch exhaustivo (compilador obliga manejar todos)
    switch (ev) {
        case OpenEvent e -> {
            // Validar, transicionar, retornar patch
        }
        case CloseEvent e -> {
            // ...
        }
        case MaintenanceSetEvent e -> {
            // ...
        }
        case MaintenanceClearEvent e -> {
            // ...
        }
    }
    
    return patch;
}
```

### ✨ Beneficios

- ✅ **Type-safe**: No hay strings arbitrarios.
- ✅ **Exhaustive checking**: Compilador obliga todos los casos.
- ✅ **Inmutable**: Records son final por defecto.
- ✅ **Pattern matching**: Soporta switch expression moderna.

---

## Tabla Comparativa

| Aspecto | Event.java | PastureEvent |
|---------|-----------|------------|
| **Ubicación** | `entities/` | `events/` |
| **Patrón** | POJO Bean | Sealed interface |
| **Constructor** | Builder | Direct (record) |
| **Persistible** | ✅ Sí (DynamoDB) | ❌ No (transiente) |
| **Mutabilidad** | ❌ Mutable (setters) | ✅ Immutable |
| **Type-Safety** | ❌ eventType es String | ✅ Sealed interface |
| **Validación** | Runtime | Compile-time |
| **Pattern Matching** | ❌ Difícil | ✅ Soporta switch |
| **Casos** | GRAZING_END, FEED_CHANGE, ... | OPEN, CLOSE, MAINTENANCE_* |
| **Consumidor** | ? (Repositorio histórico) | PastureStatusEngine |
| **Propósito** | Auditoría | Transaccional |

---

## Casos de Uso

### Cuándo Usar Event.java

**Escenarios**:
1. Registrar que un **evento importante ocurrió** (auditoría).
2. Datos debe **persistirse para siempre**.
3. Múltiples **eventos pueden ocurrir sin cambiar estado**.
4. Necesitas **histórico analizable** (reportes, trazabilidad).

**Ejemplos**:
- Registrar GRAZING_END cuando se cierra un potrero.
- Registrar FEED_CHANGE cuando se cambia alimentación.
- Registrar HEALTH_CHECK cuando se revisa un bovino.

### Cuándo Usar PastureEvent

**Escenarios**:
1. Necesitas **cambiar estado** de una entidad.
2. Quieres **garantías en compile-time** (type-safety).
3. Datos son **transientes** (no persisten aparte).
4. Usarás **pattern matching** (Java 17+).

**Ejemplos**:
- Abrir potrero (EN_DESCANSO → EN_USO).
- Cerrar potrero (EN_USO → EN_DESCANSO).
- Bloquear para mantenimiento (→ MANTENIMIENTO).

---

## Recomendaciones

### Arquitectura Propuesta

```
┌──────────────────────────────────────────────────────────┐
│  Usuario dispara acción en Frontend (abrir potrero)      │
└────────────────────┬─────────────────────────────────────┘
                     │
                     ├─► PastureEventController
                     │   (recibe evento)
                     │
                     ├─► PastureStatusEngine
                     │   (aplica cambio lógico)
                     │
                     └─► EntityPatch
                         (retorna cambios)
                         │
                         ├─► PastureRepository
                         │   (persiste en DB)
                         │
                         └─► EventBuilder
                             (registra actividad)
                             │
                             └─► Event (guarda histórico)
```

### Patrones Sugeridos

1. **PastureEvent** para cambios de estado.
2. **Event.java** para auditoría de cambios importantes.
3. **EntityPatch** para aplicar cambios.

### Validaciones

```java
// En PastureEventController
@PostMapping("/{pastureId}/events")
public ResponseEntity<?> applyEvent(
    @PathVariable String farmId,
    @PathVariable String pastureId,
    @RequestBody PastureEventRequest request) {
    
    // 1. Validar request
    validateEventRequest(request);
    
    // 2. Convertir a PastureEvent
    PastureEvent event = requestToPastureEvent(request);
    
    // 3. Aplicar evento (change de estado)
    EntityPatch patch = pastureService.applyEvent(farmId, pastureId, event);
    
    // 4. (Opcional) Registrar en Event para auditoría
    eventService.recordEvent(farmId, pastureId, "PASTURE_" + event.type(), ...);
    
    // 5. Retornar resultado
    return ResponseEntity.ok(pasture);
}
```

---

## 📚 Referencias

- [sealed-interface-pattern.md](sealed-interface-pattern.md): Profundización en sealed interfaces.
- [generic-events-builder.md](generic-events-builder.md): Detalles de Event.java.
- [entity-patch-pattern.md](entity-patch-pattern.md): Cómo funciona EntityPatch.
- [docs/pastures/events-architecture.md](../pastures/events-architecture.md): Ejemplo completo en Pastures.

---

**Generado**: 2026-01-09 | **Versión**: 1.0
