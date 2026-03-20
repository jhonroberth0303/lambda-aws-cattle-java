# 🐄 Bovines Overview: Visión Técnica

**Fecha**: 2026-01-09

## 🎯 Objetivo

Documentación técnica completa del módulo Bovines: arquitectura, flujos, datos, endpoints, y estado actual.

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

**Bovines** (Bovinos) es un módulo que gestiona la **información maestro de bovinos** en la finca.

**Propósito**:
- Registrar bovinos con información completa
- Mantener catálogo actualizado
- Rastrear genética y parentesco
- Seguimiento de estado reproductivo

**Datos**:
- ID bovino (auto-generado)
- Nombre y características físicas
- Raza, color, género
- Fecha de nacimiento y edad
- Estado reproductivo (lactating, pregnant, etc.)
- Información de parentaje
- Arete RFID (opcional)

---

## Arquitectura

### Vista General

```
┌─────────────────────────────────────────────────────────┐
│                   FRONTEND                              │
│                                                         │
│  BovineList (grid de bovinos)                           │
│   └─ BovineCard (tarjeta)                               │
│                                                         │
│  AddBovine (formulario)                                 │
│   └─ Crear/editar bovino                                │
│                                                         │
│  BovineDetail (vista lectura)                           │
│   └─ Información completa                               │
│                                                         │
│  Hooks:                                                 │
│   ├─ useBovineForm (form data)                          │
│   └─ useBovineDetail (fetch)                            │
│                                                         │
│  Services:                                              │
│   └─ bovinesServices.js                                 │
│                                                         │
└────────────────┬──────────────────────────────────────────┘
                 │ HTTP REST API
┌────────────────▼──────────────────────────────────────────┐
│                   BACKEND                                │
│                                                         │
│  Controllers:                                           │
│   └─ BovinesController.java                             │
│                                                         │
│  Processors:                                            │
│   └─ BovinesProcessor.java                              │
│                                                         │
│  Services:                                              │
│   └─ BovinesService.java                                │
│                                                         │
│  Repositories:                                          │
│   ├─ BovineRepository.java                              │
│   └─ CountersRepository.java                            │
│                                                         │
│  Entities:                                              │
│   └─ Bovine.java                                        │
│                                                         │
│  Mappers:                                               │
│   └─ BovinesMapperImpl.java                              │
│                                                         │
└────────────────┬──────────────────────────────────────────┘
                 │ AWS SDK
┌────────────────▼──────────────────────────────────────────┐
│              DynamoDB                                    │
│                                                         │
│  Table: TABLE_FARM_BOVINES                              │
│   PK: BOVINE#{bovineId}                                 │
│   SK: PROFILE                                           │
│   GSI1: PROFILE → BOVINE#{id}                           │
│                                                         │
│  Table: TABLE_COUNTERS (IDs)                            │
│   PK: {tableName}                                       │
│   nextId incrementado                                   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Modelado DynamoDB

### Tabla 1: TABLE_FARM_BOVINES

```
Nombre: TABLE_FARM_BOVINES

Partition Key (PK):
  BOVINE#{bovineId}
  Ejemplo: BOVINE#1, BOVINE#5, BOVINE#42

Sort Key (SK):
  PROFILE (todos los registros son PROFILE)

GSI1:
  GSI1-PK: PROFILE (para listar todos)
  GSI1-SK: BOVINE#{bovineId} (ordenado)
```

### Tabla 2: TABLE_COUNTERS

```
Nombre: TABLE_COUNTERS

Partition Key:
  Nombre de tabla → TABLE_FARM_BOVINES

Attribute:
  nextId: número incrementado
  
Uso: Generar IDs secuenciales para bovineIdentityItems
```

### Atributos de Bovine

| Atributo | Tipo | Descripción | Ejemplo |
|----------|------|-------------|---------|
| **pk** | String | Partition Key | BOVINE#5 |
| **sk** | String | Sort Key | PROFILE |
| **gsi1pk** | String | GSI1 PK | PROFILE |
| **gsi1sk** | String | GSI1 SK | BOVINE#5 |
| **bovineId** | Integer | ID único | 5 |
| **name** | String | Nombre | Estrella |
| **breed** | String | Raza | Holstein |
| **gender** | String | Género | female/male |
| **status** | String | Estado reproductivo | OPEN/PREGNANT/LACTATING |
| **bornDate** | String | Fecha nacimiento | 2023-05-10 |
| **age** | String | Edad calculada | 2 años 8 meses |
| **color** | String | Color | black, white, spotted |
| **tag** | String | Arete RFID | ABC123XYZ |
| **fatherNameSnapshot** | String | Padre (snapshot) | Bull01 |
| **motherNameSnapshot** | String | Madre (snapshot) | Daisy |
| **autoId** | Boolean | ID auto o manual | true |
| **enabled** | Boolean | Activo/Inactivo | true |
| **createdAt** | String | Timestamp creación | 2025-12-10T08:30:00Z |
| **updatedAt** | String | Timestamp actualización | 2025-12-10T09:00:00Z |

### Ejemplo de Ítem

```json
{
  "pk": "BOVINE#5",
  "sk": "PROFILE",
  "gsi1pk": "PROFILE",
  "gsi1sk": "BOVINE#5",
  "bovineId": 5,
  "name": "Estrella",
  "breed": "Holstein",
  "gender": "female",
  "status": "LACTATING",
  "bornDate": "2023-05-10",
  "age": "2 años 8 meses",
  "color": "black and white",
  "tag": "ABC123XYZ",
  "fatherNameSnapshot": "Bull01",
  "motherNameSnapshot": "Daisy",
  "autoId": true,
  "enabled": true,
  "createdAt": "2025-12-10T08:30:00Z",
  "updatedAt": "2025-12-10T09:15:00Z"
}
```

---

## Flujos de Negocio

### Flujo 1: Listar Todos los Bovinos

```
INICIO
  ↓
[Usuario entra a página Bovines]
  ↓
[BovineList.jsx hace fetch]
  ↓
[GET /bovineIdentityItems]
  ↓
[Backend]:
  - Query GSI1
  - GSI1-PK = "PROFILE"
  - Retorna todos los bovineIdentityItems
  ↓
[Retorna List<BovineDTO>]
  ↓
[Frontend agrupa en grid]
  ↓
[Renderiza BovineCard × N]
  ↓
FIN
```

### Flujo 2: Crear Nuevo Bovino

```
INICIO
  ↓
[Usuario hace click "Nuevo Bovino"]
  ↓
[Abre formulario AddBovine]
  ↓
[Llena campos]:
  - Nombre *
  - Género *
  - Fecha nacimiento *
  - Raza, color, status
  - Parentaje (opcional)
  - Tag RFID (scan o manual)
  ↓
[Click "Guardar"]
  ↓
[Validación frontend]
  ├─ name ≠ empty
  ├─ gender ≠ empty
  └─ bornDate ≠ empty
  ↓
[POST /bovineIdentityItems con payload]
  ↓
[Backend]:
  1. Obtener siguiente ID de CountersRepository
  2. Generar PK = "BOVINE#{id}"
  3. SK = "PROFILE"
  4. Generar GSI keys
  5. Crear entity
  6. setCreatedAt = ahora
  7. setEnabled = true
  8. Guardar en DynamoDB
  ↓
[Response 200 + BovineDTO creado]
  ↓
[Frontend refetch lista]
  ↓
[Mostrar confirmación]
  ↓
FIN
```

### Flujo 3: Editar Bovino

```
INICIO
  ↓
[Usuario click "Editar" en tarjeta]
  ↓
[Navega a /edit/{bovineId}]
  ↓
[AddBovine mount con initialBovine]
  ↓
[useBovineForm carga datos iniciales]
  ↓
[GET /bovineIdentityItems/{id}]
  ↓
[Formulario precargado]
  ↓
[Usuario modifica campos]
  ↓
[Click "Actualizar"]
  ↓
[PUT /bovineIdentityItems/{id} con payload]
  ↓
[Backend]:
  1. Validar que existe (GET)
  2. Actualizar atributos
  3. setUpdatedAt = ahora
  4. Guardar en DynamoDB
  ↓
[Response 200 + BovineDTO actualizado]
  ↓
[Frontend refetch]
  ↓
[Mostrar confirmación]
  ↓
FIN
```

---

## Backend

### Componentes

#### BovinesController.java
**Responsabilidad**: Exponer endpoints REST

```
GET /bovineIdentityItems
  → getAll()
  → Retorna List<BovineDTO>

GET /bovineIdentityItems/{id}
  → findById(id)
  → Retorna BovineDTO

POST /bovineIdentityItems
  → save(BovineDTO)
  → Retorna BovineDTO (creado)

PUT /bovineIdentityItems/{id}
  → update(id, BovineDTO)
  → Retorna BovineDTO (actualizado)
```

#### BovinesProcessor.java
**Responsabilidad**: Lógica de negocio, mapeo, validaciones

```
findAll()
  - Delegar a Service
  - Mapear a DTOs
  - Retornar lista

findById(id)
  - Validar id > 0
  - Delegar a Service
  - Mapear a DTO

save(BovineDTO)
  - Mapear a Entity
  - Delegar a Service
  - Retornar DTO

update(BovineDTO)
  - Validar ID existe
  - Mapear a Entity
  - Delegar a Service
  - Retornar DTO
```

#### BovinesService.java
**Responsabilidad**: Orquestar acceso a datos

```
findAll()
  - Delegar a Repository
  - Manejar excepciones

findById(id)
  - Delegar a Repository
  - Retornar Optional<Bovine>

save(Bovine)
  - Obtener siguiente ID (Counter)
  - Generar PK/SK
  - Generar GSI keys
  - setCreatedAt, setEnabled
  - Delegar a Repository

update(Bovine)
  - Validar que existe
  - Generar PK/SK
  - setUpdatedAt
  - Delegar a Repository
```

#### BovineRepository.java
**Responsabilidad**: Acceso a DynamoDB

```
findAll()
  - Query GSI1-PK = "PROFILE"
  - Retornar List<Bovine>

findById(id)
  - Get PK = "BOVINE#{id}", SK = "PROFILE"
  - Retornar Optional<Bovine>

save(Bovine)
  - PutItem en tabla
  - Retornar Optional<Bovine>

update(Bovine)
  - UpdateItem en tabla
  - Retornar Optional<Bovine>
```

#### CountersRepository.java
**Responsabilidad**: Generar IDs secuenciales

```
getNextId(tableName)
  - Query TABLE_COUNTERS
  - Incrementar nextId
  - UpdateItem
  - Retornar String (nuevo ID)
```

---

## Frontend

### Componentes

#### BovineList.jsx
**Responsabilidad**: Listar y renderizar grid

```
- Estado: bovineIdentityItems, loading
- useEffect: fetch GET /bovineIdentityItems
- Mapear a BovineCard
- Grid responsive
```

#### BovineCard.jsx
**Responsabilidad**: Tarjeta individual

```
- Props: bovineIdentityItem (object)
- Mostrar: nombre, ID, raza, género, edad
- Botones: Ver detalle, Editar
- Navegar a /detail/{id} o /edit/{id}
```

#### AddBovine.jsx
**Responsabilidad**: Formulario crear/editar

```
- Props: onBovineAdded (callback), initialBovine (optional)
- Hook: useBovineForm
- Modo crear: campos vacíos
- Modo editar: campos precargados
- Campos:
  * name (required)
  * gender (female/male)
  * status (dropdown por género)
  * bornDate (date picker, calcula edad)
  * breed, color
  * fatherNameSnapshot, motherNameSnapshot
  * tag (RFID)
  * enabled (checkbox)
- Acciones:
  * Escanear arete RFID
  * Limpiar formulario
- Submit: POST o PUT según modo
```

#### BovineDetail.jsx
**Responsabilidad**: Vista detalle (lectura)

```
- Props: bovineId (from URL params)
- Hook: useBovineDetail(id)
- Mostrar información completa
- Loading state
- Error handling
- Botón volver / editar
```

### Hooks

#### useBovineForm
```typescript
const {
  formData,
  setFormData,
  scanning,
  saving,
  ageLabel,
  handleChange,
  handleScanTag,
  handleSubmit,
  maleStatuses,
  femaleStatuses,
  breeds,
  genders,
  isEditMode,
  update
} = useBovineForm({ onBovineAdded, endpoint, initialBovine });
```

- Gestiona: estado formulario, loading, scanning
- calcAge: calcula edad automáticamente
- handleScanTag: simula escaneo RFID
- handleSubmit: valida y llama POST/PUT
- Constantes: estados por género, razas, géneros

#### useBovineDetail
```typescript
const { bovineIdentityItem, loading, error } = useBovineDetail(id);
```

- Fetch GET /bovineIdentityItems/{id}
- Estados: loading, error, bovineIdentityItem
- Mounted check para cleanup

### Servicios

#### bovinesServices.js
```javascript
getBovinesEndpoint()
  - GET /bovineIdentityItems
  - Retorna: Array<Bovine>

getBovineById(id)
  - GET /bovineIdentityItems/{id}
  - Retorna: Bovine

createBovine(payload)
  - POST /bovineIdentityItems
  - Retorna: Bovine creado

updateBovine(id, payload)
  - PUT /bovineIdentityItems/{id}
  - Retorna: Bovine actualizado
```

---

## Endpoints REST

### GET /bovineIdentityItems - Listar Todos

**Response 200**:
```json
[
  {
    "bovineId": 1,
    "name": "Bossy",
    "breed": "Holstein",
    "gender": "female",
    "status": "LACTATING",
    "bornDate": "2023-05-10",
    "age": "2 años 8 meses",
    "color": "black and white",
    "enabled": true
  },
  ...
]
```

### GET /bovineIdentityItems/{id} - Detalle

**Response 200**:
```json
{
  "bovineId": 1,
  "name": "Bossy",
  "breed": "Holstein",
  "gender": "female",
  "status": "LACTATING",
  "bornDate": "2023-05-10",
  "age": "2 años 8 meses",
  "color": "black and white",
  "tag": "ABC123XYZ",
  "fatherNameSnapshot": "Bull01",
  "motherNameSnapshot": "Daisy",
  "autoId": true,
  "enabled": true,
  "createdAt": "2025-12-10T08:30:00Z",
  "updatedAt": "2025-12-10T09:00:00Z"
}
```

### POST /bovineIdentityItems - Crear

**Request**:
```json
{
  "name": "Estrella",
  "gender": "female",
  "status": "OPEN",
  "bornDate": "2023-05-10",
  "breed": "Holstein",
  "color": "black and white",
  "tag": "XYZ789",
  "fatherNameSnapshot": "Bull01",
  "motherNameSnapshot": "Daisy",
  "enabled": true
}
```

**Response 200**:
```json
{
  "bovineId": 42,
  "name": "Estrella",
  "gender": "female",
  "status": "OPEN",
  "bornDate": "2023-05-10",
  "age": "2 años 8 meses",
  "breed": "Holstein",
  "color": "black and white",
  "tag": "XYZ789",
  "createdAt": "2025-12-10T08:30:00Z"
}
```

**Validaciones Backend**:
- ✅ name required
- ✅ gender in [female, male]
- ✅ bornDate required, ≤ hoy
- ✅ status válido para género
- ⚠️ Validar raza en enum (falta)

### PUT /bovineIdentityItems/{id} - Actualizar

**Request**:
```json
{
  "bovineId": 5,
  "name": "Estrella",
  "gender": "female",
  "status": "PREGNANT",
  "color": "black and white",
  "enabled": true
}
```

**Response 200**: Bovine actualizado

---

## Testing

### Tests Unitarios

```java
@Test
public void testFindAllCallsRepository() {
  // Arrange
  List<Bovine> bovineIdentityItems = List.of(new Bovine());
  when(bovineRepository.findAll()).thenReturn(Optional.of(bovineIdentityItems));
  
  // Act
  List<BovineDTO> result = processor.findAll();
  
  // Assert
  assertNotNull(result);
  assertEquals(1, result.size());
}

@Test
public void testSaveGeneratesNewId() {
  // Arrange
  BovineDTO dto = new BovineDTO();
  dto.setName("Bossy");
  
  // Act
  Optional<BovineDTO> result = processor.save(dto);
  
  // Assert
  assertTrue(result.isPresent());
  assertNotNull(result.get().getBovineId());
}
```

### Tests de Integración

```bash
# Listar
curl http://localhost:8080/bovineIdentityItems

# Crear
curl -X POST http://localhost:8080/bovineIdentityItems \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Estrella",
    "gender": "female",
    "bornDate": "2023-05-10"
  }'

# Obtener detalle
curl http://localhost:8080/bovineIdentityItems/5

# Actualizar
curl -X PUT http://localhost:8080/bovineIdentityItems/5 \
  -H "Content-Type: application/json" \
  -d '{
    "bovineId": 5,
    "name": "Estrella (actualizado)",
    "status": "PREGNANT"
  }'
```

---

## Estado Actual

### ✅ Implementado

- [x] Tabla DynamoDB con claves correctas
- [x] Controller REST (GET, POST, PUT)
- [x] Processor con lógica básica
- [x] Service + Repository
- [x] Entity Bovine
- [x] Mapper BovinesMapperImpl
- [x] CountersRepository para IDs
- [x] Frontend BovineList (grid)
- [x] Componente BovineCard
- [x] Formulario AddBovine (crear/editar)
- [x] BovineDetail (lectura)
- [x] Hook useBovineForm
- [x] Hook useBovineDetail
- [x] Servicios JavaScript
- [x] Escaneo RFID simulado
- [x] Cálculo automático de edad

### ⚠️ Parcialmente Implementado

- [ ] Validaciones completas (enum de status, razas)
- [ ] Error handling mejorado
- [ ] Confirmaciones antes de acciones destructivas
- [ ] Loading states en UI

### ❌ No Implementado

- [ ] Búsqueda por nombre/ID
- [ ] Filtrado por género/estado
- [ ] Árbol genealógico (parentesco)
- [ ] Historial de cambios
- [ ] Reportes
- [ ] Exportar a CSV
- [ ] Editar padre/madre (solo snapshot)

---

## 📝 Convenciones

### Claves DynamoDB
```
PK: BOVINE#{bovineId}
SK: PROFILE
```

### Status por Género

**Hembras (female)**:
```
OPEN        - disponible para reproducción
PREGNANT    - embarazada
DRY         - no lactante (post-parto)
LACTATING   - produciendo leche
```

**Machos (male)**:
```
BULL    - reproductor
STEER   - castrado
CALF    - ternero
```

### Fechas
```
YYYY-MM-DD (ISO)
2023-05-10
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
