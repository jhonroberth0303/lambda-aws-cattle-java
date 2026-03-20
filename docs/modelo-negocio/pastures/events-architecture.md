# 📌 Arquitectura de Eventos: Sealed Interface Pattern

**Documento complementario para Pastures**

Fecha: 2026-01-09

## 🎯 Objetivo

Explicar en detalle cómo funciona el sistema de eventos en el módulo Pastures usando **sealed interfaces** y **records** de Java (feature introducido en Java 17), y cómo diferenciarlo del sistema genérico de eventos (`Event.java`).

---

## 📚 Tabla de Contenidos

1. [¿Qué es Sealed Interface?](#qué-es-sealed-interface)
2. [Arquitectura de PastureEvent](#arquitectura-de-pastureevent)
3. [Tipos de Eventos](#tipos-de-eventos)
4. [Pattern Matching y Switch Expression](#pattern-matching-y-switch-expression)
5. [Implementación en PastureStatusEngine](#implementación-en-pasturestatusengine)
6. [Comparación: PastureEvent vs Event.java](#comparación-pastureevent-vs-eventjava)
7. [Ejemplos de Uso](#ejemplos-de-uso)

---

## ¿Qué es Sealed Interface?

### Definición

Una **sealed interface** es un contrato que limita explícitamente qué tipos pueden implementarla. Esto proporciona **exhaustividad de compilación** y **seguridad de tipos**.

### Sintaxis

```java
public sealed interface PastureEvent permits OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent {
    EventType type();
    String user();
}
```

**Lectura**: "PastureEvent es una interfaz sellada que SOLO puede ser implementada por los cuatro tipos listados en `permits`."

### Beneficios

| Beneficio | Explicación |
|-----------|-------------|
| **Type-Safe** | No hay strings arbitrarios para tipo de evento. Compilador lo valida. |
| **Exhaustive Checking** | El compilador obliga a manejar TODOS los casos en un `switch`. Si añades nuevo tipo, el IDE te avisa. |
| **Inmutabilidad** | Records son inmutables por defecto (todas las propiedades `final`). |
| **No-nullability** | Records de Java pueden combinar con anotación `@NonNull`. |
| **Pattern Matching** | Soportan pattern matching en `switch` (Java 17+). |

---

## Arquitectura de PastureEvent

### Jerarquía de Tipos

```
PastureEvent (sealed interface)
├── OpenEvent (record)
├── CloseEvent (record)
├── MaintenanceSetEvent (record)
└── MaintenanceClearEvent (record)
```

### Interfaz Sellada

```java
package com.cattle.events;

import com.cattle.enums.EventType;

public sealed interface PastureEvent permits OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent {
    
    /**
     * Retorna el tipo de evento (OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR)
     */
    EventType type();
    
    /**
     * Identifica quién generó el evento (usuario, sistema, sensor)
     */
    String user();
}
```

**Notas**:
- No tiene métodos con implementación (es un contrato puro).
- Los únicos métodos comunes a todos los eventos son `type()` y `user()`.
- Cada implementación puede tener atributos adicionales específicos.

---

## Tipos de Eventos

### 1. OpenEvent

**Propósito**: Abrir un potrero para pastoreo (EN_DESCANSO/DISPONIBLE → EN_USO).

```java
public record OpenEvent(
    String user,             // Usuario que abre (ej: "juan.perez@farm.com")
    String lotId,            // ID del lote/grupo de animales (ej: "LOT001")
    Integer animals          // Cantidad de animales a pastar (ej: 15)
) implements PastureEvent {
    
    @Override
    public EventType type() {
        return EventType.OPEN;
    }
}
```

**Ejemplo de instancia**:
```java
var openEvent = new OpenEvent(
    user = "juan.perez@farm.com",
    lotId = "LOT#F001#001",
    animals = 15
);
```

**Cambios que aplica**:
- `status` → "EN_USO"
- `lastUseAt` → hoy
- Graba `lotId` y `animals` para auditoría

---

### 2. CloseEvent

**Propósito**: Cerrar un potrero después de pastoreo (EN_USO → EN_DESCANSO).

```java
public record CloseEvent(
    String user,              // Usuario que cierra
    String lotId,             // ID del lote
    Integer animals,          // Cantidad de animales
    Integer residualCm        // Altura residual después del pastoreo
) implements PastureEvent {
    
    @Override
    public EventType type() {
        return EventType.CLOSE;
    }
}
```

**Ejemplo**:
```java
var closeEvent = new CloseEvent(
    user = "juan.perez@farm.com",
    lotId = "LOT#F001#001",
    animals = 15,
    residualCm = 8  // El pasto quedó a 8 cm de alto
);
```

**Cambios que aplica**:
- `status` → "EN_DESCANSO"
- `currentHeightCm` → `residualCm` (altura después del pastoreo)
- `lastUseAt` → fecha de cierre
- Inicia conteo de días de descanso

---

### 3. MaintenanceSetEvent

**Propósito**: Bloquear un potrero para mantenimiento (cualquier estado → MANTENIMIENTO).

```java
public record MaintenanceSetEvent(
    String user,                           // Usuario que bloquea
    PastureSubstatus substatus,            // FERTILIZANDO, REPARANDO, CUARENTENA
    String holdUntil                       // Fecha de liberación (YYYY-MM-DD)
) implements PastureEvent {
    
    @Override
    public EventType type() {
        return EventType.MAINTENANCE_SET;
    }
}
```

**Ejemplo**:
```java
var maintenanceEvent = new MaintenanceSetEvent(
    user = "admin@farm.com",
    substatus = PastureSubstatus.FERTILIZANDO,
    holdUntil = "2025-12-15"  // Liberar el 15 de diciembre
);
```

**Cambios que aplica**:
- `status` → "MANTENIMIENTO"
- `substatus` → FERTILIZANDO|REPARANDO|CUARENTENA
- `holdUntil` → fecha de liberación
- `gsi2pk` → "farm#F001#blocked#true" (para filtrar bloqueados)

**Motor automático** (en próximos GETs):
- Si `holdUntil < hoy`, automáticamente transiciona a EN_DESCANSO o DISPONIBLE según ETA.

---

### 4. MaintenanceClearEvent

**Propósito**: Liberar bloqueo de mantenimiento manualmente (MANTENIMIENTO → EN_DESCANSO/DISPONIBLE).

```java
public record MaintenanceClearEvent(
    String user              // Usuario que libera
) implements PastureEvent {
    
    @Override
    public EventType type() {
        return EventType.MAINTENANCE_CLEAR;
    }
}
```

**Ejemplo**:
```java
var clearEvent = new MaintenanceClearEvent(
    user = "admin@farm.com"
);
```

**Cambios que aplica**:
- `substatus` → NINGUNO
- `holdUntil` → null
- `status` → EN_DESCANSO o DISPONIBLE (según ETA calculado)
- `gsi2pk` → "farm#F001#blocked#false"

---

## Pattern Matching y Switch Expression

### Switch Expression Moderno (Java 17+)

En `PastureStatusEngine.applyEvent()`, se usa **pattern matching** con `switch` expression:

```java
public EntityPatch applyEvent(Pasture pasture, Plan plan, PastureEvent ev) {
    EntityPatch patch = new EntityPatch();
    
    // Switch exhaustive: compilador obliga manejar TODOS los casos
    switch (ev) {
        case OpenEvent e -> {
            if (isBlockedEffective(pasture)) {
                throw new IllegalStateException("No se puede abrir: potrero bloqueado");
            }
            patch.set("status", PastureStatus.EN_USO.name());
            patch.set("lastUseAt", LocalDate.now().toString());
            // Grabar lotId y animals para auditoría
            logEvent("OPEN", e.lotId(), e.animals());
        }
        
        case CloseEvent e -> {
            patch.set("status", PastureStatus.EN_DESCANSO.name());
            patch.set("currentHeightCm", e.residualCm());
            patch.set("lastUseAt", LocalDate.now().toString());
            logEvent("CLOSE", e.lotId(), e.animals());
        }
        
        case MaintenanceSetEvent e -> {
            patch.set("status", PastureStatus.MANTENIMIENTO.name());
            patch.set("substatus", e.substatus().name());
            patch.set("holdUntil", e.holdUntil());
            patch.set("gsi2pk", "farm#" + pasture.getFarmId() + "#blocked#true");
            logEvent("MAINTENANCE_SET", e.substatus().name(), null);
        }
        
        case MaintenanceClearEvent e -> {
            patch.set("substatus", PastureSubstatus.NINGUNO.name());
            patch.set("holdUntil", null);
            int eta = EtaCalculator.etaOpenDays(pasture, plan);
            PastureStatus next = (eta <= 0) ? PastureStatus.DISPONIBLE : PastureStatus.EN_DESCANSO;
            patch.set("status", next.name());
            patch.set("gsi2pk", "farm#" + pasture.getFarmId() + "#blocked#false");
            logEvent("MAINTENANCE_CLEAR", null, null);
        }
    }
    
    return patch;
}
```

### Ventajas del Switch sobre If-Else

| Aspecto | If-Else | Switch Expression |
|--------|---------|-------------------|
| **Exhaustividad** | ❌ Fácil olvidar un caso | ✅ Compilador obliga todos |
| **Seguridad** | ❌ Cast a tipos específicos | ✅ Type inference automático |
| **Legibilidad** | ❌ Chains largos | ✅ Más conciso |
| **Pattern Matching** | ❌ No soporta | ✅ Soporta (Java 17+) |

---

## Implementación en PastureStatusEngine

### Flujo Completo

```
1. Controller recibe HTTP POST /farms/{farmId}/pastures/{pastureId}/events
2. Convierte payload JSON → PastureEventRequest
3. Mapea request → instancia de PastureEvent (OpenEvent, CloseEvent, etc.)
4. Service obtiene Pasture y Plan
5. StatusEngine.applyEvent(pasture, plan, event) → EntityPatch
6. Repository.update(pk, patch) → DynamoDB
7. Retorna potrero actualizado en HTTP 200
```

### Conversión de Payload a Evento

```java
// En PastureEventController
@PostMapping("/{pastureId}/events")
public ResponseEntity<?> applyEvent(
    @PathVariable String farmId,
    @PathVariable String pastureId,
    @RequestBody PastureEventRequest request) {
    
    // Paso 1: Mapear request a PastureEvent
    PastureEvent event = switch (request.getEventType()) {
        case "OPEN" -> new OpenEvent(
            request.getUser(),
            request.getLotId(),
            request.getAnimals()
        );
        case "CLOSE" -> new CloseEvent(
            request.getUser(),
            request.getLotId(),
            request.getAnimals(),
            request.getResidualCm()
        );
        case "MAINTENANCE_SET" -> new MaintenanceSetEvent(
            request.getUser(),
            PastureSubstatus.valueOf(request.getSubstatus()),
            request.getHoldUntil()
        );
        case "MAINTENANCE_CLEAR" -> new MaintenanceClearEvent(
            request.getUser()
        );
        default -> throw new IllegalArgumentException("Unknown event type: " + request.getEventType());
    };
    
    // Paso 2: Aplicar evento y guardar
    return pastureService.applyEvent(pastureId, event);
}
```

---

## Comparación: PastureEvent vs Event.java

### Tabla Comparativa

| Aspecto | PastureEvent | Event.java |
|---------|-------------|-----------|
| **Ubicación** | `events/PastureEvent.java` | `entities/Event.java` |
| **Patrón** | Sealed interface + records | POJO Bean + Builder |
| **Tipo de Datos** | Múltiples tipos (OpenEvent, CloseEvent, ...) | Objeto único |
| **Seguridad de Tipos** | Type-safe (sealed, records) | Strings (eventType) |
| **Inmutabilidad** | Sí (records) | No (setter getters) |
| **Tabla DynamoDB** | No (transiente, usado en memoria) | Sí (persistido) |
| **Propósito** | Cambiar estado del potrero | Registrar actividad de pastoreo |
| **Consumidor** | PastureStatusEngine | ? (no claro en el módulo Pastures) |
| **Validación** | Compilador (exhaustive check) | Runtime (validation) |

### Cuándo Usar Cada Uno

**Usa PastureEvent si**:
- Necesitas **cambiar estado** de una entidad.
- Quieres **type-safety** en compile-time.
- Los datos son **transientes** (no persisten en tabla separada).
- Usarás **pattern matching** en Java 17+.

**Usa Event.java si**:
- Necesitas **registrar un histórico** de actividades.
- Los datos se **persisten en DynamoDB**.
- Múltiples eventos pueden suceder sin cambiar estado de otra entidad.
- Usas un **builder pattern** tradicional.

---

## Ejemplos de Uso

### Ejemplo 1: Abrir Potrero

**Request HTTP**:
```json
POST /farms/F001/pastures/P001/events
Content-Type: application/json

{
  "eventType": "OPEN",
  "user": "juan.perez@farm.com",
  "lotId": "LOT#F001#001",
  "animals": 15
}
```

**Flujo Interno**:
```java
// 1. Deserializar
PastureEventRequest req = mapper.readValue(body, PastureEventRequest.class);

// 2. Convertir a OpenEvent
PastureEvent event = new OpenEvent(
    user = "juan.perez@farm.com",
    lotId = "LOT#F001#001",
    animals = 15
);

// 3. Obtener Pasture y Plan
Pasture pasture = pastureRepository.findById("PASTURE#P001").orElseThrow();
Plan plan = planRepository.findByFarmAndSpecies("F001", pasture.getSpecies()).orElseThrow();

// 4. Aplicar evento
EntityPatch patch = pastureStatusEngine.applyEvent(pasture, plan, event);
// patch contiene: { status: "EN_USO", lastUseAt: "2026-01-09" }

// 5. Guardar
pastureRepository.update("PASTURE#P001", patch);

// 6. Retornar 200 OK con potrero actualizado
return ResponseEntity.ok(pasture);
```

---

### Ejemplo 2: Bloquear para Fertilización

**Request HTTP**:
```json
POST /farms/F001/pastures/P001/events
Content-Type: application/json

{
  "eventType": "MAINTENANCE_SET",
  "user": "admin@farm.com",
  "substatus": "FERTILIZANDO",
  "holdUntil": "2025-12-25"
}
```

**Flujo**:
```java
// 1. Convertir a MaintenanceSetEvent
PastureEvent event = new MaintenanceSetEvent(
    user = "admin@farm.com",
    substatus = PastureSubstatus.FERTILIZANDO,
    holdUntil = "2025-12-25"
);

// 2. Switch en applyEvent():
switch (event) {
    case MaintenanceSetEvent e -> {
        patch.set("status", PastureStatus.MANTENIMIENTO.name());
        patch.set("substatus", PastureSubstatus.FERTILIZANDO.name());
        patch.set("holdUntil", "2025-12-25");
        patch.set("gsi2pk", "farm#F001#blocked#true");
        // Ahora está bloqueado, no aparecerá en queries normales
    }
}

// 3. En próximo GET a /farms/F001/pastures:
//    - Motor chequea isHoldUntilExpired()
//    - Si 2026-01-09 > 2025-12-25, automáticamente libera
//    - Transición a EN_DESCANSO o DISPONIBLE según ETA
```

---

### Ejemplo 3: Cerrar Potrero Post-Pastoreo

**Request HTTP**:
```json
POST /farms/F001/pastures/P001/events
Content-Type: application/json

{
  "eventType": "CLOSE",
  "user": "juan.perez@farm.com",
  "lotId": "LOT#F001#001",
  "animals": 15,
  "residualCm": 8
}
```

**Cambios en Potrero**:
- Antes: `{ status: "EN_USO", currentHeightCm: 25, lastUseAt: "2026-01-08" }`
- Después: `{ status: "EN_DESCANSO", currentHeightCm: 8, lastUseAt: "2026-01-09" }`

**Motor de Rotación Automático**:
- Calcula ETA = 30 - 1 + (20 - 8)/2.5 = 29 + 4.8 = ~34 días
- Estado derivado: EN_DESCANSO (ETA > 0)

---

## 🎓 Aprendizajes Clave

1. **Sealed Interfaces**: Proporcionan exhaustividad de compilación para tipos de eventos.
2. **Records**: Inmutables, concisas, perfectas para eventos.
3. **Pattern Matching**: Reemplaza if-else con switch expression más seguro.
4. **Type-Safe Events**: No uses strings para tipos de eventos; usa enums o sealed interfaces.
5. **PastureEvent ≠ Event.java**: Cada uno sirve un propósito diferente. No confundir.

---

## 📚 Referencias

- [JEP 409: Sealed Classes](https://openjdk.java.net/jeps/409)
- [JEP 405: Record Classes](https://openjdk.java.net/jeps/405)
- [JEP 406: Pattern Matching for Switch](https://openjdk.java.net/jeps/406)
- [pastures-overview.md](./pastures-overview.md) - Documentación técnica del módulo Pastures
