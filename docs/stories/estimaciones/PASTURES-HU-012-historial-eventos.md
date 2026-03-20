# 🌱 PASTURES-HU#12: Backend: GET Historial de Eventos

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟡 MEDIO (P2) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Paginated Query API + Event History Service | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend developer**, quiero crear endpoints REST para consultar el historial de eventos de potreros, de tal forma que:

1. Se pueda obtener todos los eventos de un potrero (ordenados por fecha)
2. Se pueda filtrar por tipo de evento (OPEN, CLOSE, MAINTENANCE_SET, etc.)
3. Se pueda filtrar por rango de fechas
4. Se pueda paginar los resultados
5. Se retorne información completa (quién, cuándo, qué cambió)
6. Se incluya antes/después del cambio (para auditoría)
7. El endpoint sea rápido y escalable

Esto habilitará que operarios y gerentes vean el historial completo de rotación, detecten patrones, y auditen cambios.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Endpoint GET Historial Básico
```gherkin
Scenario: Obtener todos los eventos de un potrero
  Given finca "F001" con potrero "P001"
  And potrero tiene 5 eventos en historial
  When GET /farms/F001/pastures/P001/events
  Then:
    [ ] Status: 200 OK
    [ ] Retorna array de eventos
    [ ] Cada evento tiene: id, type, timestamp, userId, details
    [ ] Ordenado por fecha (más reciente primero)
    [ ] Sin paginación: retorna máximo 100 eventos
```

### AC#2: Estructura de Evento Retornado
```gherkin
Scenario: Evento tiene estructura completa
  Given evento de tipo OPEN en 2026-01-05
  When se consulta historial
  Then se retorna:
    {
      "id": "EVT-001",
      "eventType": "OPEN",
      "timestamp": "2026-01-05T10:30:00Z",
      "pastureId": "P001",
      "userId": "USER-123",
      "userName": "Carlos López",
      "details": {
        "height": 45,
        "notes": "Abierto para animales"
      },
      "beforeState": {
        "status": "DISPONIBLE",
        "currentHeight": 0
      },
      "afterState": {
        "status": "EN_USO",
        "currentHeight": 45
      }
    }
```

### AC#3: Filtrar por Tipo de Evento
```gherkin
Scenario: Filtrar eventos por tipo
  Given potrero con eventos: OPEN, CLOSE, MAINTENANCE_SET, CLOSE
  When GET /farms/F001/pastures/P001/events?type=CLOSE
  Then:
    [ ] Retorna solo los 2 eventos CLOSE
    [ ] Otros eventos filtrados
    [ ] Status: 200 OK
  
  When GET /farms/F001/pastures/P001/events?type=OPEN,CLOSE
  Then:
    [ ] Retorna OPEN y CLOSE (múltiples tipos)
```

### AC#4: Filtrar por Rango de Fechas
```gherkin
Scenario: Filtrar eventos por rango de fechas
  When GET /farms/F001/pastures/P001/events?startDate=2026-01-01&endDate=2026-01-31
  Then:
    [ ] Solo eventos entre 2026-01-01 y 2026-01-31
    [ ] Eventos fuera del rango excluidos
    [ ] Status: 200 OK
  
  When GET /farms/F001/pastures/P001/events?startDate=2026-01-01
  Then:
    [ ] Eventos desde 2026-01-01 hasta hoy
```

### AC#5: Paginación
```gherkin
Scenario: Paginar resultados
  Given potrero con 250 eventos
  When GET /farms/F001/pastures/P001/events?page=0&size=50
  Then:
    [ ] Retorna primeros 50 eventos
    [ ] Incluye metadata: totalCount=250, page=0, totalPages=5
    [ ] Response:
        {
          "content": [...],
          "totalElements": 250,
          "totalPages": 5,
          "currentPage": 0,
          "size": 50
        }
  
  When GET /farms/F001/pastures/P001/events?page=1&size=50
  Then:
    [ ] Retorna eventos 50-99
    [ ] Metadata actualizado: page=1
```

### AC#6: Ordenamiento
```gherkin
Scenario: Ordenar eventos por campo
  When GET /farms/F001/pastures/P001/events?sort=timestamp,DESC
  Then:
    [ ] Ordenado por timestamp descendente (más reciente primero)
  
  When GET /farms/F001/pastures/P001/events?sort=timestamp,ASC
  Then:
    [ ] Ordenado ascendente (más antiguo primero)
  
  When GET /farms/F001/pastures/P001/events?sort=eventType,ASC
  Then:
    [ ] Ordenado alfabético por tipo
```

### AC#7: Búsqueda por Usuario
```gherkin
Scenario: Filtrar eventos por usuario que los realizó
  When GET /farms/F001/pastures/P001/events?userId=USER-123
  Then:
    [ ] Solo eventos creados por USER-123
    [ ] Otros usuarios filtrados
  
  When GET /farms/F001/pastures/P001/events?userName=Carlos
  Then:
    [ ] Eventos donde userName contiene "Carlos"
```

### AC#8: Estadísticas Básicas (Bonus)
```gherkin
Scenario: Retornar estadísticas junto con eventos
  When GET /farms/F001/pastures/P001/events?includeStats=true
  Then:
    [ ] Incluye en response:
        {
          "content": [...],
          "stats": {
            "totalEvents": 45,
            "eventCounts": {
              "OPEN": 15,
              "CLOSE": 14,
              "MAINTENANCE_SET": 8,
              "MAINTENANCE_CLEAR": 8
            },
            "lastEventAt": "2026-01-05T10:30:00Z",
            "firstEventAt": "2025-01-10T08:00:00Z"
          }
        }
```

### AC#9: Historial de Finca (Todos los Potreros)
```gherkin
Scenario: Obtener historial de todos los potreros de una finca
  Given finca con 10 potreros
  When GET /farms/F001/events
  Then:
    [ ] Retorna eventos de TODOS los potreros
    [ ] Incluye pastureId en cada evento
    [ ] Ordenado por timestamp global
    [ ] Útil para auditoría de finca completa
```

### AC#10: Campos Sensibles No Incluidos
```gherkin
Scenario: Excluir campos sensibles del historial
  When se retorna evento
  Then:
    [ ] No incluye: contraseñas, tokens, datos médicos sensibles
    [ ] Sí incluye: userId, userName (para auditoría)
    [ ] Cumple con privacidad
```

### AC#11: Performance - Queries Optimizadas
```gherkin
Scenario: Historial retorna rápido
  Given potrero con 1000+ eventos
  When GET /farms/F001/pastures/P001/events?page=0&size=50
  Then:
    [ ] Respuesta en < 500ms
    [ ] Usa índices en BD (timestamp, eventType, userId)
    [ ] Sin N+1 queries
    [ ] Paginación eficiente
```

### AC#12: Validación de Acceso
```gherkin
Scenario: Solo usuarios autorizados ven historial
  Given usuario sin permisos en finca F001
  When GET /farms/F001/pastures/P001/events
  Then:
    [ ] Status: 403 Forbidden
    [ ] Error: "No tienes permiso para ver este historial"
  
  Given usuario con permisos
  When GET /farms/F001/pastures/P001/events
  Then:
    [ ] Status: 200 OK
    [ ] Retorna historial
```

### AC#13: Casos Edge - Potrero sin Historial
```gherkin
Scenario: Potrero sin eventos
  Given potrero P999 recién creado, sin eventos
  When GET /farms/F001/pastures/P999/events
  Then:
    [ ] Status: 200 OK
    [ ] Retorna array vacío: "content": []
    [ ] Sin error
    [ ] Metadata: totalElements=0
```

### AC#14: Export de Historial
```gherkin
Scenario: Descargar historial como JSON/CSV
  When GET /farms/F001/pastures/P001/events/export?format=json
  Then:
    [ ] Retorna todos los eventos en JSON
    [ ] Headers: Content-Disposition: attachment; filename="events.json"
  
  When GET /farms/F001/pastures/P001/events/export?format=csv
  Then:
    [ ] Retorna CSV con columnas: timestamp, type, userId, userName, details
    [ ] Descarga como archivo adjunto
    [ ] Fácil abrir en Excel
```

### AC#15: Documentación de API
```gherkin
Scenario: API está bien documentada
  Given endpoint GET /farms/{farmId}/pastures/{pastureId}/events
  Then:
    [ ] Swagger/OpenAPI documentado
    [ ] Parámetros explicados (type, page, size, sort)
    [ ] Ejemplos de request/response
    [ ] Códigos de error documentados
    [ ] Fácil para frontend consumir
```

---

## 📊 **Especificación Técnica**

### Endpoints REST

#### 1. GET /farms/{farmId}/pastures/{pastureId}/events
```
Descripción: Obtener historial de eventos de un potrero

Query Parameters:
  - type: string (OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR)
  - startDate: ISO 8601 date (2026-01-01)
  - endDate: ISO 8601 date (2026-01-31)
  - userId: string (USER-123)
  - userName: string (Carlos)
  - page: int (default: 0, min: 0)
  - size: int (default: 50, min: 1, max: 500)
  - sort: string (timestamp,DESC | eventType,ASC)
  - includeStats: boolean (default: false)

Response 200:
{
  "content": [
    {
      "id": "EVT-001",
      "eventType": "OPEN",
      "timestamp": "2026-01-05T10:30:00Z",
      "pastureId": "P001",
      "userId": "USER-123",
      "userName": "Carlos López",
      "details": {
        "height": 45,
        "notes": "Abierto para animales"
      },
      "beforeState": {
        "status": "DISPONIBLE",
        "currentHeight": 0,
        "eta": 0
      },
      "afterState": {
        "status": "EN_USO",
        "currentHeight": 45,
        "eta": 25
      }
    }
  ],
  "totalElements": 45,
  "totalPages": 1,
  "currentPage": 0,
  "size": 50,
  "hasNext": false,
  "hasPrevious": false,
  "stats": {
    "totalEvents": 45,
    "eventCounts": {
      "OPEN": 15,
      "CLOSE": 14,
      "MAINTENANCE_SET": 8,
      "MAINTENANCE_CLEAR": 8
    },
    "lastEventAt": "2026-01-05T10:30:00Z",
    "firstEventAt": "2025-01-10T08:00:00Z"
  }
}

Response 400: Bad Request
{
  "error": "Parámetro 'page' debe ser >= 0"
}

Response 403: Forbidden
{
  "error": "No tienes permiso para ver este historial"
}

Response 404: Not Found
{
  "error": "Potrero P001 no encontrado"
}
```

#### 2. GET /farms/{farmId}/events
```
Descripción: Obtener historial de todos los potreros de una finca

Query Parameters: (mismos que endpoint anterior)

Response 200:
{
  "content": [
    {
      "id": "EVT-001",
      "eventType": "OPEN",
      "timestamp": "2026-01-05T10:30:00Z",
      "pastureId": "P001",
      "userId": "USER-123",
      "userName": "Carlos López",
      ...
    }
  ],
  "totalElements": 250,
  "totalPages": 5,
  "currentPage": 0,
  "size": 50
}
```

#### 3. GET /farms/{farmId}/pastures/{pastureId}/events/{eventId}
```
Descripción: Obtener detalles de un evento específico

Response 200:
{
  "id": "EVT-001",
  "eventType": "OPEN",
  "timestamp": "2026-01-05T10:30:00Z",
  "pastureId": "P001",
  "userId": "USER-123",
  "userName": "Carlos López",
  "details": {...},
  "beforeState": {...},
  "afterState": {...}
}
```

#### 4. GET /farms/{farmId}/pastures/{pastureId}/events/export
```
Descripción: Descargar historial como JSON o CSV

Query Parameters:
  - format: json | csv (default: json)
  - type, startDate, endDate, etc. (mismos filtros)

Response 200:
  Content-Type: application/json o text/csv
  Content-Disposition: attachment; filename="events.json"
```

### Modelo de Datos

#### PastureEvent Entity
```java
@Entity
@Table(name = "pasture_events", indexes = {
  @Index(name = "idx_pasture_id", columnList = "pasture_id"),
  @Index(name = "idx_timestamp", columnList = "timestamp"),
  @Index(name = "idx_event_type", columnList = "event_type"),
  @Index(name = "idx_user_id", columnList = "user_id")
})
public class PastureEvent {
  
  @Id
  private String id; // EVT-001, EVT-002, etc
  
  @Enumerated(EnumType.STRING)
  private EventType eventType; // OPEN, CLOSE, MAINTENANCE_SET, etc
  
  private LocalDateTime timestamp;
  private String pastureId;
  private String userId;
  private String userName;
  
  @Convert(converter = JsonAttributeConverter.class)
  private Map<String, Object> details; // { height: 45, notes: "..." }
  
  @Convert(converter = JsonAttributeConverter.class)
  private PastureState beforeState;
  
  @Convert(converter = JsonAttributeConverter.class)
  private PastureState afterState;
  
  private String farmId;
}
```

#### PastureState (Embedded)
```java
public class PastureState {
  private Status status;
  private Integer currentHeightCm;
  private Integer eta;
  private String substatus;
  private LocalDateTime lastUseAt;
  private LocalDateTime lastClosedAt;
}
```

### Servicios

#### PastureEventService.java
```java
public class PastureEventService {
  
  public Page<PastureEventDTO> getPastureEvents(
    String farmId,
    String pastureId,
    PastureEventFilter filter,
    Pageable pageable
  ) {
    // Validar acceso
    // Construir query con filtros
    // Retornar página de eventos
  }
  
  public Page<PastureEventDTO> getFarmEvents(
    String farmId,
    PastureEventFilter filter,
    Pageable pageable
  ) {
    // Similar pero para todos los potreros
  }
  
  public PastureEventDTO getEvent(String farmId, String eventId) {
    // Obtener evento específico
  }
  
  public byte[] exportEvents(
    String farmId,
    String pastureId,
    PastureEventFilter filter,
    String format // json, csv
  ) {
    // Exportar a JSON o CSV
  }
}
```

#### PastureEventRepository.java
```java
@Repository
public interface PastureEventRepository extends JpaRepository<PastureEvent, String> {
  
  Page<PastureEvent> findByPastureIdAndEventTypeIn(
    String pastureId,
    Collection<EventType> types,
    Pageable pageable
  );
  
  Page<PastureEvent> findByPastureIdAndTimestampBetween(
    String pastureId,
    LocalDateTime start,
    LocalDateTime end,
    Pageable pageable
  );
  
  Page<PastureEvent> findByFarmIdAndTimestampBetween(
    String farmId,
    LocalDateTime start,
    LocalDateTime end,
    Pageable pageable
  );
  
  List<PastureEvent> findByPastureIdOrderByTimestampDesc(String pastureId);
}
```

### DTOs

#### PastureEventDTO.java
```java
public class PastureEventDTO {
  private String id;
  private EventType eventType;
  private LocalDateTime timestamp;
  private String pastureId;
  private String userId;
  private String userName;
  private Map<String, Object> details;
  private PastureState beforeState;
  private PastureState afterState;
}
```

#### PastureEventFilter.java
```java
public class PastureEventFilter {
  private List<EventType> types;
  private LocalDate startDate;
  private LocalDate endDate;
  private String userId;
  private String userName;
  
  // constructor, getters, setters
}
```

#### PastureEventStats.java
```java
public class PastureEventStats {
  private Long totalEvents;
  private Map<EventType, Long> eventCounts;
  private LocalDateTime lastEventAt;
  private LocalDateTime firstEventAt;
}
```

### Controller

#### PastureEventController.java
```java
@RestController
@RequestMapping("/farms/{farmId}/pastures/{pastureId}/events")
public class PastureEventController {
  
  @GetMapping
  public ResponseEntity<Page<PastureEventDTO>> getPastureEvents(
    @PathVariable String farmId,
    @PathVariable String pastureId,
    @RequestParam(required = false) String type,
    @RequestParam(required = false) LocalDate startDate,
    @RequestParam(required = false) LocalDate endDate,
    @RequestParam(required = false) String userId,
    @RequestParam(required = false) String userName,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "50") int size,
    @RequestParam(defaultValue = "timestamp,DESC") String sort,
    @RequestParam(defaultValue = "false") boolean includeStats
  ) {
    PastureEventFilter filter = new PastureEventFilter(...);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    
    Page<PastureEventDTO> result = eventService.getPastureEvents(
      farmId, pastureId, filter, pageable
    );
    
    return ResponseEntity.ok(result);
  }
  
  @GetMapping("/{eventId}")
  public ResponseEntity<PastureEventDTO> getEvent(
    @PathVariable String farmId,
    @PathVariable String eventId
  ) {
    return ResponseEntity.ok(eventService.getEvent(farmId, eventId));
  }
  
  @GetMapping("/export")
  public ResponseEntity<byte[]> exportEvents(
    @PathVariable String farmId,
    @PathVariable String pastureId,
    @RequestParam(defaultValue = "json") String format,
    // ... otros parámetros de filtro
  ) {
    byte[] data = eventService.exportEvents(farmId, pastureId, filter, format);
    return ResponseEntity.ok()
      .header("Content-Disposition", "attachment; filename=\"events." + format + "\"")
      .body(data);
  }
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`PastureEvent.java`** - Entity
2. **`PastureEventRepository.java`** - Repository
3. **`PastureEventService.java`** - Service
4. **`PastureEventDTO.java`** - DTO
5. **`PastureEventFilter.java`** - Filter
6. **`PastureEventStats.java`** - Stats DTO
7. **`PastureEventController.java`** - REST Controller
8. **`PastureEventControllerTest.java`** - Tests

### Archivos a Modificar

1. **`PastureService.java`** - Agregar método para registrar eventos (opcional)
2. **`EventType.java`** - Asegurar enum tiene todos los tipos

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear Entity PastureEvent
- Tabla con índices (pastureId, timestamp, eventType, userId)
- Campos: id, eventType, timestamp, details, beforeState, afterState

### Paso 2: Crear Repository
- Queries con filtros (type, date range, user)
- Índices para performance

### Paso 3: Crear DTO y Filter
- Serialización segura
- Mapeo desde Entity

### Paso 4: Implementar Service
- Validar acceso
- Construir queries dinámicas
- Paginar resultados
- Export JSON/CSV

### Paso 5: Crear Controller
- Endpoints REST
- Validación de parámetros
- Manejo de errores

### Paso 6: Tests
- Tests unitarios para service
- Tests de integración para controller
- Tests con diferentes filtros

---

## 🧪 **Casos de Prueba**

### Test Unitarios

```java
describe('PastureEventService', () => {
  
  test('retorna eventos ordenados por timestamp DESC', () => {
    List<PastureEvent> events = service.getPastureEvents(...);
    assertTrue(events.get(0).getTimestamp() > events.get(1).getTimestamp());
  });
  
  test('filtra por tipo correctamente', () => {
    filter.setTypes(List.of(EventType.CLOSE));
    Page<PastureEventDTO> result = service.getPastureEvents(...);
    assertTrue(result.getContent().stream()
      .allMatch(e -> e.getEventType() == EventType.CLOSE));
  });
});
```

### Test de Integración

```java
describe('PastureEventController', () => {
  
  test('GET /events retorna 200 con eventos', () => {
    mockMvc.perform(get("/farms/F001/pastures/P001/events"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").isArray());
  });
  
  test('GET /events?type=CLOSE filtra correctamente', () => {
    mockMvc.perform(get("/farms/F001/pastures/P001/events?type=CLOSE"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[*].eventType", hasItems("CLOSE")));
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD)**

### Escenario 1: Ver Historial Completo
```gherkin
Scenario: Usuario ve historial de potrero
  Given potrero P001 con 10 eventos
  When GET /farms/F001/pastures/P001/events
  Then retorna todos los eventos ordenados por fecha
```

### Escenario 2: Filtrar por Tipo
```gherkin
Scenario: Filtrar eventos por tipo
  Given potrero con eventos: OPEN, CLOSE, MAINTENANCE_SET
  When GET /farms/F001/pastures/P001/events?type=CLOSE
  Then retorna solo CLOSE (1 evento)
```

### Escenario 3: Paginar Resultados
```gherkin
Scenario: Paginar historial grande
  Given potrero con 250 eventos
  When GET /farms/F001/pastures/P001/events?page=0&size=50
  Then retorna primeros 50 con totalPages=5
```

---

## 📚 **Referencias y Dependencias**

**Dependencias**:
- Spring Data JPA
- Spring Web
- Jackson (JSON)
- OpenCSV (CSV export)

**Componentes relacionados**:
- PastureService (HU#1)
- Auditoría (HU#13)

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Paginated Query API + Event History Service + Query Optimization

**Justificación:** **Paginated Query**: REST endpoint con filtros y paginación standard. **Event History Service**: Encapsula lógica consulta + auditoría. **Query Optimization**: Índices en DynamoDB (timestamp, eventType, userId). **Before/After State**: Auditoría completa cambios. **Performance**: <500ms para 1000+ eventos. **Escalabilidad**: GSI para filtros comunes.

**Componentes Afectados:**

- **PastureEventHistoryController.java (Nuevo):** Endpoints REST. GET `/farms/{farmId}/pastures/{pastureId}/events` + filtros. Query params: page, size, type, startDate, endDate, userId, sort. Retorna: paginated response + metadata.

- **EventHistoryService.java (Nuevo):** Lógica consulta + filtros. Métodos: `getEventHistory(farmId, pastureId, filters) → Page<EventHistoryDTO>`. Aplica filtros, paginación, ordenamiento.

- **EventHistoryRepository.java (Nuevo):** Query a DynamoDB con índices. Métodos: `queryByPastureAndType`, `queryByDateRange`, `queryByUser`. Usa GSI optimizados.

- **EventHistoryDTO.java (Nuevo):** DTO salida. Campos: id, eventType, timestamp, userId, userName, details (JSON), beforeState, afterState.

- **EventHistoryRequest.java (Nuevo):** DTO entrada filtros. Page, size, type (enum), startDate, endDate, userId, sort.

- **EventHistoryProcessor.java (Nuevo):** Orquestador. Delega a service, manejo de excepciones, logging.

**Hitos:**
1. EventHistoryDTO.java + EventHistoryRequest.java (sin dependencias)
2. EventHistoryRepository.java (depende: DynamoDB)
3. EventHistoryService.java (depende: Repository)
4. EventHistoryProcessor.java (depende: Service)
5. PastureEventHistoryController.java (depende: Processor)

### Validación de Impacto

✅ **Paginated Query**: Standard REST query, escalable
✅ **Query Optimization**: Índices + GSI en DynamoDB
✅ **Before/After State**: Auditoría completa
✅ **Performance**: <500ms para 1000+ eventos
✅ **Filtros**: tipo, fecha, usuario combinables
✅ **Escalabilidad**: Sin N+1 queries

### Notas Técnicas

**EventHistoryDTO - Estructura:**
```java
@Data
@Builder
public class EventHistoryDTO {
  private String id;
  private String eventType;
  private Instant timestamp;
  private String userId;
  private String userName;
  private Map<String, Object> details;
  private Map<String, Object> beforeState;
  private Map<String, Object> afterState;
}
```

**Query Pattern - DynamoDB Índices:**
```
PK: PASTURE#{pastureId}
SK: EVENT#{timestamp}

GSI1:
  PK: PASTURE#{pastureId}#TYPE#{eventType}
  SK: timestamp

GSI2:
  PK: USER#{userId}
  SK: timestamp
```

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-001: Backend POST Eventos (genera eventos)
- ✅ PASTURES-HU-003: Detail Panel (usa historial)
- → PASTURES-HU-012: Historial Eventos (esta - auditoría)
- → PASTURES-HU-013: Auditoría (usa eventos)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Paginated Query + Event History (auditoría completa)

---

## 🔧 **Refinamiento Técnico**

### API Contract

**Endpoint:**
```
GET /farms/{farmId}/pastures/{pastureId}/events
Query params: page=0&size=50&type=OPEN,CLOSE&startDate=2026-01-01

Response (200 OK):
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 2,
  "currentPage": 0,
  "pageSize": 50
}
```

### Pseudocódigo - Backend Query

```pseudocode
GET /farms/{farmId}/pastures/{pastureId}/events

// 1. Validar parámetros
VALIDATE farmId, pastureId, page, size

// 2. Construir query con filtros
query = EVENT_TABLE.query()
query.filterBy(pastureId = pastureId)
IF type IS SPECIFIED THEN
  query.filterBy(type IN [OPEN, CLOSE, ...])
IF startDate IS SPECIFIED THEN
  query.filterBy(createdAt >= startDate)

// 3. Ordenar por fecha descendente
query.sortBy(createdAt DESC)

// 4. Paginar
query.limit(size)
query.offset(page * size)

// 5. Ejecutar y retornar
events = query.execute()
RETURN {content: events, totalElements: count, totalPages: ceil(count/size)}
```

### DynamoDB Schema - Events Table

```
PK (String): farmId#pastureId
SK (String): EVENT#{timestamp}#{eventId}
Attributes:
  - type (String): OPEN|CLOSE|MAINTENANCE_SET
  - timestamp (ISO8601): 2026-01-09T10:30:00Z
  - user (String): user@farm.com
  - beforeValues (Map): {status: "DISPONIBLE", ...}
  - afterValues (Map): {status: "EN_USO", ...}
  - TTL (Number): optional retention (epoch)

Índices:
  - GSI: pastureId-timestamp (para queries rápidas)
```

### Testing Strategy

**Integration Tests:**
```java
@Test
void testGetEventsWithPagination() {
  // 100 eventos, page 0, size 50
  Response response = get("/farms/F001/pastures/P001/events?page=0&size=50");
  
  assertEquals(200, response.status);
  assertEquals(100, response.totalElements);
  assertEquals(2, response.totalPages);
  assertEquals(50, response.content.size());
}

@Test
void testGetEventsFiltered() {
  // Solo eventos de tipo CLOSE
  Response response = get("/farms/F001/pastures/P001/events?type=CLOSE");
  
  assertEquals(200, response.status);
  assertTrue(response.content.stream().allMatch(e -> e.type.equals("CLOSE")));
}
```

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `PastureEvent.java` entity con índices
- [ ] `PastureEventRepository.java` con queries
- [ ] `PastureEventService.java` con lógica
- [ ] `PastureEventDTO.java` serializable
- [ ] `PastureEventFilter.java` con filtros
- [ ] `PastureEventController.java` con endpoints
- [ ] GET /events endpoint funciona
- [ ] Filtros funcionan (type, date, user)
- [ ] Paginación funciona
- [ ] Ordenamiento funciona
- [ ] Antes/después del cambio incluido
- [ ] Estadísticas (bonus) funcionan
- [ ] Export JSON funciona
- [ ] Export CSV funciona
- [ ] Validación de acceso
- [ ] Potrero sin historial maneja gracefully
- [ ] Performance: respuesta < 500ms
- [ ] Índices creados en BD
- [ ] Tests unitarios: cobertura >= 80%
- [ ] Tests integración: todos los endpoints
- [ ] Validación de parámetros
- [ ] Error handling completo
- [ ] Swagger/OpenAPI documentado
- [ ] Code review aprobado
- [ ] Sin warnings de linting
- [ ] CI/CD green (build, test)
- [ ] Base para HU#13 (Auditoría)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
