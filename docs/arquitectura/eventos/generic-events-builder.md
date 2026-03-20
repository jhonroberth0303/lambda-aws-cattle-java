# 🏗️ Sistema Genérico: Event.java + EventBuilder.java

**Fecha**: 2026-01-09

## 🎯 Objetivo

Documentar el patrón tradicional **Builder Pattern** implementado en `Event.java` y `EventBuilder.java` para registrar eventos genéricos de actividades.

---

## 📚 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Event.java (POJO Bean)](#eventjava-pojo-bean)
3. [EventBuilder.java](#eventbuilderjava)
4. [Patrón Builder](#patrón-builder)
5. [Casos de Uso](#casos-de-uso)
6. [Validaciones](#validaciones)
7. [Persistencia en DynamoDB](#persistencia-en-dynamodb)

---

## Introducción

### ¿Qué es Event.java?

`Event.java` es un **POJO Bean** que representa un evento genérico de una actividad que ocurrió en la granja. Es **persistible en DynamoDB** para auditoría histórica.

**Características**:
- POJO estándar con Lombok (getters, setters, builder).
- Anotaciones DynamoDB para mapeo automático.
- Diseño flexible para múltiples tipos de eventos.
- Persistencia a largo plazo en tabla `TABLE_EVENTS`.

### ¿Cuándo Usar?

- Necesitas **registrar una actividad** (auditoría).
- Los datos deben **persistirse indefinidamente**.
- Es un **evento histórico** (no cambia estado de otra entidad).
- Quieres usar el **builder pattern** clásico.

---

## Event.java (POJO Bean)

### Estructura Completa

```java
package com.cattle.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class Event {

    // =============== CLAVES ===============
    
    /**
     * Partition Key
     * Formato: farm#{farmId}#pasture#{pastureId}
     * Ejemplo: farm#F001#pasture#P-01
     */
    private String pk;
    
    /**
     * Sort Key
     * Formato: eventAt#{iso-timestamp}#{event-type}
     * Ejemplo: eventAt#2025-09-24T10:00:00Z#GRAZING_END
     * 
     * Beneficio: ordena automáticamente por tiempo
     */
    private String sk;
    
    // =============== ÍNDICES GLOBALES ===============
    
    /**
     * GSI1 Partition Key
     * Formato: farm#{farmId}#type#{event-type}
     * Ejemplo: farm#F001#type#GRAZING_END
     * 
     * Permite: query por tipo de evento en una finca
     */
    private String gsi1pk;
    
    /**
     * GSI1 Sort Key
     * Formato: iso-timestamp
     * Ejemplo: 2025-09-24T10:00:00Z
     * 
     * Permite: query por tipo y fecha, ordenado
     */
    private String gsi1sk;
    
    // =============== ATRIBUTOS ===============
    
    /**
     * Tipo de evento
     * Valores: GRAZING_END, FEED_CHANGE, HEALTH_CHECK, etc.
     */
    private String eventType;
    
    /**
     * Timestamp ISO cuando ocurrió el evento
     * Formato: YYYY-MM-DDTHH:MM:SSZ
     * Ejemplo: 2025-09-24T10:00:00Z
     */
    private String eventAt;
    
    /**
     * Cantidad de animales involucrados
     * Ej: 15 animales pastoreando
     */
    private Integer animals;
    
    /**
     * Altura residual del pasto en cm
     * Ej: 8 cm (altura después del pastoreo)
     */
    private Integer residualCm;
    
    /**
     * Usuario que registró el evento
     * Ej: juan.perez@farm.com
     */
    private String user;
    
    // =============== MAPEO DYNAMODB ===============
    
    @DynamoDbPartitionKey
    public String getPk() { 
        return pk; 
    }
    
    @DynamoDbSortKey
    public String getSk() { 
        return sk; 
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1-type-date")
    public String getGsi1pk() { 
        return gsi1pk; 
    }
    
    @DynamoDbSecondarySortKey(indexNames = "gsi1-type-date")
    public String getGsi1sk() { 
        return gsi1sk; 
    }
}
```

### Anotaciones Lombok

| Anotación | Genera |
|-----------|--------|
| `@Getter` | Getters para todos los campos. |
| `@Setter` | Setters para todos los campos. |
| `@Builder` | Builder pattern fluent. |
| `@AllArgsConstructor` | Constructor con todos los parámetros. |
| `@NoArgsConstructor` | Constructor sin parámetros. |
| `@DynamoDbBean` | Marca como bean de DynamoDB. |

---

## EventBuilder.java

### Definición

```java
package com.cattle.builders;

import com.cattle.entities.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    private String lotId;  // Campo extra para contexto
    
    /**
     * Defaults para evento GRAZING_END (cerrar pastoreo)
     */
    public EventBuilder defaultsForGrazingEnd() {
        this.eventType = this.eventType != null ? this.eventType : "GRAZING_END";
        return this;
    }
    
    /**
     * Construye el Event con validaciones
     */
    public Event build() {
        // Validación 1: campos requeridos no vacíos
        requireNonBlank(pk, "pk");
        requireNonBlank(sk, "sk");
        requireNonBlank(eventType, "eventType");
        requireNonBlank(eventAt, "eventAt");
        
        // Validación 2: defaults para GSI si no están especificados
        if (gsi1pk == null && eventType != null) {
            this.gsi1pk = "farm#UNKNOWN#type#" + eventType;
        }
        if (gsi1sk == null && eventAt != null) {
            this.gsi1sk = eventAt;
        }
        
        // Construir Event final
        return Event.builder()
                .pk(pk)
                .sk(sk)
                .gsi1pk(gsi1pk)
                .gsi1sk(gsi1sk)
                .eventType(eventType)
                .eventAt(eventAt)
                .animals(animals)
                .residualCm(residualCm)
                .user(user)
                .build();
    }
    
    /**
     * Valida que un string no sea null ni vacío
     */
    private static void requireNonBlank(String v, String field) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo requerido vacío: " + field);
        }
    }
}
```

---

## Patrón Builder

### ¿Qué es el Builder Pattern?

**Builder Pattern** es un patrón creacional que **construye objetos complejos paso a paso** usando una interfaz fluida.

### Beneficios

| Beneficio | Explicación |
|-----------|-------------|
| **Legibilidad** | `.pk(...).sk(...).eventType(...)` es muy claro. |
| **Flexibilidad** | Puedes omitir campos opcionales. |
| **Validación** | Concentrada en `build()`, no en constructor. |
| **Defaults** | Métodos como `defaultsForGrazingEnd()` aplican automáticos. |
| **Cadena de Llamadas** | Cada setter retorna `this` para encadenar. |

### Uso Básico

```java
// Crear evento GRAZING_END
Event event = new EventBuilder()
    .pk("farm#F001#pasture#P-01")
    .sk("eventAt#2025-09-24T10:00:00Z#GRAZING_END")
    .eventType("GRAZING_END")
    .eventAt("2025-09-24T10:00:00Z")
    .animals(15)
    .residualCm(8)
    .user("juan.perez@farm.com")
    .defaultsForGrazingEnd()  // ← Aplica defaults
    .build();
```

### Uso con Defaults

```java
// Más simple: usa defaults
Event event = new EventBuilder()
    .pk("farm#F001#pasture#P-01")
    .sk("eventAt#2025-09-24T10:00:00Z#GRAZING_END")
    .eventAt("2025-09-24T10:00:00Z")
    .animals(15)
    .residualCm(8)
    .user("juan.perez@farm.com")
    .defaultsForGrazingEnd()  // ← eventType se asigna automáticamente
    .build();
```

### Validación en build()

```java
Event event = new EventBuilder()
    .pk("farm#F001#pasture#P-01")
    .sk("eventAt#2025-09-24T10:00:00Z#GRAZING_END")
    // ¡Falta eventAt! → Lanzará IllegalArgumentException en build()
    .defaultsForGrazingEnd()
    .build();  // ← Error: "Campo requerido vacío: eventAt"
```

---

## Casos de Uso

### Caso 1: Registrar Fin de Pastoreo (GRAZING_END)

```java
// En PastureService, después de hacer CloseEvent
Event grazingEnd = new EventBuilder()
    .pk("farm#F001#pasture#P-01")
    .sk("eventAt#" + LocalDateTime.now().toString() + "#GRAZING_END")
    .eventAt(LocalDateTime.now().toString())
    .animals(15)
    .residualCm(8)
    .user("juan.perez@farm.com")
    .lotId("LOT#F001#001")
    .defaultsForGrazingEnd()
    .build();

// Persiste para auditoría
eventRepository.save(grazingEnd);
```

### Caso 2: Registrar Cambio de Alimentación (FEED_CHANGE)

```java
Event feedChange = new EventBuilder()
    .pk("farm#F001#bovineIdentityItem#B123")
    .sk("eventAt#" + timestamp + "#FEED_CHANGE")
    .eventType("FEED_CHANGE")
    .eventAt(timestamp)
    .user("admin@farm.com")
    .gsi1pk("farm#F001#type#FEED_CHANGE")
    .gsi1sk(timestamp)
    .build();

eventRepository.save(feedChange);
```

### Caso 3: Registrar Revisión de Salud (HEALTH_CHECK)

```java
Event healthCheck = new EventBuilder()
    .pk("farm#F001#bovineIdentityItem#B123")
    .sk("eventAt#" + timestamp + "#HEALTH_CHECK")
    .eventType("HEALTH_CHECK")
    .eventAt(timestamp)
    .user("veterinario@farm.com")
    .gsi1pk("farm#F001#type#HEALTH_CHECK")
    .gsi1sk(timestamp)
    .build();

eventRepository.save(healthCheck);
```

---

## Validaciones

### En Builder.build()

```java
public Event build() {
    // Validación 1: Campos requeridos
    requireNonBlank(pk, "pk");              // No null, no vacío
    requireNonBlank(sk, "sk");
    requireNonBlank(eventType, "eventType");
    requireNonBlank(eventAt, "eventAt");
    
    // Validación 2: Formato (opcional pero recomendado)
    validateIsoTimestamp(eventAt);
    
    // Validación 3: Valores permitidos (opcional)
    validateEventType(eventType);
    
    // Validación 4: Defaults inteligentes
    if (gsi1pk == null && eventType != null) {
        this.gsi1pk = "farm#UNKNOWN#type#" + eventType;
    }
    if (gsi1sk == null && eventAt != null) {
        this.gsi1sk = eventAt;
    }
    
    // Construir
    return Event.builder()
        // ...
        .build();
}

private void validateIsoTimestamp(String eventAt) {
    try {
        LocalDateTime.parse(eventAt, DateTimeFormatter.ISO_DATE_TIME);
    } catch (Exception e) {
        throw new IllegalArgumentException("eventAt debe ser ISO format: " + e.getMessage());
    }
}

private void validateEventType(String eventType) {
    List<String> ALLOWED = List.of("GRAZING_END", "FEED_CHANGE", "HEALTH_CHECK", ...);
    if (!ALLOWED.contains(eventType)) {
        throw new IllegalArgumentException("eventType no permitido: " + eventType);
    }
}
```

---

## Persistencia en DynamoDB

### Tabla: TABLE_EVENTS

```
Tabla: TABLE_EVENTS (o cattle-events-dev)

PK: farm#{farmId}#pasture#{pastureId}
SK: eventAt#{timestamp}#{eventType}

GSI1:
  PK: farm#{farmId}#type#{eventType}
  SK: {timestamp}
```

### Query Ejemplos

#### 1. Obtener todos los eventos de un potrero

```bash
aws dynamodb query \
  --table-name cattle-events-dev \
  --key-condition-expression "pk = :pk" \
  --expression-attribute-values '{":pk":{"S":"farm#F001#pasture#P-01"}}' \
  --region us-east-1
```

#### 2. Obtener eventos GRAZING_END de una finca (usando GSI1)

```bash
aws dynamodb query \
  --table-name cattle-events-dev \
  --index-name gsi1-type-date \
  --key-condition-expression "gsi1pk = :pk" \
  --expression-attribute-values '{":pk":{"S":"farm#F001#type#GRAZING_END"}}' \
  --region us-east-1
```

#### 3. Obtener eventos de un tipo en un rango de fechas

```bash
aws dynamodb query \
  --table-name cattle-events-dev \
  --index-name gsi1-type-date \
  --key-condition-expression "gsi1pk = :pk AND gsi1sk BETWEEN :start AND :end" \
  --expression-attribute-values '{
    ":pk":{"S":"farm#F001#type#GRAZING_END"},
    ":start":{"S":"2025-09-01T00:00:00Z"},
    ":end":{"S":"2025-09-30T23:59:59Z"}
  }' \
  --region us-east-1
```

---

## 📝 Convenciones

### Formato de Claves

```
PK: farm#{farmId}#[entity-type]#{entity-id}
    farm#F001#pasture#P-01
    farm#F001#bovineIdentityItem#B123

SK: eventAt#{iso-timestamp}#{event-type}
    eventAt#2025-09-24T10:00:00Z#GRAZING_END
    eventAt#2025-09-24T11:30:00Z#FEED_CHANGE

GSI1PK: farm#{farmId}#type#{event-type}
        farm#F001#type#GRAZING_END

GSI1SK: iso-timestamp
        2025-09-24T10:00:00Z
```

### Nombres de EventType

| Tipo | Descripción |
|------|-------------|
| `GRAZING_END` | Fin de pastoreo |
| `FEED_CHANGE` | Cambio de alimentación |
| `HEALTH_CHECK` | Revisión de salud |
| `MAINTENANCE_START` | Inicio de mantenimiento |
| `MAINTENANCE_END` | Fin de mantenimiento |
| `VETERINARY_VISIT` | Visita veterinaria |

---

## 🔄 Flujo Completo: Ejemplo

```
1. Usuario cierra potrero en Frontend
   ↓
2. POST /farms/F001/pastures/P001/events
   { "eventType": "CLOSE", "residualCm": 8 }
   ↓
3. PastureEventController recibe request
   ↓
4. PastureStatusEngine.applyEvent() 
   → CloseEvent(user, lotId, animals, residualCm)
   → EntityPatch { status: EN_DESCANSO, ... }
   ↓
5. PastureRepository.update() persiste cambios
   ↓
6. EventBuilder registra para auditoría
   Event.builder()
     .pk("farm#F001#pasture#P-01")
     .sk("eventAt#2025-09-24T10:00:00Z#GRAZING_END")
     .eventType("GRAZING_END")
     .eventAt("2025-09-24T10:00:00Z")
     .animals(15)
     .residualCm(8)
     .user("juan.perez@farm.com")
     .defaultsForGrazingEnd()
     .build()
   ↓
7. EventRepository.save() persiste evento histórico
   ↓
8. Auditoria completa en TABLE_EVENTS
   - Qué pasó
   - Cuándo
   - Quién
   - Detalles
```

---

## 🔗 Referencias

- [events-overview.md](events-overview.md): Comparación con PastureEvent.
- [entity-patch-pattern.md](entity-patch-pattern.md): Cómo se aplican cambios.
- [docs/pastures/events-architecture.md](../pastures/events-architecture.md): Ejemplo completo.

---

**Generado**: 2026-01-09 | **Versión**: 1.0
