# 🌱 MÓDULO: POTREROS Y ROTACIÓN (Pastures)

**Última actualización**: 2026-01-08

## 📋 Descripción General

Sistema integral de **gestión inteligente de potreros y rotación de pasturas** con soporte para múltiples especies forrajeras. El motor automático calcula disponibilidad (ETA), gestiona transiciones de estado y optimiza la rotación basada en parámetros biológicos (altura de pasto, días de descanso, tasa de crecimiento).

### Características Principales

- ✅ **Dashboard Visual**: KPIs en tiempo real, tabla interactiva, semáforo de disponibilidad
- ✅ **Motor de Rotación Automático**: Cálculo dinámico de ETA y transiciones de estado
- ✅ **Múltiples Especies**: Gestión de diferentes especies forrajeras con planes independientes
- ✅ **Estados Automáticos**: Transiciones basadas en eventos y reglas de negocio
- ✅ **Filtros Avanzados**: Por especie, estado, búsqueda textual
- ✅ **Calendarios**: Mini-calendario para visualización de rotaciones
- ✅ **Centro de Alertas**: Notificaciones de eventos relevantes
- ✅ **Panel Detallado**: Vista completa de propiedades y estado de cada potrero

---

## 🗄️ Modelo de Datos

### Tablas DynamoDB

#### 1. `TABLE_PASTURE`

**Propósito**: Almacenar estado actual de potreros y sus propiedades.

```javascript
{
  // === KEYS ===
  pk: "PASTURE#P001",                      // Partition Key (PK)
  gsi1pk: "FARM#F001#SPECIES#Kikuyo",     // GSI1 PK (por especie)
  gsi1sk: 15,                              // GSI1 SK (ETA en días)
  gsi2pk: "FARM#F001",                     // GSI2 PK (por finca)
  gsi2sk: 15,                              // GSI2 SK (ETA en días)
  
  // === IDENTIFICACIÓN ===
  farmId: "F001",
  id: "P001",
  name: "Potrero Principal",
  
  // === CARACTERÍSTICAS FÍSICAS ===
  species: "Kikuyo",                       // Especie forrajera
  areaHa: 2.5,                             // Hectáreas
  establishmentDate: "2023-01-15",         // Fecha de establecimiento
  
  // === ESTADO ACTUAL ===
  status: "EN_DESCANSO",                   // EN_DESCANSO | EN_USO | DISPONIBLE | MANTENIMIENTO
  substatus: "NINGUNO",                    // Subestado específico (bloques, razones)
  currentHeightCm: 25,                     // Altura actual del pasto
  lastUseAt: "2025-11-20",                 // Última fecha de uso
  
  // === GESTIÓN Y CONTROL ===
  holdUntil: null,                         // Fecha de bloqueo (si aplica)
  blockReason: null,                       // Razón del bloqueo
  notes: "Observaciones...",
  
  // === METADATA ===
  createdAt: "2023-01-15T08:00:00Z",
  updatedAt: "2025-12-08T14:30:00Z"
}
```

**Índices Globales Secundarios (GSI)**:

| Nombre | PK | SK | Propósito |
|--------|----|----|-----------|
| `gsi1-species-eta` | `FARM#<farmId>#SPECIES#<species>` | `<etaOpenDays>` | Listar potreros por especie ordenados por ETA |
| `gsi2-farm-blocked-eta` | `FARM#<farmId>` | `<etaOpenDays>` | Listar potreros de una finca (incluyendo estado bloqueado) |

#### 2. `TABLE_PLAN` (Planes de Rotación)

**Propósito**: Almacenar parámetros de rotación por especie y finca.

```javascript
{
  // === KEYS ===
  pk: "PLAN#FARM#F001#SPECIES#Kikuyo",
  
  // === IDENTIFICACIÓN ===
  farmId: "F001",
  species: "Kikuyo",
  
  // === PARÁMETROS BIOLÓGICOS ===
  restDays: 30,                            // Días de descanso requeridos
  minHeightCm: 20,                         // Altura mínima para pastoreo
  optimalHeightCm: 30,                     // Altura óptima
  growthRateCmPerDay: 2.5,                 // Tasa de crecimiento en cm/día
  
  // === METADATA ===
  createdAt: "2023-01-15T08:00:00Z",
  updatedAt: "2025-12-08T14:30:00Z",
  notes: "Plan de rotación estándar para Kikuyo"
}
```

---

## 🔄 Máquina de Estados

### Diagrama de Transiciones

```
                    ┌─────────────────┐
                    │   EN_DESCANSO   │ ◄────────────────┐
                    └────────┬────────┘                  │
                             │                          │
                    ┌────────▼────────┐      ┌──────────┴────────┐
                    │ DISPONIBLE (ETA │      │ Motor: Tick       │
                    │     <= 0)       │      │ - ETA cálculado   │
                    └────────┬────────┘      │ - Estado derivado │
                             │               └───────────────────┘
                    ┌────────▼────────┐
                    │    EN_USO       │ (OPEN event)
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  MANTENIMIENTO  │ (MAINTENANCE_SET)
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │ holdUntil       │
                    │ expirado?       │
                    └─────────────────┘
                             │
                    ┌────────▼────────┐
                    │ Regresa a       │
                    │ EN_DESCANSO o   │
                    │ DISPONIBLE      │
                    └─────────────────┘
```

### Estados

| Estado | Descripción |
|--------|-------------|
| `EN_DESCANSO` | Potrero en período de recuperación, no disponible para pastoreo. |
| `DISPONIBLE` | Potrero listo para abrir (ETA <= 0, altura suficiente). |
| `EN_USO` | Potrero actualmente siendo usado por rebaño. |
| `MANTENIMIENTO` | Potrero bloqueado (fertilización, reparación, etc.), con fecha de liberación. |

### Subestados

| Subestado | Descripción |
|-----------|-------------|
| `NINGUNO` | Sin bloqueos o restricciones adicionales. |
| `FERTILIZANDO` | En proceso de fertilización. |
| `REPARANDO` | En reparación o mantenimiento físico. |
| `CUARENTENA` | Bloqueado por razones sanitarias. |

---

## 🧮 Motor de Rotación (PastureStatusEngine)

### Fórmula de ETA (Estimated Time to Availability)

```
ETA = restDays - daysSinceLastUse + (minHeightCm - currentHeightCm) / growthRateCmPerDay
```

**Componentes**:
- **restDays**: Días de descanso requeridos según plan (p. ej. 30 días).
- **daysSinceLastUse**: Días transcurridos desde última fecha de uso.
- **Deficit de altura**: Calculado como el tiempo necesario para alcanzar altura mínima.

**Ejemplo**:
- Plan: `restDays=30, minHeightCm=20, growthRateCmPerDay=2.5`
- Estado: `currentHeightCm=10, lastUseAt=2025-11-15` (15 días ago)
- Cálculo: `ETA = 30 - 15 + (20 - 10) / 2.5 = 15 + 4 = 19 días`

### Lógica de "Tick" Automático

En cada consulta GET (`getRotationSemaphore`):

1. **Obtener potreros** de la finca.
2. **Obtener planes** de rotación por especie.
3. **Para cada potrero**:
   - Calcular ETA según fórmula.
   - Ejecutar "tick" del motor: evaluar transiciones automáticas.
   - Si hay cambios, aplicar `EntityPatch` a DynamoDB.
   - Generar DTO con estado efectivo.
4. **Retornar lista** de DTOs con estado actual.

### Métodos Principales

#### `applyEvent(Pasture, Plan, PastureEvent ev)`
Aplica un evento (instancia de sealed interface PastureEvent) y retorna cambios (`EntityPatch`).

```java
public EntityPatch applyEvent(Pasture pasture, Plan plan, PastureEvent ev) {
    switch(ev.type()) {
        case OPEN -> {
            // Validar no bloqueado, transición a EN_USO
        }
        case CLOSE -> {
            // Transición a EN_DESCANSO, guardar residualCm
        }
        case MAINTENANCE_SET -> {
            // Bloquear con substatus y holdUntil
        }
        case MAINTENANCE_CLEAR -> {
            // Liberar, calcular nuevo estado
        }
    }
}
```

**Casos por tipo**:
- **OPEN** (`OpenEvent`): Transición EN_DESCANSO/DISPONIBLE → EN_USO. Valida que no esté bloqueado. Guarda `lastUseAt`.
- **CLOSE** (`CloseEvent`): Transición EN_USO → EN_DESCANSO. Guarda `residualCm` (altura después de pastar).
- **MAINTENANCE_SET** (`MaintenanceSetEvent`): Transición a MANTENIMIENTO. Asigna `substatus` y `holdUntil`. Bloquea búsquedas.
- **MAINTENANCE_CLEAR** (`MaintenanceClearEvent`): Libera bloqueo, calcula nuevo estado según ETA.

#### `autoUpdateStatusTickByHoldUntil(Pasture, Plan)`
Verifica si `holdUntil` ha expirado y automáticamente libera el bloqueo.

#### `deriveEffectiveStatus(Pasture, etaOpenDays)`
Deriva el estado efectivo basado en lógica priorizada:
1. Si `EN_USO` → retorna `EN_USO`.
2. Si bloqueado efectivamente → retorna `MANTENIMIENTO`.
3. Si `etaOpenDays <= 0` → retorna `DISPONIBLE`.
4. Si no → retorna `EN_DESCANSO`.

#### `isBlockedEffective(Pasture)`
Comprueba si el potrero está bloqueado (tiene subestado ≠ NINGUNO).

---

## 🎨 Frontend - Arquitectura y Componentes

### Árbol de Componentes

```
PaddockPage
├── Header
│   ├── Título
│   └── Subtítulo
├── ListKpiCards
│   ├── KpiCard (Hectáreas totales)
│   ├── KpiCard (Disponibles)
│   ├── KpiCard (En uso)
│   └── KpiCard (En descanso)
├── Section (Filtros)
│   └── Filtros
│       ├── Especie (select)
│       ├── Estado (select)
│       └── Búsqueda (input)
├── RotationSemaphore
│   └── Semáforo visual de disponibilidad
├── Section (Tabla)
│   └── PastureTable
│       ├── Cabecera (ID, Nombre, Especie, Área, Estado, etc.)
│       └── Filas (uno por potrero)
│           └── Botones de acción (Detalle, Abrir, Cerrar)
├── Section (Calendario)
│   └── CalendarMini
│       └── Vista de rotaciones por mes
├── Section (Alertas)
│   └── AlertCenter
│       └── Notificaciones de eventos
└── DetailPanel (modal/drawer)
    └── Detalles completos del potrero seleccionado
```

### Componentes Principales

#### `PaddockPage.jsx`
**Ubicación**: `cattle-front/src/components/Paddock/page/PaddockPage.jsx`

**Responsabilidad**: Componente raíz del dashboard. Gestiona estado global de filtros, pastures seleccionados, y coordina fetch de datos.

**Props**: Ninguna.

**Estado Local**:

**Datos Fetched**:
```
GET https://44xpamzadd.execute-api.us-east-1.amazonaws.com/Prod/farms/F001/pastures
```

**Flujo**:
1. En `useEffect`, fetch lista de potreros.
2. Calcula KPIs en `useMemo` (hectáreas, disponibles, en uso, en descanso).
3. Filtra lista según `species`, `status`, `query` usando hook `useFilteredPastures`.
4. Renderiza secciones: header, KPIs, filtros, semáforo, tabla, calendario, alertas, detalle.

#### `PastureTable.jsx`
**Ubicación**: `cattle-front/src/components/Paddock/pastureTable/pastureTable.jsx`

**Responsabilidad**: Renderiza tabla HTML con filas de potreros.

**Props**:
- `rows` (array): Lista de potreros a mostrar.
- `onOpen` (function): Callback al hacer clic en "Detalle".

**Columnas Mostradas**:
| Columna | Origen |
|---------|--------|
| ID | `p.pastureId` |
| Nombre | `p.name` |
| Especie | `p.species` (Chip) |
| Área (ha) | `p.areaHa` |
| Estado | `p.status` (StatusChip) |
| Último uso | `p.lastUseAt` |
| Descanso (d) | `p.daysRest` |
| ETA (d) | `p.etaOpenDays` |
| Altura (cm) | `p.currentHeightCm` |
| Residual (cm) | `p.residualPrevCm` |
| Acciones | Botones (Detalle, Abrir, Cerrar) |

**Acciones**:
- **Detalle**: Abre panel lateral con detalles completos.
- **Abrir**: Dispara evento OPEN (transición EN_DESCANSO → EN_USO).
- **Cerrar**: Dispara evento CLOSE (transición EN_USO → EN_DESCANSO).

#### `ListKpiCards.jsx`
**Ubicación**: `cattle-front/src/components/Paddock/page/listKpiCards.jsx`

**Responsabilidad**: Renderiza grid de tarjetas KPI.

**Props**:
- `kpis` (array): Lista de objetos `{ title, value }`.

**Ejemplo de KPIs calculados en PaddockPage**:
```javascript
const kpiList = useMemo(() => [
  { title: "Hectáreas totales", value: `${total}ha` },
  { title: "Potreros disponibles", value: count },
  { title: "En uso", value: count },
  { title: "En descanso", value: count }
], [pastures]);
```

#### `StatusChip.jsx`
**Ubicación**: `cattle-front/src/components/Paddock/StatusChip/StatusChip.jsx`

**Responsabilidad**: Renderiza etiqueta de estado con color asociado.

**Props**:
- `status` (string): Nombre del estado (p. ej. "EN_USO", "DISPONIBLE").

**Mapeo de Colores**:
- `EN_DESCANSO`: Gris
- `DISPONIBLE`: Verde
- `EN_USO`: Azul
- `MANTENIMIENTO`: Rojo

#### `RotationSemaphore.jsx`
**Ubicación**: `cattle-front/src/components/Paddock/rotationSemaphore/rotationSemaphore.jsx`

**Responsabilidad**: Renderiza semáforo visual (rojo/amarillo/verde) de disponibilidad general.

**Lógica**:
- Verde: Hay potreros disponibles.
- Amarillo: Algunos disponibles, pero pocos.
- Rojo: Ninguno disponible o crítico.

#### `CalendarMini.jsx`
**Ubicación**: `cattle-front/src/components/Paddock/calendarMini/calendarMini.jsx`

**Responsabilidad**: Mini calendario interactivo que muestra rotaciones proyectadas.

#### `AlertCenter.jsx`
**Ubicación**: `cattle-front/src/components/Paddock/alertCenter/alertCenter.jsx`

**Responsabilidad**: Centro de notificaciones con eventos de rotación y alertas.

#### `DetailPanel.jsx`
**Ubicación**: `cattle-front/src/components/Paddock/detailPanel/detailPanel.jsx`

**Responsabilidad**: Panel lateral (drawer) con detalles completos del potrero seleccionado.

### Custom Hooks

#### `useFilteredPastures(pastures, species, status, query)`
**Ubicación**: `cattle-front/src/components/Paddock/hooks/padockHooks.js`

**Responsabilidad**: Filtra lista de potreros según criterios.

**Filtros Aplicados**:
1. Por especie (si ≠ "ALL").
2. Por estado (si ≠ "ALL").
3. Por búsqueda textual en nombre/notas.

**Retorno**: Array filtrado.

### Constantes

#### `paddockSelectOptions.js`
**Ubicación**: `cattle-front/src/components/Paddock/paddockConstants/paddockSelectOptions.js`

**Contenido**:
```javascript
export const SPECIES_OPTIONS = [
  { value: "ALL", label: "Todas" },
  { value: "Kikuyo", label: "Kikuyo" },
  { value: "Pasto azul", label: "Pasto azul" },
  // ...
];

export const STATUS_OPTIONS = [
  { value: "ALL", label: "Todos" },
  { value: "EN_DESCANSO", label: "En descanso" },
  { value: "DISPONIBLE", label: "Disponible" },
  { value: "EN_USO", label: "En uso" },
  { value: "MANTENIMIENTO", label: "Mantenimiento" },
];
```

---

---

## 📌 Nota Importante: Distinción entre Sistemas de Eventos

### `Event.java` vs `PastureEvent`

El proyecto tiene **dos sistemas de eventos independientes**:

#### 1. **Event.java** (Eventos Genéricos de Pastoreo)
- **Ubicación**: `entities/Event.java`, `builders/EventBuilder.java`
- **Propósito**: Registrar eventos de pastoreo (GRAZING_END) con datos de actividad.
- **Estructura**: POJO Bean con Lombok, usable en DynamoDB.
- **Tabla**: Probablemente `TABLE_EVENTS` (no usado en Pastures).
- **Campos**: `pk`, `sk`, `gsi1pk`, `gsi1sk`, `eventType`, `eventAt`, `animals`, `residualCm`, `user`.
- **Patrón**: Builder pattern tradicional con `EventBuilder.java`.

#### 2. **PastureEvent** (Eventos de Rotación de Potreros)
- **Ubicación**: `events/PastureEvent.java`, `events/OpenEvent.java`, `events/CloseEvent.java`, etc.
- **Propósito**: Eventos que cambian el estado de un potrero (OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR).
- **Estructura**: Sealed interface + records (Java 17+).
- **Consumidor Principal**: `PastureStatusEngine.applyEvent()`.
- **Campos**: Varían por tipo de evento (records).
- **Patrón**: Sealed interface pattern (type-safe, exhaustive checking).

**Relación**: 
- `Event` = Actividad de pastoreo registrada (histórico).
- `PastureEvent` = Cambio de estado de rotación (transaccional).

**Para el módulo Pastures**: Se usa **PastureEvent**, NO `Event.java`.

---

### Flujo de Datos

```
HTTP Request (GET /farms/F001/pastures)
        ↓
PasturesController.getRotationSemaphore(farmId)
        ↓
RotationPlanProcessor.getRotationSemaphoreItems(farmId)
        ↓
PastureService.getPastures(farmId) + PlanService.getPlans(farmId)
        ↓
PastureRepository.findPastures(farmId) [GSI2: FARM#F001]
PlanRepository.findPlansByFarmAndSpecies(farmId, species)
        ↓
DynamoDB: TABLE_PASTURE, TABLE_PLAN
        ↓
PastureStatusEngine.tick() para cada potrero
        ↓
EtaCalculator.etaOpenDays(pasture, plan)
        ↓
PasturesMapper.toDTOList(pastures) → RotationSemaphoreItemDTO
        ↓
HTTP Response (200 OK, lista de DTOs)
```

### Componentes Backend

#### `PasturesController.java`
**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/controller/PasturesController.java`

**Responsabilidad**: Exposición de endpoints REST.

**Endpoints**:
```
GET /farms/{farmId}/pastures
```

**Retorno**: `List<RotationSemaphoreItemDTO>` (200 OK).

**Lógica**:
```java
@GetMapping
public ResponseEntity<List<RotationSemaphoreItemDTO>> getRotationSemaphore(
    @PathVariable("farmId") String farmId) {
  return ResponseEntity.ok(
    rotationPlanProcessor.getRotationSemaphoreItems(farmId).orElse(List.of())
  );
}
```

#### `RotationPlanProcessor.java`
**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/processor/RotationPlanProcessor.java`

**Responsabilidad**: Orquestación de lógica de rotación.

**Métodos Principales**:
- `getRotationSemaphoreItems(farmId)`: Coordina obtención de potreros/planes, aplica motor, mapea a DTOs.

#### `PastureProcessor.java`
**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/processor/PastureProcessor.java`

**Responsabilidad**: Procesamiento de operaciones sobre potreros.

**Métodos**:
- `listPastures(farmId)`: Lista potreros con mapeo a DTO.

#### `PastureService.java`
**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/services/PastureService.java`

**Responsabilidad**: Lógica de negocio para potreros.

**Métodos Principales**:
- `getPastures(farmId)`: Obtiene lista de potreros por finca.
- `applyPatch(pk, EntityPatch)`: Aplica cambios a un potrero.

#### `PastureRepository.java`
**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/repository/PastureRepository.java`

**Responsabilidad**: Acceso a datos en DynamoDB.

**Métodos**:
- `findPastures(farmId)`: Query en `TABLE_PASTURE` usando GSI2 (`FARM#farmId`).
- `save(pasture)`: PutItem.
- `update(pasture, patch)`: UpdateItem con cambios parciales.

#### `PastureStatusEngine.java`
**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/utils/PastureStatusEngine.java`

**Responsabilidad**: Máquina de estados y lógica de transiciones.

**Métodos Principales**:
- `applyEvent(pasture, plan, event)`: Aplica evento y retorna `EntityPatch`.
- `autoUpdateStatusTickByHoldUntil(pasture, plan)`: Verifica expiración de hold.
- `deriveEffectiveStatus(pasture, etaOpenDays)`: Calcula estado efectivo.
- `isBlockedEffective(pasture)`: Comprueba si está bloqueado.

**Lógica de Evento OPEN**:
```java
case "OPEN":
  if (isBlockedEffective(pasture)) {
    throw new IllegalStateException("No se puede abrir: potrero bloqueado");
  }
  entityPatch.set("status", PastureStatus.EN_USO.name());
  entityPatch.set("lastUseAt", LocalDate.now().toString());
  break;
```

#### `EtaCalculator.java`
**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/utils/EtaCalculator.java`

**Responsabilidad**: Cálculo de ETA.

**Fórmula Implementada**:
```java
public static int etaOpenDays(Pasture pasture, Plan plan) {
  int daysSinceLastUse = calculateDaysSince(pasture.getLastUseAt());
  int heightDeficit = Math.max(0, plan.getMinHeightCm() - pasture.getCurrentHeightCm());
  int daysForHeight = (int) Math.ceil((double) heightDeficit / plan.getGrowthRateCmPerDay());
  return plan.getRestDays() - daysSinceLastUse + daysForHeight;
}
```

#### `PasturesMapper.java`
**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/mapper/PasturesMapper.java`

**Responsabilidad**: Mapeo Entity ↔ DTO.

**Métodos**:
- `toDTO(Pasture, Plan, int etaOpenDays)`: Convierte a `RotationSemaphoreItemDTO`.
- `toDTOList(List<Pasture>)`: Mapea lista completa.

### DTOs

#### `RotationSemaphoreItemDTO.java`
**Campos**:
```java
private String pastureId;
private String name;
private String species;
private Double areaHa;
private String status;              // EN_DESCANSO, DISPONIBLE, etc.
private String lastUseAt;
private Integer daysRest;           // Días de descanso en plan
private Integer etaOpenDays;        // ETA calculado
private Integer currentHeightCm;
private Integer residualPrevCm;     // Altura residual anterior
private String notes;
private String blockReason;
```

### Entidades

#### `Pasture.java`
**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/entities/Pasture.java`

**Campos Principales**:
```java
@DynamoDbPartitionKey
private String pk;                  // PASTURE#P001

@DynamoDbSecondarySortKey(indexNames = "gsi1-species-eta")
private String gsi1pk;              // FARM#F001#SPECIES#Kikuyo

@DynamoDbSecondarySortKey(indexNames = "gsi2-farm-blocked-eta")
private String gsi2pk;              // FARM#F001

private String farmId;
private String id;
private String name;
private String species;
private String status;
private String substatus;
private Double areaHa;
private Integer currentHeightCm;
private String lastUseAt;
private String holdUntil;
private String blockReason;
private String notes;
```

#### `Plan.java`
**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/entities/Plan.java`

**Campos**:
```java
@DynamoDbPartitionKey
private String pk;                  // PLAN#FARM#F001#SPECIES#Kikuyo

private String farmId;
private String species;
private Integer restDays;           // Ej: 30
private Integer minHeightCm;        // Ej: 20
private Integer optimalHeightCm;    // Ej: 30
private Double growthRateCmPerDay;  // Ej: 2.5
private String notes;
```

### Enumeraciones

#### `PastureStatus.java`
```java
public enum PastureStatus {
  EN_DESCANSO,
  DISPONIBLE,
  EN_USO,
  MANTENIMIENTO,
  NINGUNO
}
```

#### `PastureSubstatus.java`
```java
public enum PastureSubstatus {
  NINGUNO,
  FERTILIZANDO,
  REPARANDO,
  CUARENTENA
}
```

### Eventos (Sealed Interface Pattern)

#### `PastureEvent.java` (Sealed Interface)
**Propósito**: Define contrato para todos los eventos de potrero usando Java sealed interfaces + records.

```java
public sealed interface PastureEvent permits OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent {
    EventType type();        // Retorna tipo del evento
    String user();           // Usuario que generó el evento
}
```

**Beneficios**:
- ✅ Type-safe: no hay strings arbitrarios.
- ✅ Exhaustive checking: compilador obliga manejar todos los casos.
- ✅ Immutable: records son inmutables por defecto.

#### `OpenEvent.java` (record)
```java
public record OpenEvent(
    String user,             // Usuario que abre el potrero
    String lotId,            // Lote/grupo de animales
    Integer animals          // Cantidad de animales
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.OPEN; }
}
```

**Usado en**: Transición EN_DESCANSO/DISPONIBLE → EN_USO.

#### `CloseEvent.java` (record)
```java
public record CloseEvent(
    String user,
    String lotId,
    Integer animals,
    Integer residualCm       // Altura residual después de pastoreo
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.CLOSE; }
}
```

**Usado en**: Transición EN_USO → EN_DESCANSO.

#### `MaintenanceSetEvent.java` (record)
```java
public record MaintenanceSetEvent(
    String user,
    PastureSubstatus substatus,  // FERTILIZANDO, REPARANDO, CUARENTENA
    String holdUntil             // Fecha de liberación (YYYY-MM-DD)
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.MAINTENANCE_SET; }
}
```

**Usado en**: Bloquear potrero con fecha de expiración.

#### `MaintenanceClearEvent.java` (record)
```java
public record MaintenanceClearEvent(
    String user
) implements PastureEvent {
    @Override
    public EventType type() { return EventType.MAINTENANCE_CLEAR; }
}
```

**Usado en**: Liberar bloqueo manualmente (alternativa a expiración automática).

#### `EventType.java` (enum)
```java
public enum EventType {
    OPEN,
    CLOSE,
    MAINTENANCE_SET,
    MAINTENANCE_CLEAR
}
```

---

## 📊 Patrones de Acceso (DynamoDB Query)

### 1. Listar Potreros de una Finca

```
Índice: GSI2 (gsi2-farm-blocked-eta)
Condition: gsi2pk = "FARM#F001"
Proyección: Todos los atributos
Resultado: Lista ordenada por ETA
```

### 2. Listar Potreros por Especie

```
Índice: GSI1 (gsi1-species-eta)
Condition: gsi1pk = "FARM#F001#SPECIES#Kikuyo"
Proyección: name, status, etaOpenDays, etc.
Resultado: Potreros de Kikuyo ordenados por ETA
```

### 3. Obtener un Potrero Específico

```
Tabla Principal: TABLE_PASTURE
Condition: pk = "PASTURE#P001"
Proyección: Todos
Resultado: Item completo
```

### 4. Filtrar Potreros Disponibles (EN CÓDIGO)

En **PaddockPage.jsx**, aplicado en memoria:
```javascript
const filtered = pastures.filter(p => 
  (species === "ALL" || p.species === species) &&
  (status === "ALL" || p.status === status) &&
  (query === "" || p.name.toLowerCase().includes(query))
);
```

---

## 🔄 Flujos de Caso de Uso

### Caso 1: Consultar Dashboard de Potreros

**Actor**: Usuario en frontend.

**Pasos**:
1. Usuario accede a `/potreros`.
2. `PaddockPage` monta y ejecuta `useEffect`.
3. Fetch a `GET /farms/F001/pastures`.
4. Backend: `PasturesController` → `RotationPlanProcessor.getRotationSemaphoreItems()`.
5. Para cada potrero:
   - Calcula ETA con `EtaCalculator`.
   - Aplica "tick" con `PastureStatusEngine.autoUpdateStatusTickByHoldUntil()`.
   - Mapea a DTO.
6. Retorna lista de `RotationSemaphoreItemDTO`.
7. Frontend: actualiza `pastures`, calcula KPIs, renderiza dashboard.
8. Usuario ve tabla, KPIs, semáforo, calendario, alertas.

### Caso 2: Filtrar Potreros

**Actor**: Usuario en frontend.

**Pasos**:
1. Usuario selecciona especie (ej. "Kikuyo") en select.
2. `species` state actualiza.
3. `useFilteredPastures` filtra en vivo.
4. Tabla se actualiza dinámicamente.

### Caso 3: Abrir un Potrero (OPEN Event)

**Actor**: Usuario en frontend.

**Pasos**:
1. Usuario hace clic en botón "Abrir" en fila de potrero.
2. Frontend prepara event: `{ eventType: "OPEN", pastureId: "P001", ... }`.
3. POST a `POST /farms/F001/pastures/{pastureId}/events` (endpoint no documentado, TO-DO).
4. Backend: `PastureStatusEngine.applyEvent(pasture, plan, event)`.
   - Verifica que no esté bloqueado.
   - Transición: status = "EN_USO".
   - Asigna `lastUseAt` = hoy.
5. `PastureRepository.update()` aplica cambios a DynamoDB.
6. Retorna potrero actualizado.
7. Frontend: actualiza estado local, tabla refleja cambio.

### Caso 4: Bloquear Potrero (MAINTENANCE_SET Event)

**Actor**: Usuario (o sistema automático).

**Pasos**:
1. Genera event: `{ eventType: "MAINTENANCE_SET", holdUntil: "2025-12-15", blockReason: "Fertilización" }`.
2. Backend aplica con `PastureStatusEngine.applyEvent()`.
3. status → "MANTENIMIENTO", substatus → "FERTILIZANDO", `holdUntil` → "2025-12-15".
4. En próximos GETs, motor verifica `isHoldUntilExpired()`.
5. Una vez expirado, automáticamente transiciona a EN_DESCANSO o DISPONIBLE según ETA.

---

## 🧪 Tests

### Unit Tests

#### `PastureStatusEngineTest.java`
**Ubicación**: `cattle-lambda-function/src/test/java/com/cattle/utils/PastureStatusEngineTest.java`

**Casos Cubiertos**:
- ✅ Transición EN_DESCANSO → EN_USO (OPEN).
- ✅ Transición EN_USO → EN_DESCANSO (CLOSE).
- ✅ Bloqueo efectivo (isBlockedEffective).
- ✅ Expiración de holdUntil.
- ✅ Cálculo de estado efectivo (deriveEffectiveStatus).

#### `PastureRepositoryTest.java`
**Ubicación**: `cattle-lambda-function/src/test/java/com/cattle/repository/PastureRepositoryTest.java`

**Casos Cubiertos**:
- ✅ Query por finca (GSI2).
- ✅ Query por especie (GSI1).
- ✅ PutItem y UpdateItem.

#### `EtaCalculatorTest.java` (TO-DO)
- [ ] Cálculo correcto con altura deficit.
- [ ] Cálculo con días desde uso = 0.
- [ ] ETA negativo (ya disponible).

### Integration Tests (TO-DO)
- [ ] Flujo completo: fetch potreros → aplicar motor → mapear DTOs.

---

## 📝 Configuración

### Variables de Entorno

| Variable | Valor (Dev) | Descripción |
|----------|-------------|-------------|
| `TABLE_PASTURE` | `cattle-pastures-dev` | Tabla de potreros |
| `TABLE_PLAN` | `cattle-plans-dev` | Tabla de planes de rotación |
| `AWS_REGION` | `us-east-1` | Región AWS |

### Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/farms/{farmId}/pastures` | Lista potreros y semáforo |
| POST | `/farms/{farmId}/pastures/{pastureId}/events` | Dispara evento (TO-DO) |
| PUT | `/farms/{farmId}/pastures/{pastureId}` | Actualiza potrero (TO-DO) |

---

## 🚀 Comandos útiles

### Listar potreros con CLI AWS

```bash
# Obtener potreros de finca F001
aws dynamodb query \
  --table-name cattle-pastures-dev \
  --index-name gsi2-farm-blocked-eta \
  --key-condition-expression "gsi2pk = :pk" \
  --expression-attribute-values '{":pk":{"S":"FARM#F001"}}' \
  --region us-east-1
```

### Crear plan de rotación

```bash
aws dynamodb put-item \
  --table-name cattle-plans-dev \
  --item '{
    "pk":{"S":"PLAN#FARM#F001#SPECIES#Kikuyo"},
    "farmId":{"S":"F001"},
    "species":{"S":"Kikuyo"},
    "restDays":{"N":"30"},
    "minHeightCm":{"N":"20"},
    "optimalHeightCm":{"N":"30"},
    "growthRateCmPerDay":{"N":"2.5"}
  }' \
  --region us-east-1
```

---

## 📚 Referencias Internas

- [Análisis de Modelado DynamoDB](../analysis-table-design.md)
- [Arquitectura General](../architecture/index.md)
- [Flujo de Registro (Ordeno)](../architecture/flujo-registro-ordeno.md)
- [Flujo Dashboard Potreros](../architecture/flujo-dashboard-potreros.md)
- [Diagramas: Estado y Actividad](../../documentation/)
