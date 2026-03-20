# 🔄 Patrón EntityPatch: Aplicar Cambios Incrementales

**Fecha**: 2026-01-09

## 🎯 Objetivo

Documentar el patrón `EntityPatch` y `PatchApplier` que permite registrar y aplicar cambios parciales a entidades.

---

## 📚 Tabla de Contenidos

1. [Introducción](#introducción)
2. [EntityPatch Record](#entitypatch-record)
3. [PatchApplier Utility](#patchapplier-utility)
4. [Flujo Completo](#flujo-completo)
5. [Ejemplos Prácticos](#ejemplos-prácticos)
6. [Ventajas y Casos de Uso](#ventajas-y-casos-de-uso)

---

## Introducción

### El Problema

Cuando una entidad tiene muchos atributos y necesitas cambiar solo algunos, tienes dos opciones:

1. **Guardar toda la entidad** (ineficiente):
   - Alto consumo de RCU/WCU en DynamoDB.
   - Difícil de auditar qué cambió exactamente.
   - Riesgo de sobrescribir datos no intencionalmente.

2. **Aplicar cambios parciales** (mejor):
   - Solo las propiedades que cambian.
   - Auditable (qué set, qué removed).
   - Eficiente en ancho de banda.

### La Solución: EntityPatch

`EntityPatch` es un patrón que:
- Registra **cambios SET** (atributos a establecer).
- Registra **cambios REMOVE** (atributos a eliminar).
- Se aplica en memoria con `PatchApplier`.
- Se persiste en DynamoDB con `UpdateItem`.

---

## EntityPatch Record

### Definición

```java
package com.cattle.events;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record EntityPatch(
    Map<String, Object> set,   // Atributos a SET
    List<String> remove        // Atributos a REMOVE
) {
    
    /**
     * Constructor recomendado: crea patch vacío
     */
    public static EntityPatch of() {
        return new EntityPatch(new LinkedHashMap<>(), new ArrayList<>());
    }
    
    /**
     * Añade un SET al patch (fluent API)
     */
    public EntityPatch set(String k, Object v) {
        set.put(k, v);
        return this;
    }
    
    /**
     * Añade un REMOVE al patch (fluent API)
     */
    public EntityPatch remove(String k) {
        remove.add(k);
        return this;
    }
    
    /**
     * Comprueba si el patch tiene cambios
     */
    public boolean isEmpty() {
        return set.isEmpty() && remove.isEmpty();
    }
}
```

### Características

| Aspecto | Descripción |
|---------|-------------|
| **Record** | Java 16+ feature: immutable, auto-generated getters, equals, hashCode, toString. |
| **Map<String, Object>** | Cambios a establecer (clave → valor). |
| **List<String>** | Cambios a eliminar (solo nombres de atributos). |
| **Fluent API** | `.set(...).set(...).remove(...)` encadenable. |
| **isEmpty()** | Optimización: no aplicar patch vacío. |

### Creación de Instancias

```java
// Opción 1: Constructor factory
EntityPatch patch1 = EntityPatch.of();

// Opción 2: Fluent API completa
EntityPatch patch2 = EntityPatch.of()
    .set("status", "EN_USO")
    .set("lastUseAt", "2026-01-09")
    .set("currentHeightCm", 25)
    .remove("holdUntil")
    .remove("blockReason");

// Opción 3: Inline record constructor (si necesario)
EntityPatch patch3 = new EntityPatch(
    Map.of("status", "EN_USO"),
    List.of("holdUntil")
);
```

---

## PatchApplier Utility

### Definición

```java
package com.cattle.events;

import com.cattle.entities.Pasture;
import com.cattle.enums.PastureStatus;
import com.cattle.enums.PastureSubstatus;
import java.util.Map;

public final class PatchApplier {
    
    /**
     * Aplica en memoria el patch a la entidad Pasture.
     * Convierte tipos y mantiene coherencia.
     */
    public static void applyLocal(Pasture pasture, EntityPatch patch) {
        if (patch == null || patch.isEmpty() || pasture == null) return;
        
        // 1) Aplicar SETS
        for (Map.Entry<String, Object> e : patch.set().entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            
            switch (key) {
                case "status" -> pasture.setStatus(parseStatus(value).name());
                case "substatus" -> pasture.setSubstatus(parseSubstatus(value).name());
                case "holdUntilIso" -> pasture.setHoldUntil(asString(value));
                case "lastUseAtIso" -> pasture.setLastUseAt(asString(value));
                case "gsi2pk" -> pasture.setGsi2pk(asString(value));
                case "gsi2sk" -> pasture.setGsi2sk(asInteger(value));
                default -> {
                    // Atributos no manejados: ignorar o log
                }
            }
        }
        
        // 2) Aplicar REMOVES
        for (String k : patch.remove()) {
            switch (k) {
                case "holdUntilIso" -> pasture.setHoldUntil(null);
                case "gsi2pk" -> pasture.setGsi2pk(null);
                case "gsi2sk" -> pasture.setGsi2sk(null);
                default -> {
                    // Atributos no manejados
                }
            }
        }
    }
    
    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }
    
    private static Integer asInteger(Object object) {
        if (object == null) return null;
        if (object instanceof Integer i) return i;
        if (object instanceof Number n) return n.intValue();
        try { return Integer.parseInt(object.toString()); } catch (Exception e) { return null; }
    }
    
    private static PastureStatus parseStatus(Object object) {
        if (object == null) return null;
        if (object instanceof PastureStatus s) return s;
        return PastureStatus.valueOf(object.toString());
    }
    
    private static PastureSubstatus parseSubstatus(Object object) {
        if (object == null) return null;
        if (object instanceof PastureSubstatus s) return s;
        return PastureSubstatus.valueOf(object.toString());
    }
}
```

### Características

| Aspecto | Descripción |
|---------|-------------|
| **static final class** | Utilidad pura, no instanciable. |
| **applyLocal()** | Método estático para aplicar patch a entidad. |
| **Type Conversion** | Convierte Object a tipos específicos (String, Integer, Status, etc.). |
| **Null Safety** | Verifica null en todos los pasos. |
| **Switch Expression** | Usa Java 17+ pattern matching. |
| **Enum Parsing** | Convierte String → enum de manera segura. |

---

## Flujo Completo

### Secuencia de Operaciones

```
1. HTTP Request
   POST /farms/F001/pastures/P001/events
   { "eventType": "CLOSE", "residualCm": 8 }
   
   ↓
   
2. PastureEventController
   Deserializa request → PastureEventRequest
   
   ↓
   
3. Conversión a PastureEvent
   PastureEventRequest → CloseEvent (record)
   
   ↓
   
4. PastureStatusEngine.applyEvent()
   CloseEvent + Pasture + Plan → EntityPatch
   (genera patch con cambios lógicos)
   
   patch = { 
     set: { 
       "status": "EN_DESCANSO",
       "currentHeightCm": 8,
       "lastUseAtIso": "2026-01-09"
     },
     remove: []
   }
   
   ↓
   
5. PatchApplier.applyLocal()
   Aplica patch a Pasture en memoria
   (pasture.status = EN_DESCANSO, pasture.currentHeightCm = 8, ...)
   
   ↓
   
6. PastureRepository.update()
   Persiste en DynamoDB usando UpdateItem
   con condiciones y atributos del patch
   
   ↓
   
7. HTTP Response 200 OK
   { pasture object con cambios aplicados }
```

### Pseudocódigo

```java
// En Service
public Pasture applyEvent(String pastureId, PastureEvent event) {
    // 1. Obtener entidades
    Pasture pasture = pastureRepository.findById("PASTURE#" + pastureId)
        .orElseThrow(() -> new NotFoundException("Pasture not found"));
    Plan plan = planRepository.findByFarmAndSpecies(...)
        .orElseThrow(() -> new NotFoundException("Plan not found"));
    
    // 2. Generar patch
    EntityPatch patch = pastureStatusEngine.applyEvent(pasture, plan, event);
    
    // 3. Validar patch
    if (patch.isEmpty()) {
        // Sin cambios, retornar como está
        return pasture;
    }
    
    // 4. Aplicar en memoria
    PatchApplier.applyLocal(pasture, patch);
    
    // 5. Persistir en BD
    pastureRepository.update(pasture.getPk(), patch);
    
    // 6. Retornar entidad actualizada
    return pasture;
}
```

---

## Ejemplos Prácticos

### Ejemplo 1: Abrir Potrero (OPEN Event)

**Evento**: `OpenEvent(user="juan", lotId="LOT001", animals=15)`

**En StatusEngine**:
```java
case OpenEvent e -> {
    if (isBlockedEffective(pasture)) {
        throw new IllegalStateException("Potrero bloqueado");
    }
    
    EntityPatch patch = EntityPatch.of()
        .set("status", PastureStatus.EN_USO)
        .set("lastUseAtIso", LocalDate.now().toString())
        .set("gsi2pk", "farm#" + pasture.getFarmId() + "#in-use#true");
    
    // Auditoría (opcional)
    logEvent("OPEN", e.lotId(), e.animals());
    
    return patch;
}
```

**Patch Resultante**:
```
EntityPatch {
  set: {
    "status": PastureStatus.EN_USO,
    "lastUseAtIso": "2026-01-09",
    "gsi2pk": "farm#F001#in-use#true"
  },
  remove: []
}
```

**En PatchApplier**:
```
pasture.setStatus("EN_USO");
pasture.setLastUseAt("2026-01-09");
pasture.setGsi2pk("farm#F001#in-use#true");
```

---

### Ejemplo 2: Bloquear para Fertilización (MAINTENANCE_SET Event)

**Evento**: `MaintenanceSetEvent(user="admin", substatus=FERTILIZANDO, holdUntil="2025-12-25")`

**En StatusEngine**:
```java
case MaintenanceSetEvent e -> {
    EntityPatch patch = EntityPatch.of()
        .set("status", PastureStatus.MANTENIMIENTO)
        .set("substatus", e.substatus())
        .set("holdUntilIso", e.holdUntil())
        .set("gsi2pk", "farm#" + pasture.getFarmId() + "#blocked#true")
        .set("gsi2sk", calculateETA(pasture, plan));
    
    return patch;
}
```

**Patch Resultante**:
```
EntityPatch {
  set: {
    "status": PastureStatus.MANTENIMIENTO,
    "substatus": PastureSubstatus.FERTILIZANDO,
    "holdUntilIso": "2025-12-25",
    "gsi2pk": "farm#F001#blocked#true",
    "gsi2sk": 15
  },
  remove: []
}
```

---

### Ejemplo 3: Liberar Bloqueo (MAINTENANCE_CLEAR Event)

**Evento**: `MaintenanceClearEvent(user="admin")`

**En StatusEngine**:
```java
case MaintenanceClearEvent e -> {
    // Calcular nuevo estado
    int eta = EtaCalculator.etaOpenDays(pasture, plan);
    PastureStatus nextStatus = (eta <= 0) ? 
        PastureStatus.DISPONIBLE : 
        PastureStatus.EN_DESCANSO;
    
    EntityPatch patch = EntityPatch.of()
        .set("status", nextStatus)
        .set("substatus", PastureSubstatus.NINGUNO)
        .set("gsi2pk", "farm#" + pasture.getFarmId() + "#blocked#false")
        .set("gsi2sk", eta)
        .remove("holdUntilIso")  // ← Nota: remove holdUntil
        .remove("blockReason");
    
    return patch;
}
```

**Patch Resultante**:
```
EntityPatch {
  set: {
    "status": PastureStatus.DISPONIBLE,
    "substatus": PastureSubstatus.NINGUNO,
    "gsi2pk": "farm#F001#blocked#false",
    "gsi2sk": 5
  },
  remove: ["holdUntilIso", "blockReason"]
}
```

---

## Ventajas y Casos de Uso

### ✅ Ventajas

| Ventaja | Explicación |
|---------|-------------|
| **Eficiencia** | Solo se persisten cambios, no toda la entidad. |
| **Auditoría** | Fácil ver qué SET y qué REMOVE exactamente. |
| **Atomicidad** | Un UpdateItem persiste todos los cambios. |
| **Tipo-Safe** | EntityPatch.of().set().remove() es seguro. |
| **Transaccional** | Cambios correlacionados se aplican juntos. |

### 🎯 Casos de Uso

1. **Estado de máquina**: Transiciones de estado complejas.
2. **Auditoría**: Registrar exactamente qué cambió.
3. **Concurrencia**: Cambios parciales minimizan conflictos.
4. **Performance**: Menos datos persisten por cambio.
5. **Replicación**: Fácil enviar solo deltas a otros sistemas.

### 🚀 Patrón en Arquitectura

```
┌────────────────────┐
│  Evento (Input)    │  OpenEvent { user, lotId, animals }
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│   StatusEngine     │  applyEvent() → EntityPatch
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│  EntityPatch       │  { set: {...}, remove: [...] }
└─────────┬──────────┘
          │
    ┌─────┴──────┐
    │            │
    ▼            ▼
┌────────┐   ┌──────────────┐
│ Memory │   │  DynamoDB    │
│ (Local)│   │  (Persisted) │
└────────┘   └──────────────┘
    │            │
    └─ PatchApplier.applyLocal()
    └─ Repository.update()
```

---

## 📝 Convenciones

### Nombres de Atributos en Patch

- `status` → `PastureStatus` enum
- `substatus` → `PastureSubstatus` enum
- `holdUntilIso` → ISO date string (YYYY-MM-DD)
- `lastUseAtIso` → ISO date string
- `gsi2pk`, `gsi2sk` → GSI keys

### Fluent API

```java
EntityPatch patch = EntityPatch.of()
    .set("key1", value1)
    .set("key2", value2)
    .remove("key3")
    .remove("key4");
```

---

## 🔗 Referencias

- [events-overview.md](events-overview.md): Comparación de sistemas.
- [sealed-interface-pattern.md](sealed-interface-pattern.md): Eventos type-safe.
- [docs/pastures/events-architecture.md](../pastures/events-architecture.md): Ejemplo completo.

---

**Generado**: 2026-01-09 | **Versión**: 1.0
