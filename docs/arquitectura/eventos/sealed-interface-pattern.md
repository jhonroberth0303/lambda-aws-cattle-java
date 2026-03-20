# 🔒 Sealed Interface Pattern: Type-Safe Events

**Fecha**: 2026-01-09

## 🎯 Objetivo

Documentar el patrón **Sealed Interface + Records** implementado en `PastureEvent` y sus 4 implementaciones para eventos type-safe.

---

## 📚 Tabla de Contenidos

1. [Java 17+ Features](#java-17-features)
2. [Sealed Interface Explicado](#sealed-interface-explicado)
3. [Records Explicados](#records-explicados)
4. [PastureEvent Hierarchía](#pastureevent-hierarchía)
5. [Pattern Matching](#pattern-matching)
6. [Ventajas sobre Enums](#ventajas-sobre-enums)
7. [Ejemplos Prácticos](#ejemplos-prácticos)

---

## Java 17+ Features

### ¿Qué es Java 17?

Java 17 (LTS - Long Term Support, 2021) introdujo varias features importantes para este patrón:

| Feature | JEP | Introducida | Descripción |
|---------|-----|-----------|-------------|
| Records | 405 | Java 14 (preview) → 16 (final) | Clases immutables concisas |
| Sealed Classes | 409 | Java 15 (preview) → 17 (final) | Controlar qué clases heredan |
| Pattern Matching | 406 | Java 16 (preview) → 17 (enhanced) | Switch sobre instancias |

---

## Sealed Interface Explicado

### Definición

Una **sealed interface** limita explícitamente qué tipos pueden implementarla.

```java
public sealed interface PastureEvent 
    permits OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent {
    
    EventType type();
    String user();
}
```

### Lectura Línea por Línea

```
public sealed interface PastureEvent
         ↑        ↑      ↑
         │        │      └─ Interfaz (contrato)
         │        └───────── sealed = solo tipos específicos pueden implementar
         └────────────────── public = visible en todo el módulo

    permits OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent
    ↑
    └─ Lista EXHAUSTIVA de implementadores (debe ser exhaustiva)
```

### Ejemplos Válidos

```java
// ✅ Implementar especificado en permits
public record OpenEvent(...) implements PastureEvent { }

// ❌ Implementar NO especificado en permits
public record UnknownEvent(...) implements PastureEvent { }  // ERROR de compilación
```

### Non-Sealed y Final

```java
// Opción 1: record (automáticamente final)
public record OpenEvent(...) implements PastureEvent { }
// → OpenEvent no puede ser extendida

// Opción 2: class non-sealed (puede ser heredado)
public non-sealed class ExtendableEvent implements PastureEvent { }
public class ChildEvent extends ExtendableEvent { }

// Opción 3: class final (no puede ser heredado)
public final class FinalEvent implements PastureEvent { }
```

### Beneficios

| Beneficio | Explicación |
|-----------|-------------|
| **Exhaustiveness** | Compilador obliga manejar TODOS los casos en switch. |
| **Type-Safety** | No hay strings para tipos. Errores en compile-time, no runtime. |
| **Intent** | El código comunica claramente qué tipos son permitidos. |
| **Maintenance** | Agregar nuevo tipo hace que compilador avise dónde actualizar. |
| **Reflection** | Puedes iterar los subtipos permitidos en runtime. |

---

## Records Explicados

### Qué es un Record

Un **record** es una declaración concisa de una clase **immutable** que solo contiene datos.

### Sintaxis

```java
public record OpenEvent(
    String user,
    String lotId,
    Integer animals
) implements PastureEvent {
    
    // El compilador genera automáticamente:
    // - Campos privados final
    // - Constructor canónico
    // - Getters (user(), lotId(), animals())
    // - equals(Object)
    // - hashCode()
    // - toString()
    
    @Override
    public EventType type() {
        return EventType.OPEN;
    }
}
```

### Equivalencia: Antes vs Ahora

#### ❌ ANTES (Java 16 -)

```java
@Getter
@EqualsAndHashCode
@ToString
public final class OpenEvent {
    private final String user;
    private final String lotId;
    private final Integer animals;
    
    public OpenEvent(String user, String lotId, Integer animals) {
        this.user = user;
        this.lotId = lotId;
        this.animals = animals;
    }
    
    public String user() { return user; }
    public String lotId() { return lotId; }
    public Integer animals() { return animals; }
}
```

#### ✅ AHORA (Java 16+)

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

**Líneas**: 30 → 10. **Claridad**: Mejor.

### Características de Records

| Característica | Descripción |
|----------------|-------------|
| **final** | No pueden ser heredados. |
| **Inmutable** | Campos son `final`, no hay setters. |
| **Compact Constructor** | Puedes validar en el constructor. |
| **Accessors** | `user()` no `getUser()`. |
| **Serializable** | Soportan serialización automática. |

### Validación en Record

```java
public record OpenEvent(
    String user,
    String lotId,
    Integer animals
) implements PastureEvent {
    
    // Compact constructor (valida antes de asignar)
    public OpenEvent {
        if (user == null || user.isBlank()) {
            throw new IllegalArgumentException("user no puede ser vacío");
        }
        if (animals == null || animals <= 0) {
            throw new IllegalArgumentException("animals debe ser > 0");
        }
    }
    
    @Override
    public EventType type() { return EventType.OPEN; }
}
```

---

## PastureEvent Hierarchía

### Estructura Completa

```
com.cattle.events.PastureEvent (sealed interface)
├── OpenEvent (record)
│   - user: String
│   - lotId: String
│   - animals: Integer
│
├── CloseEvent (record)
│   - user: String
│   - lotId: String
│   - animals: Integer
│   - residualCm: Integer
│
├── MaintenanceSetEvent (record)
│   - user: String
│   - substatus: PastureSubstatus
│   - holdUntil: String
│
└── MaintenanceClearEvent (record)
    - user: String

com.cattle.enums.EventType (enum)
├── OPEN
├── CLOSE
├── MAINTENANCE_SET
└── MAINTENANCE_CLEAR
```

### Definiciones Completas

```java
// 1. Sealed Interface
package com.cattle.events;

import com.cattle.enums.EventType;

public sealed interface PastureEvent 
    permits OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent {
    
    EventType type();
    String user();
}

// 2. OpenEvent
public record OpenEvent(
    String user,
    String lotId,
    Integer animals
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.OPEN; }
}

// 3. CloseEvent
public record CloseEvent(
    String user,
    String lotId,
    Integer animals,
    Integer residualCm
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.CLOSE; }
}

// 4. MaintenanceSetEvent
public record MaintenanceSetEvent(
    String user,
    PastureSubstatus substatus,
    String holdUntil
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.MAINTENANCE_SET; }
}

// 5. MaintenanceClearEvent
public record MaintenanceClearEvent(
    String user
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.MAINTENANCE_CLEAR; }
}

// 6. EventType Enum
package com.cattle.enums;

public enum EventType {
    OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR
}
```

---

## Pattern Matching

### Switch Expression vs If-Else

#### ❌ IF-ELSE (Viejo)

```java
public void handleEvent(PastureEvent event) {
    if (event instanceof OpenEvent) {
        OpenEvent e = (OpenEvent) event;  // ← Cast redundante
        System.out.println("Abriendo: " + e.animals() + " animales");
    } else if (event instanceof CloseEvent) {
        CloseEvent e = (CloseEvent) event;
        System.out.println("Cerrando, residual: " + e.residualCm() + "cm");
    } else if (event instanceof MaintenanceSetEvent) {
        MaintenanceSetEvent e = (MaintenanceSetEvent) event;
        System.out.println("Bloqueando hasta: " + e.holdUntil());
    } else if (event instanceof MaintenanceClearEvent) {
        MaintenanceClearEvent e = (MaintenanceClearEvent) event;
        System.out.println("Liberando");
    }
}
```

#### ✅ PATTERN MATCHING (Moderno)

```java
public void handleEvent(PastureEvent event) {
    switch (event) {
        case OpenEvent e -> 
            System.out.println("Abriendo: " + e.animals() + " animales");
        case CloseEvent e -> 
            System.out.println("Cerrando, residual: " + e.residualCm() + "cm");
        case MaintenanceSetEvent e -> 
            System.out.println("Bloqueando hasta: " + e.holdUntil());
        case MaintenanceClearEvent e -> 
            System.out.println("Liberando");
    }
}
```

### Exhaustiveness Check

```java
// ✅ VÁLIDO: trata TODOS los casos
switch (event) {
    case OpenEvent e -> { }
    case CloseEvent e -> { }
    case MaintenanceSetEvent e -> { }
    case MaintenanceClearEvent e -> { }
}

// ❌ ERROR: falta un caso
switch (event) {
    case OpenEvent e -> { }
    case CloseEvent e -> { }
    case MaintenanceSetEvent e -> { }
    // ¡Falta MaintenanceClearEvent!
}
// Compilador: error: switch expression does not cover all possible input values
```

### Guard Clauses

```java
switch (event) {
    case OpenEvent e when e.animals() > 50 ->
        System.out.println("Muchos animales: " + e.animals());
    case OpenEvent e ->
        System.out.println("Pocos animales: " + e.animals());
    case CloseEvent e when e.residualCm() < 5 ->
        System.out.println("Muy corto!");
    case CloseEvent e ->
        System.out.println("Altura normal");
    case MaintenanceSetEvent e ->
        System.out.println("Mantenimiento");
    case MaintenanceClearEvent e ->
        System.out.println("Liberar");
}
```

---

## Ventajas sobre Enums

### Por Qué NO Usar Enum

```java
// ❌ Problema con Enum

public enum PastureEventType {
    OPEN,
    CLOSE,
    MAINTENANCE_SET,
    MAINTENANCE_CLEAR
}

// Cada tipo tiene datos DIFERENTES:
// - OPEN: lotId, animals
// - CLOSE: lotId, animals, residualCm
// - MAINTENANCE_SET: substatus, holdUntil
// - MAINTENANCE_CLEAR: (nada)

// Con Enum, debes tener TODOS los campos en uno:
public class Event {
    public PastureEventType type;
    public String lotId;          // OPEN, CLOSE → presente
    public Integer animals;       // OPEN, CLOSE → presente
    public Integer residualCm;    // CLOSE solo → null en otros
    public PastureSubstatus substatus;  // MAINTENANCE_SET solo → null en otros
    public String holdUntil;      // MAINTENANCE_SET solo → null en otros
    // ↑ Muchos nulls, poco type-safe
}
```

### Ventajas de Sealed Interface + Records

```java
// ✅ Mejor con Sealed Interface

public sealed interface PastureEvent permits ... {
    EventType type();
    String user();
}

// Cada tipo tiene SOLO sus campos:
public record OpenEvent(
    String user,
    String lotId,
    Integer animals
) implements PastureEvent { }

public record MaintenanceSetEvent(
    String user,
    PastureSubstatus substatus,
    String holdUntil
) implements PastureEvent { }

// ✅ Type-safe: compilador conoce qué campos tiene cada tipo
```

### Tabla Comparativa

| Aspecto | Enum | Sealed Interface |
|---------|------|------------------|
| **Campos** | Todos en uno (muchos nulls) | Cada tipo sus campos |
| **Type-Safety** | ❌ Strings | ✅ Sealed interface |
| **Pattern Matching** | Difícil | ✅ Excelente |
| **Extensibilidad** | Enumerados fijos | Nuevos tipos = nuevo record |
| **Data** | Flat structure | Rich types |
| **Inmutabilidad** | ✅ | ✅ Records |

---

## Ejemplos Prácticos

### Ejemplo 1: Crear y Usar OpenEvent

```java
// Crear evento
PastureEvent event = new OpenEvent(
    user = "juan.perez@farm.com",
    lotId = "LOT#F001#001",
    animals = 15
);

// Usar con pattern matching
switch (event) {
    case OpenEvent e -> {
        System.out.println("Abriendo con " + e.animals() + " animales");
        // Acceso directo a campos sin cast
    }
    case CloseEvent e -> { /* ... */ }
    // ... etc
}
```

### Ejemplo 2: Validar en Constructor

```java
public record OpenEvent(
    String user,
    String lotId,
    Integer animals
) implements PastureEvent {
    
    public OpenEvent {
        if (animals == null || animals <= 0) {
            throw new IllegalArgumentException("animals debe ser > 0, fue: " + animals);
        }
    }
    
    @Override
    public EventType type() { return EventType.OPEN; }
}

// Uso:
try {
    var e = new OpenEvent("user", "LOT001", -5);  // ← Valida
} catch (IllegalArgumentException ex) {
    System.err.println("Error: " + ex.getMessage());
    // Output: "Error: animals debe ser > 0, fue: -5"
}
```

### Ejemplo 3: StatusEngine con Pattern Matching

```java
public EntityPatch applyEvent(Pasture pasture, Plan plan, PastureEvent ev) {
    EntityPatch patch = EntityPatch.of();
    
    switch (ev) {
        case OpenEvent e -> {
            if (isBlockedEffective(pasture)) {
                throw new IllegalStateException("Potrero bloqueado");
            }
            
            patch.set("status", PastureStatus.EN_USO);
            patch.set("lastUseAtIso", LocalDate.now().toString());
            
            // Auditoría con campos específicos de OpenEvent
            logEvent("OPEN", e.lotId(), e.animals());
        }
        
        case CloseEvent e -> {
            patch.set("status", PastureStatus.EN_DESCANSO);
            patch.set("currentHeightCm", e.residualCm());
            patch.set("lastUseAtIso", LocalDate.now().toString());
            
            // Auditoría con campo específico de CloseEvent
            logEvent("CLOSE", e.lotId(), e.residualCm());
        }
        
        case MaintenanceSetEvent e -> {
            patch.set("status", PastureStatus.MANTENIMIENTO);
            patch.set("substatus", e.substatus());
            patch.set("holdUntilIso", e.holdUntil());
            
            logEvent("MAINTENANCE_SET", e.substatus().toString(), null);
        }
        
        case MaintenanceClearEvent e -> {
            patch.set("status", calculateNextStatus(pasture, plan));
            patch.set("substatus", PastureSubstatus.NINGUNO);
            patch.remove("holdUntilIso");
            
            logEvent("MAINTENANCE_CLEAR", null, null);
        }
    }
    
    return patch;
}
```

---

## 🎓 Aprendizajes Clave

1. **Sealed Interfaces**: Controla qué puede implementar una interfaz.
2. **Records**: Syntax sugar para clases immutables.
3. **Pattern Matching**: Reemplaza if-else-instanceof con switch elegante.
4. **Exhaustiveness**: Compilador obliga manejar todos los casos.
5. **Type-Safe**: Cada tipo tiene solo sus campos, sin nulls.
6. **Immutability**: Records son inmutables por defecto.

---

## 📚 Referencias

- [JEP 409: Sealed Classes](https://openjdk.java.net/jeps/409)
- [JEP 405: Record Classes](https://openjdk.java.net/jeps/405)
- [JEP 406: Pattern Matching for switch](https://openjdk.java.net/jeps/406)
- [events-overview.md](events-overview.md): Comparación con Event.java.
- [docs/pastures/events-architecture.md](../pastures/events-architecture.md): Ejemplo completo.

---

**Generado**: 2026-01-09 | **Versión**: 1.0
