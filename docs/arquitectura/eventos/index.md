# 📌 Sistema de Eventos del Proyecto Cattle

**Última actualización**: 2026-01-09

## 📚 Documentación de Eventos

Este directorio contiene la documentación completa del sistema de eventos utilizados en el proyecto Cattle. El proyecto implementa **múltiples patrones de eventos** para diferentes casos de uso.

---

## 📖 Documentos

### 1. [events-overview.md](events-overview.md)
**Visión General de Todos los Sistemas de Eventos**

Análisis comparativo de los dos sistemas principales:
- 📌 **Event.java + EventBuilder.java**: Sistema genérico para registrar actividades de pastoreo.
- 📌 **PastureEvent + Records**: Sistema type-safe para cambiar estado de potreros.
- 🔄 EntityPatch y PatchApplier: Cómo se aplican cambios.
- 📊 Tabla comparativa (cuándo usar cada uno).
- 🏗️ Arquitectura general.

**Público**: Desarrolladores, arquitectos.

**Longitud**: ~400 líneas.

---

### 2. [sealed-interface-pattern.md](sealed-interface-pattern.md)
**Patrón Sealed Interface + Records (Java 17+)**

Profundización en la arquitectura type-safe de eventos:
- ✅ Qué es una sealed interface y sus beneficios.
- 📚 PastureEvent y sus 4 implementaciones (OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent).
- 🔀 Pattern matching y switch expression (Java 17+).
- 💡 Por qué usar records (inmutabilidad, concisión).
- 🎓 Aprendizajes clave sobre type-safety.

**Público**: Desarrolladores backend (Java), architects.

**Longitud**: ~600 líneas (documentación más detallada en [docs/pastures/events-architecture.md](../pastures/events-architecture.md)).

---

### 3. [generic-events-builder.md](generic-events-builder.md)
**Sistema Genérico: Event.java + EventBuilder.java**

Documentación sobre el patrón tradicional de eventos:
- 🏗️ Patrón Builder implementado.
- 📋 Event como POJO Bean persistible.
- 📊 Campos y estructura.
- 📦 Casos de uso (registrar actividades de pastoreo).
- 🔍 Diferencias con PastureEvent.

**Público**: Desarrolladores backend.

**Longitud**: ~350 líneas.

---

### 4. [entity-patch-pattern.md](entity-patch-pattern.md)
**Patrón EntityPatch: Aplicar Cambios Incrementales**

Sistema para registrar y aplicar cambios parciales:
- 📝 EntityPatch record: estructura para cambios SET y REMOVE.
- 🔨 PatchApplier: aplicador local de cambios en memoria.
- 🔄 Flujo: evento → engine genera patch → se aplica → se persiste.
- 💪 Ventajas: cambios parciales, auditoría, transacciones.

**Público**: Desarrolladores backend.

**Longitud**: ~300 líneas.

---

## 🗺️ Mapa Conceptual

```
┌─────────────────────────────────────────────────────────────┐
│                 SISTEMA DE EVENTOS CATTLE                   │
└─────────────────────────────────────────────────────────────┘

┌──────────────────┐                ┌──────────────────┐
│  Eventos        │                │  Eventos         │
│  Genéricos      │                │  Type-Safe       │
│  (Actividades)  │                │  (Cambios Estado)│
└──────────────────┘                └──────────────────┘
        │                                    │
        ├─ Event.java                       ├─ PastureEvent (sealed)
        │  POJO Bean                        │  Interface
        │  Persistible en DynamoDB          │  
        ├─ EventBuilder.java                ├─ OpenEvent (record)
        │  Builder Pattern                  │  CloseEvent (record)
        │  (fluent API)                     │  MaintenanceSetEvent (record)
        │                                   │  MaintenanceClearEvent (record)
        └─ Casos: GRAZING_END, ...         └─ Casos: Transiciones de potrero
                                                 
                                            │
                                            ├─ EventType (enum)
                                            │  OPEN, CLOSE, MAINTENANCE_SET, ...
                                            │
                                            └─ PastureStatusEngine
                                               applyEvent() → EntityPatch
                                               
┌──────────────────────────────────────────────────────────────┐
│  Patrón de Cambios Incrementales                             │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Event → StatusEngine → EntityPatch → PatchApplier          │
│                           │              │                   │
│                           │              └─ Aplicar en memoria
│                           │                                  │
│                           └─ { set: {...}, remove: [...] }  │
│                              Cambios parciales               │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 🎯 Guía Rápida: Cuándo Usar Qué

### Event.java
**Usa cuando:**
- Necesitas **registrar una actividad** (ej. "se completó pastoreo").
- Los datos deben **persistirse** en DynamoDB para auditoría.
- Es un **evento histórico** (no cambia estado de otra entidad).
- Necesitas **builder pattern** tradicional.

**Ejemplo**: Registrar evento GRAZING_END después de cerrar un potrero.

### PastureEvent
**Usa cuando:**
- Necesitas **cambiar el estado** de una entidad (potrero).
- Quieres **type-safety** en compile-time.
- Los datos son **transientes** (en memoria).
- Usarás **pattern matching** (Java 17+).

**Ejemplo**: Abrir potrero (evento OPEN → transición EN_DESCANSO → EN_USO).

### EntityPatch
**Usa cuando:**
- Necesitas aplicar **cambios parciales** a una entidad.
- Quieres **auditoría** de qué cambió exactamente.
- Tienes **múltiples cambios** de diferentes fuentes.

**Ejemplo**: Patch de { status: "EN_USO", lastUseAt: "2026-01-09" }.

---

## 📊 Tabla Comparativa Rápida

| Aspecto | Event.java | PastureEvent | EntityPatch |
|---------|-----------|------------|------------|
| **Patrón** | Builder | Sealed Interface | Record |
| **Persistible** | ✅ Sí | ❌ No | ❌ No (resultado) |
| **Type-Safe** | ❌ Strings | ✅ Sealed interface | ✅ Record |
| **Inmutable** | ❌ Setters | ✅ Records | ✅ Record |
| **Propósito** | Auditoría | Transición estado | Aplicar cambios |
| **Consumidor** | ? | PastureStatusEngine | Repository.update() |

---

## 🔗 Referencias Cruzadas

- **Pastures (Potreros)**: [docs/pastures/](../pastures/)
  - Usa PastureEvent y EntityPatch
  - Documentación en [events-architecture.md](../pastures/events-architecture.md)

- **Bovines (Bovinos)**: [docs/](../)(TO-DO)
  - Posible uso de Event.java

- **Milking (Lactancia)**: [docs/](../)(TO-DO)
  - Posible registración de eventos

---

## 🚀 Próximos Pasos

1. ✅ Documentar Event.java y EventBuilder.java (este documento)
2. ✅ Documentar PastureEvent (sealed interface + records)
3. ✅ Documentar EntityPatch y PatchApplier
4. ⏳ Implementar PastureEventController (tarea P0 en pastures)
5. ⏳ Crear tests para cada patrón

---

## 📞 Preguntas Frecuentes

**P: ¿Cuál es la diferencia entre Event.java y PastureEvent?**
R: Event.java es para **registrar actividades** (histórico persistido). PastureEvent es para **cambiar estado** (transaccional, transiente). Ver [events-overview.md](events-overview.md).

---

**P: ¿Por qué usar sealed interfaces en lugar de enums?**
R: Sealed interfaces permiten que cada tipo tenga **propiedades distintas** (OpenEvent tiene `lotId`, MaintenanceSetEvent tiene `holdUntil`). Enums no permiten esto. Ver [sealed-interface-pattern.md](sealed-interface-pattern.md).

---

**P: ¿Cómo se aplican los cambios en memoria?**
R: EntityPatch retorna cambios (SET y REMOVE), luego PatchApplier los aplica a la entidad en memoria. Ver [entity-patch-pattern.md](entity-patch-pattern.md).

---

## 📝 Convenciones

### Nombres de Eventos
- **GenericEvent**: GRAZING_END, FEED_CHANGE, etc.
- **PastureEvent**: OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR

### Estructura de Patch
```java
EntityPatch patch = EntityPatch.of()
    .set("status", "EN_USO")
    .set("lastUseAt", "2026-01-09")
    .remove("holdUntil");
```

### Consumo de Eventos
```java
// Sync pattern matching
switch (event) {
    case OpenEvent e -> { /* handle */ }
    case CloseEvent e -> { /* handle */ }
    // ... exhaustive
}
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
