# 🥛 Milking Overview: Visión Técnica

**Fecha**: 2026-01-09

## 🎯 Objetivo

Documentación técnica completa del módulo Milking: arquitectura, flujos, datos, endpoints, y estado actual.

---

## 📚 Tabla de Contenidos

1. [Descripción](#descripción)
2. [Arquitectura](#arquitectura)
3. [Modelado DynamoDB](#modelado-dynamodb)
4. [Flujos de Negocio](#flujos-de-negocio)
5. [Backend: Arquitectura y Componentes](#backend)
6. [Frontend: Arquitectura y Componentes](#frontend)
7. [Endpoints REST](#endpoints-rest)
8. [Testing](#testing)
9. [Estado Actual](#estado-actual)

---

## Descripción

**Milking** (Lactancia) es un módulo que registra la **producción lechera diaria** de bovinos.

**Propósito**:
- Registrar litros de leche en turnos (AM/PM)
- Rastrear desempeño de cada bovino
- Generar historial para análisis

**Datos**:
- Bovino ID
- Fecha y turno (AM/PM)
- Litros producidos
- Estado (completo/parcial/omitido)
- Observaciones

---

## Arquitectura

### Vista General

```
┌─────────────────────────────────────────────────────────┐
│                   FRONTEND                              │
│                                                         │
│  MilkingPage (orquestador)                              │
│   ├─ MilkingAdd (formulario)                            │
│   └─ MilkingTable (tabla)                               │
│                                                         │
│  Hooks:                                                 │
│   ├─ useMilkingRecords                                  │
│   └─ useMilkingForm                                     │
│                                                         │
│  Services:                                              │
│   └─ milkingService.js                                  │
│                                                         │
└────────────────┬──────────────────────────────────────────┘
                 │ HTTP REST API
┌────────────────▼──────────────────────────────────────────┐
│                   BACKEND                                │
│                                                         │
│  Controllers:                                           │
│   └─ MilkingController.java                             │
│                                                         │
│  Processors:                                            │
│   └─ MilkingProcessor.java                              │
│                                                         │
│  Services:                                              │
│   └─ MilkingService.java                                │
│                                                         │
│  Repositories:                                          │
│   └─ MilkingRepository.java                             │
│                                                         │
│  Entities:                                              │
│   └─ FarmMilking.java                                   │
│                                                         │
│  Mappers:                                               │
│   └─ MilkingMapperImpl.java                              │
│                                                         │
└────────────────┬──────────────────────────────────────────┘
                 │ AWS SDK
┌────────────────▼──────────────────────────────────────────┐
│              DynamoDB                                    │
│                                                         │
│  Table: TABLE_FARM_MILKING                              │
│   PK: BOVINE#{bovineId}                                 │
│   SK: MILKING#{date}#{shift}                            │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Modelado DynamoDB

### Tabla: TABLE_FARM_MILKING

```
Nombre: TABLE_FARM_MILKING

Partition Key (PK):
  BOVINE#{bovineId}
  Ejemplo: BOVINE#5, BOVINE#10, BOVINE#42

Sort Key (SK):
  MILKING#{date}#{shift}
  Ejemplo: MILKING#2025-12-10#AM, MILKING#2025-12-10#PM
```

### Atributos

| Atributo | Tipo | Descripción | Ejemplo |
|----------|------|-------------|---------|
| **PK** | String | Partition Key | BOVINE#5 |
| **SK** | String | Sort Key | MILKING#2025-12-10#AM |
| **bovineId** | Integer | ID del bovino | 5 |
| **date** | String | Fecha (YYYY-MM-DD) | 2025-12-10 |
| **shift** | String | Turno (AM o PM) | AM |
| **liters** | Double | Litros producidos | 15.5 |
| **status** | String | Estado del registro | completo/parcial/omitido |
| **observations** | String | Notas (opcional) | Normal, problemas respiratorios |
| **recordedBy** | String | Usuario que registró | jhonroberth |
| **createdAt** | String | Timestamp creación | 2025-12-10T08:30:00Z |
| **updatedAt** | String | Timestamp actualización | 2025-12-10T09:00:00Z |

### Ejemplo de Ítem

```json
{
  "PK": "BOVINE#5",
  "SK": "MILKING#2025-12-10#AM",
  "bovineId": 5,
  "date": "2025-12-10",
  "shift": "AM",
  "liters": 15.5,
  "status": "completo",
  "observations": "Normal, buena producción",
  "recordedBy": "jhonroberth",
  "createdAt": "2025-12-10T08:30:00Z",
  "updatedAt": "2025-12-10T08:30:00Z"
}
```

---

## Flujos de Negocio

### Flujo 1: Registrar Lectura de Leche

```
INICIO
  ↓
[Usuario entra a módulo Milking]
  ↓
[Abre formulario "Nuevo registro"]
  ↓
[Selecciona bovino] → Dropdown con bovinos disponibles
  ↓
[Ingresa datos]:
  - Fecha (date picker)
  - Turno (select AM/PM)
  - Litros (number input)
  - Estado (select)
  - Observaciones (textarea)
  ↓
[Click "Guardar"]
  ↓
[Validar datos en frontend]
  ├─ bovineId ≠ empty
  ├─ date ≠ empty
  └─ shift ≠ empty
  ↓
[POST /milkingRecord con payload]
  ↓
[Backend recibe request]
  ↓
[MilkingController → MilkingProcessor]
  ↓
[Validar en backend]
  ├─ bovineId > 0
  ├─ date formato YYYY-MM-DD
  └─ shift = AM o PM
  ↓
[Generar claves]:
  - PK = "BOVINE#{bovineId}"
  - SK = "MILKING#{date}#{shift}"
  ↓
[Crear FarmMilking entity]
  ↓
[Guardar en DynamoDB]
  ↓
[Response 200 OK + MilkingDTO]
  ↓
[Frontend refetch datos]
  ↓
[Tabla actualizada]
  ↓
[Mostrar confirmación]
  ↓
FIN
```

### Flujo 2: Ver Histórico de Bovino

```
INICIO
  ↓
[Usuario selecciona bovino]
  ↓
[Click "Buscar"]
  ↓
[GET /milkingRecord/{bovineId}]
  ↓
[Backend]:
  - PK = "BOVINE#{bovineId}"
  - Query tabla
  ↓
[Retorna lista de FarmMilking]
  ↓
[Frontend agrupa por fecha]
  ↓
[Renderiza tabla]:
  
  Fecha | Mañana | Obs AM | Tarde | Obs PM | Total
  ──────────────────────────────────────────────
  2025-12-10 | 15.5 L | Normal | 14.2 L | Normal | 29.7 L
  2025-12-11 | 16.0 L | Normal | —      | —      | 16.0 L
  ↓
FIN
```

### Flujo 3: Filtrar por Turno

```
[Usuario selecciona turno AM o PM]
  ↓
[GET /milkingRecord/{bovineId}?shift=AM]
  ↓
[Backend filtra por shift]
  ↓
[Retorna solo registros del turno]
  ↓
[Frontend renderiza tabla filtrada]
  ↓
FIN
```

---

## Backend

### Componentes

#### MilkingController.java
**Responsabilidad**: Exponer endpoints REST

```
POST /milkingRecord
  → createMilking(MilkingDTO)
  → Retorna MilkingDTO (creado)

GET /milkingRecord/{idBovine}
  → milkingData(idBovine, shift?)
  → Retorna List<MilkingDTO>
```

#### MilkingProcessor.java
**Responsabilidad**: Lógica de negocio, validaciones, mapeo

```
getMilkingData(idBovine, shift)
  - Query por PK
  - Filtrar por shift (si aplica)
  - Mapear a DTOs
  - Retornar

createMilking(MilkingDTO)
  - Validar campos
  - Generar PK y SK
  - Mapear a Entity
  - Delegar a Service
  - Retornar DTO
```

#### MilkingService.java
**Responsabilidad**: Orquestar acceso a datos

```
save(FarmMilking)
  - Delegar a Repository
  - Manejar excepciones
  - Log

getMilkingByPk(pk)
  - Delegar a Repository
  - Manejar excepciones
  - Retornar Optional<List<FarmMilking>>
```

#### MilkingRepository.java
**Responsabilidad**: Acceso a DynamoDB

```
save(FarmMilking)
  - PutItem en tabla

getMilkingByPk(pk)
  - Query por PK
  - Retornar items
```

#### FarmMilking.java (Entity)
**Responsabilidad**: Modelo de datos

```java
@DynamoDbBean
public class FarmMilking {
    @DynamoDbPartitionKey
    private String PK;              // BOVINE#{id}
    
    @DynamoDbSortKey
    private String SK;              // MILKING#{date}#{shift}
    
    // Atributos
    private Integer bovineId;
    private String date;
    private String shift;
    private Double liters;
    private String status;
    private String observations;
    private String recordedBy;
    private String createdAt;
    private String updatedAt;
}
```

---

## Frontend

### Componentes

#### MilkingPage.jsx
**Responsabilidad**: Orquestador principal

```
- Estado: bovineId, records, loading
- Hooks: useMilkingRecords, useMilkingForm
- Renderiza: MilkingAdd + MilkingTable
```

#### MilkingAdd.jsx
**Responsabilidad**: Formulario para agregar registros

```
- Props:
  * form: objeto con datos del formulario
  * onChange: handler para cambios de input
  * onSubmit: handler al guardar
  
- Campos:
  * bovineId (select)
  * date (input date)
  * shift (select AM/PM)
  * liters (input number)
  * status (select)
  * recordedBy (input text)
  * observations (textarea)

- Comportamiento:
  * Accordion (expandible/contraible)
  * Validación básica
  * Submit POST a /milkingRecord
```

#### MilkingTable.jsx
**Responsabilidad**: Mostrar registros agrupados

```
- Props:
  * records: array de FarmMilking
  * bovineId: ID del bovino filtrado
  * setBovineId: setter
  * onQuery: callback de búsqueda
  * loading: estado de carga

- Comportamiento:
  * Agrupa por fecha
  * Columnas: Fecha, AM, Obs AM, PM, Obs PM, Total
  * Calcula total diario
  * Muestra "—" si no hay registro
```

#### BovineSelect.jsx
**Responsabilidad**: Dropdown reutilizable de bovinos

```
- Props:
  * value: bovineId seleccionado
  * onChange: callback al cambiar

- Comportamiento:
  * Renderiza lista de bovinos
  * (Asume que datos vienen de algún mock o API)
```

### Hooks Personalizados

#### useMilkingRecords
```javascript
const {
  bovineId,
  setBovineId,
  records,
  loading,
  fetchData,
  onQuery
} = useMilkingRecords(bovineIdFromProp);
```

- Gestiona: bovineId, records, loading
- fetchData(id): obtiene registros de un bovino
- onQuery(): valida e invoca fetchData

#### useMilkingForm
```javascript
const {
  form,
  setForm,
  onChange,
  onSubmit
} = useMilkingForm(bovineId, fetchData);
```

- Gestiona: form (object)
- onChange(e): actualiza campo del formulario
- onSubmit(e): POST a /milkingRecord, refetch

### Servicios

#### milkingService.js
```javascript
getMilkingByBovineId(bovineId)
  - GET /milkingRecord/{bovineId}
  - Retorna: Array<FarmMilking>

addMilkingRecord(payload)
  - POST /milkingRecord
  - Payload: { bovineId, date, shift, liters, status, ... }
  - Retorna: FarmMilking creado
```

---

## Endpoints REST

### POST /milkingRecord - Crear Registro

**Request**:
```json
{
  "bovineId": 5,
  "date": "2025-12-10",
  "shift": "AM",
  "liters": 15.5,
  "status": "completo",
  "observations": "Normal",
  "recordedBy": "jhonroberth"
}
```

**Response 200**:
```json
{
  "bovineId": 5,
  "date": "2025-12-10",
  "shift": "AM",
  "liters": 15.5,
  "status": "completo",
  "observations": "Normal",
  "recordedBy": "jhonroberth",
  "createdAt": "2025-12-10T08:30:00Z"
}
```

**Validaciones Backend**:
- ✅ bovineId > 0
- ✅ date formato YYYY-MM-DD
- ✅ shift = "AM" o "PM"
- ❌ liters > 0 (falta)
- ❌ status en lista permitida (falta)

### GET /milkingRecord/{idBovine} - Listar Registros

**Request**:
```
GET /milkingRecord/5
GET /milkingRecord/5?shift=AM
```

**Response 200**:
```json
[
  {
    "bovineId": 5,
    "date": "2025-12-10",
    "shift": "AM",
    "liters": 15.5,
    "status": "completo",
    "observations": "Normal",
    "recordedBy": "jhonroberth",
    "createdAt": "2025-12-10T08:30:00Z"
  },
  {
    "bovineId": 5,
    "date": "2025-12-10",
    "shift": "PM",
    "liters": 14.2,
    "status": "completo",
    "observations": "Buena",
    "recordedBy": "jhonroberth",
    "createdAt": "2025-12-10T18:45:00Z"
  }
]
```

**Parámetros Query**:
- `shift` (opcional): "AM" o "PM" para filtrar

---

## Testing

### Tests Unitarios Pendientes

1. **MilkingProcessor**:
   - `testCreateMilkingValidatesBovineId()`
   - `testCreateMilkingValidatesDate()`
   - `testCreateMilkingValidatesShift()`
   - `testGetMilkingDataFiltersbyShift()`

2. **MilkingService**:
   - `testSaveSuccessful()`
   - `testSaveFails()`
   - `testGetMilkingByPkEmpty()`

3. **MilkingRepository**:
   - `testSaveCallsDynamoDB()`
   - `testQueryByPk()`

### Tests de Integración

```bash
# Crear registro
curl -X POST http://localhost:8080/milkingRecord \
  -H "Content-Type: application/json" \
  -d '{
    "bovineId": 5,
    "date": "2025-12-10",
    "shift": "AM",
    "liters": 15.5,
    "status": "completo",
    "recordedBy": "jhonroberth"
  }'

# Obtener registros
curl http://localhost:8080/milkingRecord/5

# Obtener solo mañana
curl http://localhost:8080/milkingRecord/5?shift=AM
```

---

## Estado Actual

### ✅ Implementado

- [x] Tabla DynamoDB con claves correctas
- [x] Controller REST (POST, GET)
- [x] Processor con validaciones básicas
- [x] Service + Repository
- [x] Entity FarmMilking
- [x] Mapper MilkingMapperImpl
- [x] Frontend MilkingPage
- [x] Formulario MilkingAdd (accordion)
- [x] Tabla MilkingTable (agrupada por fecha)
- [x] Hooks para estado y formulario
- [x] Servicios JavaScript

### ⚠️ Parcialmente Implementado

- [ ] Validaciones mejoradas (liters > 0, status enum)
- [ ] Error handling en UI
- [ ] Loading states
- [ ] Manejo de excepciones

### ❌ No Implementado

- [ ] Endpoint PUT para editar
- [ ] Endpoint DELETE para eliminar
- [ ] Búsqueda avanzada
- [ ] Exportar CSV
- [ ] Gráficos de tendencia
- [ ] Alertas automáticas
- [ ] Reportes diarios
- [ ] Tests unitarios

---

## 📝 Convenciones

### Claves DynamoDB
```
PK: {entity-type}#{id}
SK: {record-type}#{date}#{shift}
```

### Fechas
```
YYYY-MM-DD (ISO)
2025-12-10
```

### Turnos
```
"AM"  (Mañana: típicamente 5-12)
"PM"  (Tarde: típicamente 12-20)
```

### Status
```
"completo"   - Se completó el ordeno
"parcial"    - Se completó parcialmente
"omitido"    - Se omitió el ordeno
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
