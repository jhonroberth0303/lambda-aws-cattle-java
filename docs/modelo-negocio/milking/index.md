# 🥛 Módulo Milking (Lactancia): Documentación Completa

**Fecha**: 2026-01-09

## 📚 Índice

1. [milkingRecord-overview.md](milkingRecord-overview.md) - Visión técnica general
2. [components-frontend.md](components-frontend.md) - Componentes React con código fuente
3. [implementation-guide.md](implementation-guide.md) - Guía de implementación paso a paso
4. [tasks-pending.md](tasks-pending.md) - 15 tareas priorizadas + roadmap

---

## 🎯 Descripción Rápida

**Milking** es el módulo que gestiona el registro de **lactancia diaria** de bovinos:
- 📝 Registrar litros de leche (AM/PM)
- 📊 Ver histórico por bovino
- 📈 Analizar tendencias de producción
- 🔔 Alertas de desempeño

**Estado**: 70% implementado
- ✅ Backend funcional (GET, POST)
- ✅ Frontend básico (tabla, agregar)
- ❌ Validaciones mejoradas
- ❌ Reportes y estadísticas
- ❌ Alertas automáticas

---

## 📊 Características Principales

| Feature | Estado | Prioridad |
|---------|--------|-----------|
| Listar registros por bovino | ✅ Implementado | P0 |
| Agregar nuevo registro | ✅ Implementado | P0 |
| Ver histórico (AM/PM) | ✅ Implementado | P0 |
| Buscar por bovino | ✅ Implementado | P1 |
| Filtrar por turno (AM/PM) | ✅ Implementado | P1 |
| Editar registro | ❌ Falta | P1 |
| Eliminar registro | ❌ Falta | P1 |
| Validaciones mejoradas | ⚠️ Básicas | P1 |
| Exportar a CSV | ❌ Falta | P2 |
| Gráficos de tendencia | ❌ Falta | P2 |
| Alertas automáticas | ❌ Falta | P2 |
| Reportes diarios | ❌ Falta | P3 |

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────┐
│           MILKING FRONTEND (React)                  │
├─────────────────────────────────────────────────────┤
│                                                     │
│  MilkingPage.jsx                                   │
│   ├─ MilkingAdd.jsx (Accordion form)               │
│   └─ MilkingTable.jsx (Table con registros)        │
│                                                     │
│  Hooks:                                            │
│   ├─ useMilkingRecords (fetch + state)             │
│   └─ useMilkingForm (form + submit)                │
│                                                     │
│  Services:                                         │
│   ├─ getMilkingByBovineId(bovineId)                │
│   └─ addMilkingRecord(payload)                     │
│                                                     │
└────────────────┬─────────────────────────────────────┘
                 │ HTTP API
┌────────────────▼─────────────────────────────────────┐
│         MILKING BACKEND (Spring Boot)                │
├─────────────────────────────────────────────────────┤
│                                                     │
│  MilkingController.java                            │
│   POST   /milkingRecord                 (crear)          │
│   GET    /milkingRecord/{idBovine}      (listar)         │
│   GET    /milkingRecord/{idBovine}?shift=AM  (filtrar)   │
│                                                     │
│  MilkingProcessor.java                             │
│   ├─ getMilkingData(idBovine, shift)               │
│   └─ createMilking(dto)                            │
│                                                     │
│  MilkingService.java                               │
│   ├─ save(FarmMilking)                             │
│   └─ getMilkingByPk(pk)                            │
│                                                     │
│  MilkingRepository.java                            │
│   ├─ save(entity) → TABLE_FARM_MILKING             │
│   └─ getMilkingByPk(pk) → Query                    │
│                                                     │
└────────────────┬──────────────────────────────────────┘
                 │ DynamoDB
┌────────────────▼──────────────────────────────────────┐
│   TABLE_FARM_MILKING (DynamoDB)                     │
│                                                     │
│   PK: BOVINE#{bovineId}                            │
│   SK: MILKING#{date}#{shift}                       │
│                                                     │
│   Atributos:                                       │
│   - bovineId, date, shift, liters                  │
│   - status, observations, recordedBy               │
│   - createdAt, updatedAt                           │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 🔑 Claves DynamoDB

### Tabla: TABLE_FARM_MILKING

```
PK: BOVINE#{bovineId}
    BOVINE#5
    BOVINE#10
    BOVINE#42

SK: MILKING#{date}#{shift}
    MILKING#2025-12-10#AM
    MILKING#2025-12-10#PM
    MILKING#2025-12-11#AM
```

**Query Ejemplos**:
```bash
# Obtener todos los registros de un bovino
Query PK = BOVINE#5

# Obtener solo los de la mañana
Query PK = BOVINE#5 AND SK starts_with "MILKING#"
```

---

## 📋 Entidad: FarmMilking

```java
@DynamoDbBean
public class FarmMilking {
    
    @Getter @Setter
    @DynamoDbPartitionKey
    private String PK;              // BOVINE#{bovineId}
    
    @Getter @Setter
    @DynamoDbSortKey
    private String SK;              // MILKING#{date}#{shift}
    
    @Getter @Setter
    private Integer bovineId;       // 5, 10, 42, ...
    
    @Getter @Setter
    private String date;            // YYYY-MM-DD (2025-12-10)
    
    @Getter @Setter
    private String shift;           // "AM" o "PM"
    
    @Getter @Setter
    private Double liters;          // 15.5, 12.3, ...
    
    @Getter @Setter
    private String status;          // "completo", "parcial", "omitido"
    
    @Getter @Setter
    private String observations;    // Notas opcionales
    
    @Getter @Setter
    private String recordedBy;      // Usuario que registró
    
    @Getter @Setter
    private String createdAt;       // ISO timestamp
    
    @Getter @Setter
    private String updatedAt;       // ISO timestamp
}
```

---

## 🔄 Flujos Principales

### Flujo 1: Registrar Lectura de Lactancia

```
1. Usuario abre formulario "Nuevo registro"
   ↓
2. Selecciona bovino (dropdown)
   ↓
3. Ingresa:
   - Fecha (date picker)
   - Turno (AM/PM)
   - Litros (number)
   - Estado (completo/parcial/omitido)
   - Observaciones (optional)
   ↓
4. Click "Guardar"
   ↓
5. POST /milkingRecord con payload
   {
     "bovineId": 5,
     "date": "2025-12-10",
     "shift": "AM",
     "liters": 15.5,
     "status": "completo",
     "observations": "Normal",
     "recordedBy": "jhonroberth"
   }
   ↓
6. Backend:
   - Valida campos (bovineId, date, shift)
   - Genera PK = "BOVINE#5"
   - Genera SK = "MILKING#2025-12-10#AM"
   - Guarda en DynamoDB
   ↓
7. Response 200 OK
   ↓
8. Frontend refetch datos
   ↓
9. Tabla actualizada
```

### Flujo 2: Ver Histórico de Bovino

```
1. Usuario selecciona bovino en dropdown
   ↓
2. Click "Buscar"
   ↓
3. GET /milkingRecord/5
   ↓
4. Backend query:
   PK = "BOVINE#5"
   ↓
5. Retorna lista de FarmMilking
   [
     { date: "2025-12-10", shift: "AM", liters: 15.5, ... },
     { date: "2025-12-10", shift: "PM", liters: 14.2, ... },
     { date: "2025-12-11", shift: "AM", liters: 16.0, ... },
     ...
   ]
   ↓
6. Frontend agrupa por fecha
   ↓
7. Tabla muestra:
   Fecha | Mañana | Obs AM | Tarde | Obs PM | Total
   2025-12-10 | 15.5 L | Normal | 14.2 L | Normal | 29.7 L
   2025-12-11 | 16.0 L | Normal | — | — | 16.0 L
```

---

## 🧩 Componentes

### MilkingPage.jsx
Orquestador principal, coordina MilkingAdd y MilkingTable.

### MilkingAdd.jsx
Formulario en accordion para agregar nuevos registros.

### MilkingTable.jsx
Tabla que agrupa registros por fecha, AM/PM.

### BovineSelect.jsx
Dropdown reutilizable para seleccionar bovino.

---

## 🔗 Referencias

- [milkingRecord-overview.md](milkingRecord-overview.md) - Visión técnica detallada
- [components-frontend.md](components-frontend.md) - Componentes con código fuente
- [implementation-guide.md](implementation-guide.md) - Guía de implementación
- [tasks-pending.md](tasks-pending.md) - Tareas priorizadas

---

## 🚀 Próximos Pasos

**Corto Plazo (P0-P1)**:
- ✅ Backend funcional
- ✅ Frontend básico
- [ ] Validaciones mejoradas
- [ ] Editar/eliminar registros

**Mediano Plazo (P2)**:
- [ ] Exportar CSV
- [ ] Gráficos de tendencia
- [ ] Alertas automáticas

**Largo Plazo (P3)**:
- [ ] Reportes diarios
- [ ] Integración con datos de producción
- [ ] Machine learning para predección

---

**Generado**: 2026-01-09 | **Versión**: 1.0
