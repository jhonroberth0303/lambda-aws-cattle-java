# 🌱 PASTURES-HU#1: Backend: Endpoint POST para Eventos OPEN/CLOSE/MAINTENANCE

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🔴 CRÍTICO (P0) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Propuesta aprobada | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend developer**, quiero crear un endpoint POST que permita aplicar eventos de transición (OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR) a un potrero, de tal forma que:

1. El evento se valida y procesa correctamente
2. El motor de estados calcula nuevas transiciones automáticas
3. El potrero se actualiza en DynamoDB con EntityPatch
4. El cliente recibe respuesta actualizada

Esto habilitará que el frontend pueda registrar acciones críticas de rotación con persistencia garantizada.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Endpoint Existe y Responde
```gherkin
Scenario: Crear endpoint POST /farms/{farmId}/pastures/{pastureId}/events
  Given el backend está corriendo en puerto 8080
  When se envía POST a /farms/F001/pastures/P001/events
  And el payload contiene: { "eventType": "OPEN", "user": "juan@farm.com", ... }
  Then el endpoint retorna HTTP 201 Created
  And la respuesta incluye el potrero actualizado en JSON
```

### AC#2: Evento OPEN - Abrir Potrero para Uso
```gherkin
Scenario: Aplicar evento OPEN a potrero disponible
  Given existe potrero P001:
    | status | DISPONIBLE |
    | lastUseAt | null |
    | currentHeight | 25 cm |
  When se envía:
    POST /farms/F001/pastures/P001/events
    {
      "eventType": "OPEN",
      "user": "juan.perez@farm.com",
      "lotId": "LOT001",
      "animals": 15
    }
  Then el backend:
    [ ] Valida que user no esté vacío
    [ ] Valida que lotId no esté vacío
    [ ] Valida que animals > 0
    [ ] Busca potrero en TABLE_PASTURE
    [ ] Busca plan de rotación para especie
    [ ] Crea instancia OpenEvent(lotId, animals, user)
    [ ] Aplica: pastureStatusEngine.applyEvent(pasture, plan, openEvent)
    [ ] Obtiene EntityPatch con cambios
    [ ] Guarda con: pastureRepository.update(pk, patch)
    [ ] Retorna HTTP 201 con potrero actualizado

  And el potrero cambia:
    | Antes | Después |
    | status: DISPONIBLE | status: EN_USO |
    | lastUseAt: null | lastUseAt: 2025-12-09 (ahora) |
    | lastLotId: null | lastLotId: LOT001 |
    | lastAnimalCount: null | lastAnimalCount: 15 |
    
  And la respuesta JSON es:
    {
      "id": "P001",
      "name": "Potrero 1",
      "species": "KIKUYO",
      "status": "EN_USO",
      "lastUseAt": "2025-12-09T10:30:45Z",
      "lastLotId": "LOT001",
      "lastAnimalCount": 15,
      "eta": null,
      "etaMessage": null
    }
```

### AC#3: Evento CLOSE - Cerrar Potrero (Entrada al Descanso)
```gherkin
Scenario: Aplicar evento CLOSE a potrero en uso
  Given existe potrero P002:
    | status | EN_USO |
    | lastUseAt | 2025-12-01 |
    | lastLotId | LOT001 |
    | currentHeight | 15 cm |
  When se envía:
    POST /farms/F001/pastures/P002/events
    {
      "eventType": "CLOSE",
      "user": "juan.perez@farm.com",
      "lotId": "LOT001",
      "animals": 15,
      "residualCm": 8
    }
  Then el backend:
    [ ] Valida residualCm > 0
    [ ] Valida residualCm <= currentHeight
    [ ] Busca potrero y plan
    [ ] Crea CloseEvent(lotId, animals, residualCm, user)
    [ ] Aplica: pastureStatusEngine.applyEvent(pasture, plan, closeEvent)
    [ ] Obtiene EntityPatch
    [ ] Guarda cambios

  And el potrero cambia:
    | Campo | Antes | Después |
    | status | EN_USO | EN_DESCANSO |
    | lastClosedAt | null | 2025-12-09T14:30:45Z |
    | residualHeightCm | 15 | 8 |
    | lastLotId | LOT001 | (sin cambio) |

  And el sistema calcula automáticamente:
    [ ] daysResting = 0 (recién cerrado)
    [ ] restDaysMin = plan.restDaysMin (ej: 30 días para Kikuyo)
    [ ] currentHeight → calcular altura predicha para ETA
    [ ] ETA = (currentHeight - minHeightRequired) / growthRate + (restDaysMin - daysResting)
    [ ] Si ETA <= 0 → status transita a DISPONIBLE automáticamente
```

### AC#4: Evento MAINTENANCE_SET - Bloquear Potrero
```gherkin
Scenario: Aplicar bloqueo de mantenimiento
  Given existe potrero P003 en estado EN_DESCANSO
  When se envía:
    POST /farms/F001/pastures/P003/events
    {
      "eventType": "MAINTENANCE_SET",
      "user": "admin@farm.com",
      "substatus": "FERTILIZANDO",
      "holdUntil": "2025-12-20"
    }
  Then el backend:
    [ ] Valida holdUntil >= fecha actual
    [ ] Valida substatus válido (FERTILIZANDO, REPARACION, CUARENTENA, etc.)
    [ ] Busca potrero y plan
    [ ] Crea MaintenanceSetEvent(substatus, holdUntil, user)
    [ ] Aplica: pastureStatusEngine.applyEvent(pasture, plan, event)
    [ ] Guarda cambios

  And el potrero:
    [ ] status cambia a MANTENIMIENTO
    [ ] substatus = FERTILIZANDO
    [ ] holdUntil = 2025-12-20
    [ ] ETA = INFINITY (potrero bloqueado indefinidamente)
```

### AC#5: Evento MAINTENANCE_CLEAR - Desbloquear Potrero
```gherkin
Scenario: Remover bloqueo de mantenimiento
  Given existe potrero P004:
    | status | MANTENIMIENTO |
    | substatus | FERTILIZANDO |
    | holdUntil | 2025-12-20 |
  When se envía:
    POST /farms/F001/pastures/P004/events
    {
      "eventType": "MAINTENANCE_CLEAR",
      "user": "admin@farm.com"
    }
  Then el backend:
    [ ] Valida potrero está en MANTENIMIENTO
    [ ] Busca potrero y plan
    [ ] Crea MaintenanceClearEvent(user)
    [ ] Aplica: pastureStatusEngine.applyEvent(pasture, plan, event)
    [ ] PastureStatusEngine calcula nuevo estado:
        - Si EN_DESCANSO: mira ETA, posible transición a DISPONIBLE
        - Si EN_USO: permanece EN_USO
    [ ] holdUntil = null
    [ ] substatus = NINGUNO
    [ ] Guarda cambios

  And el potrero retorna con estado calculado
```

### AC#6: Validación de Entrada - Casos de Error
```gherkin
Scenario: Validación rechaza payload inválido
  Given el endpoint POST /farms/F001/pastures/P001/events
  
  When se envía eventType inválido:
    { "eventType": "INVALID" }
  Then retorna HTTP 400 Bad Request con mensaje:
    { "error": "eventType debe ser: OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR" }
  
  When se envía sin user:
    { "eventType": "OPEN", "lotId": "LOT001", "animals": 15 }
  Then retorna HTTP 400 con:
    { "error": "user es requerido" }
  
  When se envía CLOSE sin residualCm:
    { "eventType": "CLOSE", "lotId": "LOT001", "animals": 15 }
  Then retorna HTTP 400 con:
    { "error": "residualCm es requerido para CLOSE" }
  
  When se envía residualCm > currentHeight:
    { "eventType": "CLOSE", "lotId": "LOT001", "animals": 15, "residualCm": 50 }
    Y currentHeight del potrero es 30 cm
  Then retorna HTTP 400 con:
    { "error": "residualCm (50 cm) no puede ser mayor a altura actual (30 cm)" }
```

### AC#7: Manejo de Errores - Potrero No Existe
```gherkin
Scenario: Buscar potrero inexistente
  When se envía:
    POST /farms/F001/pastures/NONEXISTENT/events
    { "eventType": "OPEN", ... }
  Then retorna HTTP 404 Not Found con:
    { "error": "Potrero NONEXISTENT no encontrado" }
```

### AC#8: Manejo de Errores - Plan de Rotación No Existe
```gherkin
Scenario: Potrero sin plan de rotación definido
  Given existe potrero P005 con especie "DESCONOCIDA"
  And NO existe plan para "DESCONOCIDA" en TABLE_PLAN
  When se envía evento OPEN
  Then retorna HTTP 400 con:
    { "error": "No existe plan de rotación para especie: DESCONOCIDA" }
```

### AC#9: Transición Automática Post-Evento
```gherkin
Scenario: Tras evento CLOSE, si ETA <= 0 → DISPONIBLE automáticamente
  Given potrero P006:
    | status | EN_USO |
    | species | KIKUYO |
    | currentHeight | 25 cm (ya recuperado) |
  And plan KIKUYO: restDaysMin=30, minHeightRequired=20, growthRate=2.5
  When se envía CLOSE con residualCm=8:
    POST .../P006/events
    { "eventType": "CLOSE", "residualCm": 8, ... }
  Then:
    [ ] Evento CLOSE se aplica
    [ ] Status EN_DESCANSO
    [ ] Motor calcula ETA con altura actual (predicha):
        ETA = (25 - 20) / 2.5 + (30 - 0) = 2 + 30 = 32 días
    [ ] ETA > 0, entonces status permanece EN_DESCANSO
    [ ] Respuesta retorna status: "EN_DESCANSO", eta: 32

  But cuando altura es suficiente:
    [ ] currentHeight después de CLOSE = 28 cm (ejemplo)
    [ ] ETA = (28 - 20) / 2.5 + (30 - 0) = 3.2 + 30 = 33.2 (redondeado 33)
    [ ] Si pasan varios días sin uso, tick() decrece daysResting
    [ ] Cuando ETA <= 0: status DISPONIBLE
    [ ] Respuesta retorna status: "DISPONIBLE", eta: null
```

### AC#10: Auditoría - Registro de Quién y Cuándo
```gherkin
Scenario: Registrar cambio en historial (futuro: HU#13)
  Given se aplica evento OPEN
  When el evento se persiste
  Then se registra en TABLE_AUDIT_LOGS o atributo auditLog:
    | Campo | Valor |
    | timestamp | 2025-12-09T10:30:45Z |
    | user | juan.perez@farm.com |
    | operation | EVENT_OPEN |
    | pastureId | P001 |
    | changes | { status: DISPONIBLE → EN_USO, lastLotId: null → LOT001 } |
    
    # Nota: puede ser implementado en HU#13, pero preparar estructura aquí
```

---

## 📊 **Especificación Técnica**

### Estructura de DTOs

#### Entrada: `PastureEventRequest.java`
```java
@Data
@Builder
public class PastureEventRequest {
    @NotEmpty(message = "eventType es requerido")
    private String eventType; // OPEN | CLOSE | MAINTENANCE_SET | MAINTENANCE_CLEAR
    
    @NotEmpty(message = "user es requerido")
    private String user; // Email o username
    
    // Campos para OPEN
    private String lotId; // Lote/grupo siendo ingresado
    private Integer animals; // Cantidad de animales
    
    // Campos para CLOSE
    private Integer residualCm; // Altura residual post-cierre (cm)
    
    // Campos para MAINTENANCE_SET
    private String substatus; // FERTILIZANDO | REPARACION | CUARENTENA | OTRO
    private String holdUntil; // ISO date: YYYY-MM-DD
    
    // Validaciones personalizadas
    @AssertTrue(message = "OPEN requiere lotId y animals")
    private boolean isValidOpen() {
        if ("OPEN".equals(eventType)) {
            return notEmpty(lotId) && animals != null && animals > 0;
        }
        return true;
    }
    
    @AssertTrue(message = "CLOSE requiere residualCm")
    private boolean isValidClose() {
        if ("CLOSE".equals(eventType)) {
            return residualCm != null && residualCm > 0;
        }
        return true;
    }
    
    @AssertTrue(message = "MAINTENANCE_SET requiere substatus y holdUntil")
    private boolean isValidMaintenanceSet() {
        if ("MAINTENANCE_SET".equals(eventType)) {
            return notEmpty(substatus) && notEmpty(holdUntil);
        }
        return true;
    }
}
```

#### Salida: `PastureDTO.java` (existente, actualizar)
```java
@Data
@Builder
public class PastureDTO {
    private String id;
    private String name;
    private String species;
    private String status;
    private String substatus; // Nuevo: para MANTENIMIENTO
    private Integer eta;
    private String etaMessage;
    private String lastUseAt;
    private String lastClosedAt; // Nuevo
    private String lastLotId; // Nuevo
    private Integer lastAnimalCount; // Nuevo
    private Integer residualHeightCm; // Nuevo
    private String holdUntil; // Nuevo
    private Double areaHa;
    private String notes;
    // ... otros campos existentes
}
```

### Endpoints Definidos

#### **POST /farms/{farmId}/pastures/{pastureId}/events**

**Path Parameters**:
- `farmId` (String, required): ID de finca (ej: F001)
- `pastureId` (String, required): ID de potrero (ej: P001)

**Request Body**:
```json
{
  "eventType": "OPEN",
  "user": "juan.perez@farm.com",
  "lotId": "LOT001",
  "animals": 15
}
```

**Response 201 Created**:
```json
{
  "id": "P001",
  "name": "Potrero 1",
  "species": "KIKUYO",
  "status": "EN_USO",
  "substatus": "NINGUNO",
  "eta": 30,
  "etaMessage": "Disponible en 30 días",
  "lastUseAt": "2025-12-09T10:30:45Z",
  "lastClosedAt": null,
  "lastLotId": "LOT001",
  "lastAnimalCount": 15,
  "residualHeightCm": null,
  "holdUntil": null,
  "areaHa": 0.8,
  "notes": null
}
```

**Response 400 Bad Request**:
```json
{
  "timestamp": "2025-12-09T10:30:45Z",
  "status": 400,
  "error": "Bad Request",
  "message": "eventType debe ser: OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR",
  "path": "/farms/F001/pastures/P001/events"
}
```

**Response 404 Not Found**:
```json
{
  "timestamp": "2025-12-09T10:30:45Z",
  "status": 404,
  "error": "Not Found",
  "message": "Potrero P001 no encontrado",
  "path": "/farms/F001/pastures/P001/events"
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`PastureEventController.java`**
   - Recibir POST /farms/{farmId}/pastures/{pastureId}/events
   - Validar path params
   - Delegar a PastureEventProcessor

2. **`PastureEventRequest.java`** (DTO entrada)
   - Campos para OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR
   - Validaciones custom (@AssertTrue)

3. **`PastureEventProcessor.java`** (nueva o parte de PastureProcessor)
   - Orquestar flujo de evento
   - Convertir request a PastureEvent (sealed interface)
   - Llamar engine y repository
   - Mapear resultado a DTO

### Modificar Archivos

1. **`PastureService.java`**
   - Agregar método: `applyEvent(String pastureId, PastureEventRequest request)`
   - Buscar potrero
   - Buscar plan
   - Validar transición

2. **`PastureRepository.java`**
   - Asegurar método `update(String pk, EntityPatch patch)` existe
   - Lógica de guardado con UpdateItem

3. **`PastureDTO.java`**
   - Agregar campos: `substatus`, `lastClosedAt`, `lastLotId`, `lastAnimalCount`, `residualHeightCm`, `holdUntil`
   - Actualizar mapper

4. **`Pasture.java`** (Entity)
   - Asegurar que Entity tiene todos los campos para soportar eventos

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear PastureEventController

```java
@RestController
@RequestMapping("/farms/{farmId}/pastures/{pastureId}/events")
public class PastureEventController {
    
    private final PastureEventProcessor processor;
    
    @PostMapping
    public ResponseEntity<PastureDTO> applyEvent(
        @PathVariable String farmId,
        @PathVariable String pastureId,
        @Valid @RequestBody PastureEventRequest request) {
        
        PastureDTO result = processor.applyEvent(farmId, pastureId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
```

### Paso 2: Crear PastureEventProcessor

```java
@Component
public class PastureEventProcessor {
    
    private final PastureService service;
    private final PastureMapper mapper;
    
    public PastureDTO applyEvent(String farmId, String pastureId, PastureEventRequest request) {
        try {
            Pasture pasture = service.applyEvent(farmId, pastureId, request);
            return mapper.toDTO(pasture);
        } catch (RepositoryException e) {
            throw new ProcessingException("Failed to apply event", e);
        }
    }
}
```

### Paso 3: Agregar Método a PastureService

```java
public Pasture applyEvent(String farmId, String pastureId, PastureEventRequest request) {
    // 1. Buscar potrero
    Pasture pasture = repository.findById("PASTURE#" + pastureId)
        .orElseThrow(() -> new IllegalArgumentException("Potrero no encontrado"));
    
    // 2. Buscar plan
    Plan plan = planRepository.findByFarmAndSpecies(farmId, pasture.getSpecies())
        .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));
    
    // 3. Validar evento
    validateEvent(request, pasture);
    
    // 4. Convertir request a PastureEvent
    PastureEvent event = requestToEvent(request);
    
    // 5. Aplicar evento con motor
    EntityPatch patch = statusEngine.applyEvent(pasture, plan, event);
    
    // 6. Guardar
    repository.update(pasture.getPk(), patch);
    
    // 7. Retornar actualizado
    return repository.findById(pasture.getPk()).orElse(pasture);
}

private void validateEvent(PastureEventRequest request, Pasture pasture) {
    if ("CLOSE".equals(request.getEventType())) {
        Integer currentHeight = pasture.getCurrentHeightCm();
        if (request.getResidualCm() > currentHeight) {
            throw new IllegalArgumentException(
                "residualCm no puede ser mayor a altura actual"
            );
        }
    }
}

private PastureEvent requestToEvent(PastureEventRequest request) {
    return switch(request.getEventType()) {
        case "OPEN" -> new OpenEvent(request.getLotId(), request.getAnimals(), request.getUser());
        case "CLOSE" -> new CloseEvent(request.getLotId(), request.getAnimals(), 
                                       request.getResidualCm(), request.getUser());
        case "MAINTENANCE_SET" -> new MaintenanceSetEvent(request.getSubstatus(), 
                                                          request.getHoldUntil(), request.getUser());
        case "MAINTENANCE_CLEAR" -> new MaintenanceClearEvent(request.getUser());
        default -> throw new IllegalArgumentException("Tipo de evento desconocido");
    };
}
```

---

## 🧪 **Casos de Prueba**

### Test Unitarios (JUnit 5)

```java
@ExtendWith(MockitoExtension.class)
class PastureEventProcessorTest {
    
    @Mock
    private PastureService service;
    
    @Mock
    private PastureMapper mapper;
    
    @InjectMocks
    private PastureEventProcessor processor;
    
    @Test
    @DisplayName("debe aplicar evento OPEN y retornar DTO actualizado")
    void testApplyEventOpen() {
        // Arrange
        Pasture pasture = PastureBuilder.buildKikuyo();
        PastureDTO dto = createMockDTO();
        PastureEventRequest request = createOpenEventRequest();
        
        when(service.applyEvent(any(), any(), any())).thenReturn(pasture);
        when(mapper.toDTO(pasture)).thenReturn(dto);
        
        // Act
        PastureDTO result = processor.applyEvent("F001", "P001", request);
        
        // Assert
        assertEquals("EN_USO", result.getStatus());
        assertEquals("LOT001", result.getLastLotId());
        verify(service).applyEvent("F001", "P001", request);
    }
    
    @Test
    @DisplayName("debe rechazar CLOSE sin residualCm")
    void testApplyEventCloseMissingResidual() {
        // Arrange
        PastureEventRequest request = new PastureEventRequest();
        request.setEventType("CLOSE");
        request.setUser("juan@farm.com");
        // residualCm no seteado
        
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> {
            processor.applyEvent("F001", "P001", request);
        });
    }
}
```

### Test Integración (Spring Boot Test)

```java
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class PastureEventControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PastureEventProcessor processor;
    
    @Test
    @DisplayName("POST /pastures/{id}/events debe retornar 201")
    void testPostEventSuccess() throws Exception {
        // Arrange
        PastureDTO mockResponse = createMockDTO();
        PastureEventRequest request = createOpenEventRequest();
        
        when(processor.applyEvent(any(), any(), any())).thenReturn(mockResponse);
        
        // Act & Assert
        mockMvc.perform(post("/farms/F001/pastures/P001/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("EN_USO"))
            .andExpect(jsonPath("$.lastLotId").value("LOT001"));
    }
    
    @Test
    @DisplayName("POST /pastures/{id}/events con payload inválido debe retornar 400")
    void testPostEventBadRequest() throws Exception {
        // Arrange
        String invalidPayload = "{\"eventType\": \"INVALID\"}";
        
        // Act & Assert
        mockMvc.perform(post("/farms/F001/pastures/P001/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest());
    }
}
```

---

## 🔄 **Escenarios de Prueba (BDD con Gherkin)**

### Escenario 1: Evento OPEN Exitoso
```gherkin
Scenario: Registrar evento OPEN en potrero disponible
  Given existe potrero P001 en estado DISPONIBLE con 25 cm de altura
  And existe plan Kikuyo con restDays=30, minHeight=20, growthRate=2.5
  When se envía:
    POST /farms/F001/pastures/P001/events
    {
      "eventType": "OPEN",
      "user": "juan.perez@farm.com",
      "lotId": "LOT001",
      "animals": 15
    }
  Then retorna HTTP 201 Created
  And el potrero tiene:
    | status | EN_USO |
    | lastUseAt | 2025-12-09T10:30:45Z |
    | lastLotId | LOT001 |
    | lastAnimalCount | 15 |
  And el motor calcula ETA considerando:
    | Campo | Valor |
    | currentHeight | 25 cm |
    | minHeightRequired | 20 cm |
    | growthRate | 2.5 cm/día |
    | daysResting | 0 |
    | restDaysMin | 30 |
    | ETA | (25-20)/2.5 + 30 = 2 + 30 = 32 días |
```

### Escenario 2: Evento CLOSE con Transición a DISPONIBLE
```gherkin
Scenario: Cerrar potrero y transitar a DISPONIBLE si altura es suficiente
  Given existe potrero P002 en EN_USO:
    | currentHeight | 30 cm |
    | lastUseAt | 2025-12-01 (8 días) |
  And plan Kikuyo: restDaysMin=30, minHeight=20, growthRate=2.5
  When se envía CLOSE con residualCm=10:
    POST /farms/F001/pastures/P002/events
    {
      "eventType": "CLOSE",
      "user": "juan.perez@farm.com",
      "lotId": "LOT001",
      "animals": 15,
      "residualCm": 10
    }
  Then retorna HTTP 201
  And el potrero:
    | Antes | Después |
    | status: EN_USO | status: EN_DESCANSO |
    | lastUseAt: 2025-12-01 | lastClosedAt: 2025-12-09 |
    | currentHeight: 30 cm | residualHeightCm: 10 cm |
  And motor calcula:
    [ ] Altura predicha (considerando pasados 8 días): 30 + (8 * 2.5) = 50 cm
    [ ] ETA = (50 - 20) / 2.5 + (30 - 8) = 12 + 22 = 34 días
    [ ] status permanece EN_DESCANSO (ETA > 0)
```

---

## 📚 **Referencias y Dependencias**

**Dependencias de otros componentes**:
- ✅ `PastureStatusEngine.applyEvent()` (completado, HU anterior)
- ✅ `EtaCalculator.calculateEta()` (completado, HU anterior)
- ✅ `PastureRepository` (completado, HU anterior)
- ✅ `PastureMapper.toDTO()` (completado, HU anterior)
- ✅ `PastureEntity` modelo (completado, HU anterior)

**Documentación relacionada**:
- [Pastures Overview](../../pastures/pastures-overview.md)
- [Eventos Architecture](../../pastures/events-architecture.md)
- [Flujo Dashboard Potreros](../../architecture/flujo-dashboard-potreros.md)
- [Análisis Table Design](../../analysis-table-design.md)

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Controller + Processor + Service + Repository (capas existentes + extensión)

**Justificación:** El patrón **Layered Architecture** ya está consolidado en el sistema y ha demostrado ser efectivo. La **capa repository** con `EntityPatch` ya maneja persistencia inteligente. El **motor de eventos (`PastureStatusEngine`)** ya existe y orquesta transiciones de forma correcta. Esta HU extiende la ruta existente `/farms/{farmId}/pastures` (GET) con un nuevo POST. La separación de concerns se mantiene: Controller recibe, Processor orquesta, Service valida, Motor calcula, Repository persiste. **Validación técnica completada**: Código real verificado - `PasturesController`, `PastureService`, `PastureStatusEngine.applyEvent()`, `EntityPatch`, `PastureRepository.applyPatch()` - todos ya existen y funcionan correctamente.

**Componentes Afectados:**

- **PasturesController (Modificación - Menor):** Agregar endpoint POST `/{pastureId}/events` que recibe `@Valid PastureEventRequest` y retorna HTTP 201 con `PastureDTO` actualizado. Delega a `PastureEventProcessor`.
  
- **PastureEventRequest (Nuevo):** DTO de entrada con validaciones automáticas. Clase con `@Data` y `@Builder` (Lombok). Campos: `eventType`, `user`, `lotId`, `animals`, `residualCm`, `substatus`, `holdUntil`. Validaciones con `@NotEmpty`, `@AssertTrue` para reglas condicionales por tipo de evento.
  
- **PastureEventProcessor (Nuevo):** Orquestador del flujo. Convierte `PastureEventRequest` → `PastureEvent` (sealed interface). Busca potrero y plan de rotación. Delega validaciones a motor. Obtiene patch y aplica a través de service. Mapea resultado a DTO. Dependencias: `PastureService`, `PlanService`, `PastureStatusEngine`, `PasturesMapper`.
  
- **PastureService (Modificación - Menor):** Agregar método `applyEvent(String farmId, String pastureId, PastureEventRequest request)`. Busca potrero (404 si no existe). Busca plan (400 si no existe). Valida transiciones específicas (ej: CLOSE requiere `residualCm <= currentHeight`). Orquesta motor + repository + mapper. Retorna `Pasture` actualizado.
  
- **EventType Enum (Nuevo):** Enumeración con valores OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR. Usada en `PastureEvent` e `PastureEventRequest`.
  
- **Event Classes (Nuevo/Completar):** `OpenEvent`, `CloseEvent`, `MaintenanceSetEvent`, `MaintenanceClearEvent` - implementan `PastureEvent` (sealed interface que ya existe).
  
- **PastureDTO (Modificación - Menor):** Agregar campos `lastLotId`, `lastAnimalCount`, `residualHeightCm`, `lastClosedAt`, `holdUntil` para soporte de nuevos eventos.
  
- **Pasture Entity (Modificación - Menor):** Sincronizar con nuevos campos del DTO si no están ya mapeados a DynamoDB.

**Hitos de Implementación:**

1. **PastureEventRequest.java** - DTO de entrada con validaciones (sin dependencias)
2. **EventType.java** - Enumeración de tipos (sin dependencias)
3. **Event Classes concretas** - Implementaciones de `PastureEvent` sealed interface
4. **PastureEventProcessor.java** - Orquestador (depende de: Service, StatusEngine, Mapper)
5. **PastureService.applyEvent()** - Agregar método a servicio existente (depende de: Repository, StatusEngine, Mapper)
6. **PasturesController - Agregar POST** - Nuevo endpoint (depende de: Processor)
7. **PastureDTO + Pasture Entity** - Agregar campos nuevos (sincronización)

### Validación de Impacto

**Hallazgos de validación técnica (Paso 1.5):**

✅ **Código Real Consultado:**
- `PasturesController.java` - Existe, estructura lista para extensión
- `PastureService.java` - Existe, maneja búsquedas y lógica de negocio
- `PastureStatusEngine.java` - **CRÍTICO**: Ya implementa `applyEvent(pasture, plan, event)` correctamente ✅
- `EntityPatch.java` - Ya existe, maneja aplicación de parches inteligentemente
- `PastureRepository.java` - Método `applyPatch(pk, patch)` ya implementado ✅
- `PasturesMapper.java` - Convierte Entity ↔ DTO correctamente

✅ **Dependencias Externas Disponibles:**
- `PlanService` - Existe, busca planes por farm + especie
- `EtaCalculator` - Integrado en `PastureStatusEngine`, cálculo automático
- `Pasture Entity` - Todos los campos requeridos ya existen
- Validaciones JSR-380 - Spring Boot incluida (`@NotEmpty`, `@AssertTrue`)

✅ **Impacto en Performance:**
- Operación GET actual (dashboard): lee múltiples potreros + aplica motor
- Operación POST nueva (evento): lee 1 potrero + lee 1 plan + aplica motor + persiste
- Sin impacto negativo: Misma lógica de motor, scope más acotado

✅ **Cadena de Invocación Verificada:**
```
POST /farms/{farmId}/pastures/{pastureId}/events
→ PasturesController (nuevo endpoint)
  → PastureEventProcessor.applyEvent() (nuevo)
    → PastureService.applyEvent() (método nuevo)
      → PastureStatusEngine.applyEvent() (ya existe, validado) ✅
        ↓ (retorna EntityPatch)
      → PastureRepository.applyPatch() (ya existe) ✅
        → DynamoDB UpdateItem
    → PasturesMapper.toDTO() (ya existe) ✅
  ← HTTP 201 + PastureDTO actualizado
```

✅ **Riesgos Identificados - Todos Mitigables:**
- **Validación de residualCm:** Validar `0 < residualCm <= currentHeight` en controller + validar en service
- **Inconsistencia Entity/DTO:** Sincronizar nuevos campos en ambas clases
- **Potrero no encontrado:** Lanzar `EntityNotFoundException` → HTTP 404
- **Plan no encontrado:** Lanzar `BusinessRuleException` → HTTP 400 con mensaje claro
- **Transición inválida:** Motor ya valida `isBlockedEffective()` - relanzar excepción del motor

### Notas Técnicas

**Sealed Interface - Patrón Type-Safe:**
```java
public sealed interface PastureEvent permits OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent {
    EventType type();
    String user();
}
```
Ya existe en codebase. Permite `switch(event.type())` con cobertura completa de compilador.

**Mapeo de Eventos - Conversión Request → Event:**
```
PastureEventRequest → switch(eventType) 
  OPEN → new OpenEvent(lotId, animals, user)
  CLOSE → new CloseEvent(lotId, animals, residualCm, user)
  MAINTENANCE_SET → new MaintenanceSetEvent(substatus, holdUntil, user)
  MAINTENANCE_CLEAR → new MaintenanceClearEvent(user)
```

**Auditoría - Preparación para HU#13:**
- Campo `user` en cada evento registra quién realizó el cambio
- Campo `lastUseAt` ya se registra automáticamente (Instant.now())
- Estructura lista para tabla de auditoría en futuro

**Integración con Motor Existente:**
El `PastureStatusEngine.applyEvent()` ya maneja:
- Validación de bloqueos efectivos
- Generación de EntityPatch con cambios
- Cálculo de ETA automático
- Transiciones de estado por reglas de negocio
- Sincronización de GSIs para indexación

### Referencias y Validación

**Documentación Consultada:**
- [cattle-lambda-function/docs/docs/architecture/index.md](../../../architecture/index.md) - GPS arquitectónico del sistema
- [architecture-cattle-lambda-function.md](../../../architecture/architecture-cattle-lambda-function.md) - Patrones Layered Architecture + Processor Pattern
- [flujo-dashboard-potreros.md](../../../architecture/flujo-dashboard-potreros.md) - Motor de rotación existente y flujo de eventos
- **Código Verificado**: PasturesController, PastureService, PastureStatusEngine, EntityPatch, PastureRepository

**Historias Relacionadas:**
- PASTURES-HU-001 es la primera del flujo de potreros (P0) - no hay historias arquitectónicas previas en este dominio

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Exploratorio (análisis sin restricciones predefinidas, propuesta fundamentada en arquitectura existente verificada)

---

## 🔧 **Refinamiento Técnico**

### Stack Tecnológico

**Backend Stack:**
- Java 17+ / Spring Boot 3.x
- Spring Web / Spring Data JPA
- AWS SDK (DynamoDB)
- Lombok (annotations)
- JUnit 5 / Mockito

**Database:**
- AWS DynamoDB (NoSQL)
- Tabla: `TABLE_PASTURE`
- PK: `farmId#pastureId` | SK: `PASTURE#{id}`
- Índices: GSI para queries por estado, ETA, etc

### API Contract

**Endpoint:**
```
POST /farms/{farmId}/pastures/{pastureId}/events
Content-Type: application/json
Authorization: Bearer {jwt}
```

**Request Body:**
```json
{
  "eventType": "OPEN|CLOSE|MAINTENANCE_SET|MAINTENANCE_CLEAR",
  "lotId": "LOT001",
  "animalCount": 15,
  "residualHeight": 8,
  "maintenanceSubstatus": "FERTILIZANDO",
  "holdUntil": "2025-12-20",
  "notes": "Optional notes",
  "user": "juan.perez@farm.com"
}
```

**Response (201 Created):**
```json
{
  "id": "P001",
  "status": "EN_USO",
  "lastUseAt": "2026-01-09T10:30:45Z",
  "lastLotId": "LOT001",
  "lastAnimalCount": 15,
  "eta": 22,
  "etaMessage": "Disponible en 22 días",
  "currentHeight": 25,
  "event": {
    "id": "EVT-001",
    "type": "OPEN",
    "timestamp": "2026-01-09T10:30:45Z",
    "user": "juan.perez@farm.com"
  }
}
```

**Error Responses:**
- 400: Validación fallida (campos requeridos, valores inválidos)
- 401: No autenticado
- 403: Sin permisos
- 404: Potrero no encontrado
- 409: Conflicto (ej: OPEN en potrero no DISPONIBLE)

### Pseudocódigo - Flujo Backend

```pseudocode
POST /farms/{farmId}/pastures/{pastureId}/events

// 1. Validar entrada
VALIDATE farmId, pastureId (no vacío)
VALIDATE eventType (enum: OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR)
VALIDATE user (no vacío)

// 2. Obtener potrero actual
pasture = pastureRepository.findById(PK: farmId#pastureId)
IF pasture == null THEN RETURN 404

// 3. Obtener plan rotación
plan = rotationPlanRepository.findBySpecies(pasture.species)

// 4. Crear evento según tipo
SWITCH eventType:
  CASE "OPEN":
    VALIDATE lotId, animalCount > 0
    event = new OpenEvent(lotId, animalCount, user)
    
  CASE "CLOSE":
    VALIDATE residualHeight > 0 AND residualHeight <= currentHeight
    event = new CloseEvent(residualHeight, animalCount, user)
    
  CASE "MAINTENANCE_SET":
    VALIDATE maintenanceSubstatus, holdUntil
    event = new MaintenanceEvent(maintenanceSubstatus, holdUntil, user)
    
  CASE "MAINTENANCE_CLEAR":
    event = new MaintenanceClearEvent(user)

// 5. Aplicar motor de estados
newState = pastureStatusEngine.applyEvent(pasture, plan, event)

// 6. Generar EntityPatch (solo cambios)
patch = EntityPatch.generate(pasture, newState)

// 7. Guardar en DynamoDB
pastureRepository.update(PK, patch)

// 8. Retornar 201 Created
RETURN {pasture, event}
```

### Database Schema - DynamoDB

**TABLE_PASTURE:**
```
PK (String): farmId#pastureId (example: F001#P001)
SK (String): PASTURE#metadata
Attributes:
  - id (String): P001
  - farmId (String): F001
  - name (String): Potrero 1
  - species (String): KIKUYO
  - areHa (Number): 5.5
  - status (String): DISPONIBLE|EN_USO|EN_DESCANSO|MANTENIMIENTO
  - lastUseAt (ISO8601): 2025-12-01T10:30:00Z
  - lastLotId (String): LOT001
  - lastAnimalCount (Number): 15
  - currentHeight (Number): 25 (cm)
  - eta (Number): 22 (días)
  - etaMessage (String): "Disponible en 22 días"
  - createdAt (ISO8601): auto
  - updatedAt (ISO8601): auto
  - lastEvent (Map):
    - type: OPEN|CLOSE|MAINTENANCE_SET|etc
    - timestamp: ISO8601
    - user: email
```

### Configuración Crítica

**application.properties:**
```properties
# DynamoDB
aws.dynamodb.region=us-east-1
aws.dynamodb.endpoint=http://localhost:8000 # local dev
TABLE_PASTURE=cattle-pastures-dev
TABLE_EVENTS=cattle-events-dev

# Validación
validation.animalCount.min=1
validation.animalCount.max=10000
validation.residualHeight.min=1
validation.residualHeight.max=50
```

### Dependencias Maven

```xml
<!-- Spring Boot 3.x -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- DynamoDB -->
<dependency>
  <groupId>software.amazon.awssdk</groupId>
  <artifactId>dynamodb</artifactId>
</dependency>

<!-- Validación -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
</dependency>

<!-- Testing -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
```

### Testing Strategy

**Unit Tests:**
- `PastureEventProcessorTest`: 95%+ cobertura
- `PastureServiceTest`: validaciones de eventos
- `PastureEventRequestValidatorTest`: validaciones entrada

**Integration Tests:**
- `PastureEventControllerTest`: endpoint HTTP
- LocalStack DynamoDB para tests

**Test Cases Críticos:**
- OPEN en potrero DISPONIBLE → éxito
- OPEN en potrero EN_USO → 409 Conflict
- CLOSE con altura residual válida → éxito
- MAINTENANCE_SET → status MANTENIMIENTO

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `PastureEventController.java` implementado y testeado
- [ ] `PastureEventRequest.java` con validaciones complete
- [ ] `PastureEventProcessor.java` orquesta eventos correctamente
- [ ] `PastureService.applyEvent()` implementado
- [ ] Tests unitarios: cobertura >= 85%
- [ ] Tests integración: todos pasan
- [ ] Manual testing en Postman/Insomnia: OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR
- [ ] Validaciones de error probadas (404, 400, etc.)
- [ ] Documentación actualizada (JSDoc/JavaDoc)
- [ ] Code review aprobado (2 approvals)
- [ ] Sin warnings de linting
- [ ] CI/CD green (build, test, coverage)
- [ ] Transiciones automáticas validadas
- [ ] Demostrable en staging environment

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
