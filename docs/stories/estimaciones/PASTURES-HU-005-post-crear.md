# 🌱 PASTURES-HU#5: Backend: POST para Crear Potrero

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟠 ALTO (P1) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - TABLE_COUNTERS para ID generation | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend developer**, quiero crear un endpoint POST que permita crear nuevos potreros en una finca, de tal forma que:

1. El endpoint valida datos de entrada (nombre, especie, área)
2. Verifica que existe un plan de rotación para la especie
3. Genera un ID único para el potrero (ej: P0001)
4. Crea el registro en DynamoDB con estado inicial DISPONIBLE
5. Calcula valores iniciales (ETA, altura actual)
6. Retorna el potrero creado con todos sus atributos

Esto habilitará que operarios o administradores creen nuevos potreros dinámicamente sin intervención manual en la BD.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Endpoint POST Existe y Responde
```gherkin
Scenario: Crear endpoint POST /farms/{farmId}/pastures
  Given el backend está corriendo
  When se envía POST a /farms/F001/pastures
  And el payload contiene datos válidos
  Then el endpoint retorna HTTP 201 Created
  And la respuesta incluye el potrero creado con ID generado
```

### AC#2: Crear Potrero con Datos Básicos
```gherkin
Scenario: Crear potrero nuevo exitosamente
  Given no existe potrero con ese nombre en la finca F001
  When se envía:
    POST /farms/F001/pastures
    {
      "name": "Potrero Nuevo",
      "species": "KIKUYO",
      "areaHa": 1.5,
      "notes": "Potrero recién establecido"
    }
  Then el backend:
    [ ] Valida que name no esté vacío (1-100 caracteres)
    [ ] Valida que species sea válida (existe plan)
    [ ] Valida que areaHa > 0 y <= 100
    [ ] Genera ID único: P0004 (o siguiente disponible)
    [ ] Crea registro en TABLE_PASTURE con:
        * pk: PASTURE#P0004
        * sk: PASTURE#P0004
        * name: "Potrero Nuevo"
        * species: "KIKUYO"
        * areaHa: 1.5
        * status: DISPONIBLE
        * notes: "Potrero recién establecido"
    [ ] Calcula valores iniciales:
        * currentHeightCm: 0 (o valor mínimo según plan)
        * eta: 0 (disponible ahora)
        * etaMessage: "Disponible para uso"
    [ ] Retorna HTTP 201 Created con potrero completo
```

### AC#3: Validación - Especie Válida
```gherkin
Scenario: Validar que la especie tiene plan de rotación
  Given no existe plan para "ESPECIE_DESCONOCIDA" en finca F001
  When se envía:
    POST /farms/F001/pastures
    {
      "name": "Potrero X",
      "species": "ESPECIE_DESCONOCIDA",
      "areaHa": 1.0
    }
  Then retorna HTTP 400 Bad Request:
    { "error": "Especie 'ESPECIE_DESCONOCIDA' no tiene plan de rotación" }
```

### AC#4: Validación - Nombre Requerido
```gherkin
Scenario: Rechazar nombre vacío o muy largo
  When se envía con name vacío:
    { "name": "" }
  Then retorna HTTP 400:
    { "error": "Nombre es requerido y debe tener 1-100 caracteres" }
  
  When se envía con name muy largo:
    { "name": "A" * 101 }  // 101 caracteres
  Then retorna HTTP 400:
    { "error": "Nombre no puede exceder 100 caracteres" }
```

### AC#5: Validación - Área Válida
```gherkin
Scenario: Rechazar área inválida
  When se envía areaHa <= 0:
    { "areaHa": 0 }
  Then retorna HTTP 400:
    { "error": "Área debe ser mayor a 0 hectáreas" }
  
  When se envía areaHa > 100:
    { "areaHa": 150 }
  Then retorna HTTP 400:
    { "error": "Área no puede ser mayor a 100 hectáreas" }
  
  When se envía areaHa no numérica:
    { "areaHa": "hectárea" }
  Then retorna HTTP 400:
    { "error": "Área debe ser un número válido" }
```

### AC#6: Generación de ID Único
```gherkin
Scenario: Generar ID único para nuevo potrero
  Given finca F001 tiene potreros: P0001, P0002, P0003
  When se crea nuevo potrero
  Then:
    [ ] Se genera ID: P0004 (siguiente número disponible)
    [ ] ID no existe en TABLE_PASTURE
    [ ] ID sigue formato: P{number} donde number >= 0001
    [ ] Se retorna en respuesta: "id": "P0004"
```

### AC#7: Estado Inicial Correcto
```gherkin
Scenario: Nuevo potrero tiene estado inicial DISPONIBLE
  Given se crea nuevo potrero
  When se retorna respuesta
  Then:
    [ ] status: DISPONIBLE
    [ ] eta: 0 (disponible ahora)
    [ ] etaMessage: "Disponible para uso"
    [ ] lastUseAt: null (nunca usado)
    [ ] lastClosedAt: null
    [ ] residualHeightCm: null
    [ ] currentHeightCm: 0 cm (o altura mínima según plan)
```

### AC#8: Valores Iniciales Calculados
```gherkin
Scenario: Calcular valores iniciales basados en plan
  Given se crea potrero con species=KIKUYO
  And KIKUYO tiene plan:
    | minHeightRequired | 20 cm |
    | growthRate | 2.5 cm/día |
    | restDaysMin | 30 días |
  When potrero se crea
  Then se calculan:
    [ ] currentHeightCm: 0 (o minHeightRequired si se desea iniciar con altura)
    [ ] eta: 0 (disponible inmediatamente)
    [ ] lastUseAt: null
    [ ] createdAt: timestamp actual
    [ ] updatedAt: timestamp actual
```

### AC#9: Notas Opcionales
```gherkin
Scenario: Campo notes es opcional y puede estar vacío
  When se envía sin notes:
    {
      "name": "Potrero",
      "species": "KIKUYO",
      "areaHa": 1.0
      // notes omitido
    }
  Then:
    [ ] Se crea potrero exitosamente
    [ ] notes: null en base de datos
    [ ] No hay error
  
  When se envía con notes muy largo (>500):
    { "notes": "A" * 501 }
  Then retorna HTTP 400:
    { "error": "Notas no pueden exceder 500 caracteres" }
```

### AC#10: Manejo de Error - Finca No Existe
```gherkin
Scenario: Intenta crear potrero en finca inexistente
  Given no existe finca F999
  When se envía:
    POST /farms/F999/pastures
    { "name": "Potrero", ... }
  Then retorna HTTP 404 Not Found:
    { "error": "Finca F999 no encontrada" }
```

### AC#11: Manejo de Error - Nombre Duplicado (Opcional)
```gherkin
Scenario: Mismo nombre de potrero en la misma finca
  Given finca F001 ya tiene potrero con name="Potrero 1"
  When se envía:
    POST /farms/F001/pastures
    { "name": "Potrero 1", ... }
  Then:
    # Opción A: Permitir (puede haber homónimos en distintos sectores)
    [ ] Se crea potrero exitosamente con ID diferente
    # Opción B: Rechazar
    [ ] Retorna HTTP 409 Conflict:
        { "error": "Ya existe potrero con nombre 'Potrero 1'" }
  
  # Recomendación: Opción A (permitir homónimos)
```

### AC#12: Response Completo
```gherkin
Scenario: Response JSON contiene todos los campos
  Given se crea potrero exitosamente
  When retorna HTTP 201
  Then response body contiene:
    {
      "id": "P0004",
      "name": "Potrero Nuevo",
      "species": "KIKUYO",
      "areaHa": 1.5,
      "notes": "Potrero recién establecido",
      "status": "DISPONIBLE",
      "eta": 0,
      "etaMessage": "Disponible para uso",
      "currentHeightCm": 0,
      "lastUseAt": null,
      "lastClosedAt": null,
      "residualHeightCm": null,
      "createdAt": "2025-12-09T10:30:45Z",
      "updatedAt": "2025-12-09T10:30:45Z"
    }
```

### AC#13: Auditoría - Registrar Creación
```gherkin
Scenario: Registrar creación de potrero (futuro: HU#13)
  Given usuario admin@farm.com crea potrero
  When POST se procesa exitosamente
  Then se registra en TABLE_AUDIT_LOGS:
    | timestamp | 2025-12-09T10:30:45Z |
    | user | admin@farm.com |
    | operation | CREATE |
    | resourceType | PASTURE |
    | resourceId | P0004 |
    | details | { name: "Potrero Nuevo", species: "KIKUYO" } |
```

### AC#14: Garantía de Unicidad del ID
```gherkin
Scenario: Dos requests simultáneos generan IDs diferentes
  Given finca F001, último potrero es P0003
  When se envían dos POST simultáneamente
  Then:
    [ ] Request 1 recibe ID: P0004
    [ ] Request 2 recibe ID: P0005
    [ ] Ambos créanse exitosamente
    [ ] No hay duplicados en BD
    # Usar DynamoDB atomic counter o locks si es necesario
```

---

## 📊 **Especificación Técnica**

### Estructura de DTOs

#### Entrada: `PastureCreateRequest.java` (NUEVO)
```java
@Data
@Builder
public class PastureCreateRequest {
    
    @NotBlank(message = "Nombre es requerido")
    @Size(min = 1, max = 100, message = "Nombre debe tener 1-100 caracteres")
    private String name;
    
    @NotBlank(message = "Especie es requerida")
    @ValidSpecies(message = "Especie no válida o sin plan de rotación")
    private String species;
    
    @NotNull(message = "Área es requerida")
    @DecimalMin(value = "0.01", message = "Área debe ser mayor a 0")
    @DecimalMax(value = "100", message = "Área no puede ser mayor a 100 hectáreas")
    private Double areaHa;
    
    @Size(max = 500, message = "Notas no pueden exceder 500 caracteres")
    private String notes;
    
    // Validaciones personalizadas
    @AssertTrue(message = "Datos inválidos")
    private boolean isValid() {
        return name != null && species != null && areaHa != null;
    }
}
```

#### Salida: `PastureDTO.java` (existente, sin cambios)
```java
@Data
@Builder
public class PastureDTO {
    private String id;
    private String name;
    private String species;
    private Double areaHa;
    private String notes;
    private String status;        // DISPONIBLE
    private Integer eta;          // 0
    private String etaMessage;    // "Disponible para uso"
    private Integer currentHeightCm;
    private String lastUseAt;
    private String lastClosedAt;
    private Integer residualHeightCm;
    private String createdAt;
    private String updatedAt;
}
```

### Endpoints Definidos

#### **POST /farms/{farmId}/pastures**

**Path Parameters**:
- `farmId` (String, required): ID de finca (ej: F001)

**Request Body**:
```json
{
  "name": "Potrero Nuevo",
  "species": "KIKUYO",
  "areaHa": 1.5,
  "notes": "Potrero recién establecido"
}
```

**Campos opcionales**: `notes`

**Response 201 Created**:
```json
{
  "id": "P0004",
  "name": "Potrero Nuevo",
  "species": "KIKUYO",
  "areaHa": 1.5,
  "notes": "Potrero recién establecido",
  "status": "DISPONIBLE",
  "eta": 0,
  "etaMessage": "Disponible para uso",
  "currentHeightCm": 0,
  "lastUseAt": null,
  "lastClosedAt": null,
  "residualHeightCm": null,
  "createdAt": "2025-12-09T10:30:45Z",
  "updatedAt": "2025-12-09T10:30:45Z"
}
```

**Response 400 Bad Request**:
```json
{
  "timestamp": "2025-12-09T10:30:45Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Especie 'ESPECIE_DESCONOCIDA' no tiene plan de rotación",
  "path": "/farms/F001/pastures"
}
```

**Response 404 Not Found**:
```json
{
  "timestamp": "2025-12-09T10:30:45Z",
  "status": 404,
  "error": "Not Found",
  "message": "Finca F001 no encontrada",
  "path": "/farms/F001/pastures"
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`PastureCreateRequest.java`** (DTO entrada)
   - Campos: name, species, areaHa, notes
   - Validaciones: @NotBlank, @Size, @DecimalMin/Max
   - Validator personalizado para species

2. **`PastureCreateController.java`** (NUEVO)
   - Endpoint POST /farms/{farmId}/pastures
   - Mapear request a DTO
   - Delegar a servicio

3. **`PastureCreateProcessor.java`** (NUEVO)
   - Orquestar creación
   - Generar ID único
   - Mapear resultado a DTO

4. **`PastureIdGenerator.java`** (NUEVO o reutilizar)
   - Generar IDs únicos (P0001, P0002, etc.)
   - Usar DynamoDB counter o similar

### Archivos a Modificar

1. **`PastureService.java`**
   - Agregar método: `createPasture(String farmId, PastureCreateRequest request)`
   - Buscar plan para validar especie
   - Generar ID único
   - Calcular valores iniciales
   - Guardar en BD

2. **`PastureRepository.java`**
   - Asegurar método `save(Pasture pasture)` existe
   - Lógica de guardado con PutItem

3. **`PastureDTO.java`** (opcional)
   - Asegurar todos los campos están presentes

4. **`Pasture.java`** (Entity)
   - Asegurar que tiene campos: name, species, areaHa, notes, status, eta, createdAt, updatedAt

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear PastureCreateRequest
```java
@Data
@Builder
public class PastureCreateRequest {
    
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;
    
    @NotBlank
    @ValidSpecies
    private String species;
    
    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100")
    private Double areaHa;
    
    @Size(max = 500)
    private String notes;
}
```

### Paso 2: Crear PastureIdGenerator
```java
@Component
public class PastureIdGenerator {
    
    private final PastureRepository repository;
    
    public String generateNextId(String farmId) {
        // Obtener último ID usado para esta finca
        // Format: P0001, P0002, etc.
        // Si no hay potreros: P0001
        // Si hay P0003: siguiente es P0004
        
        int maxNumber = repository.findMaxIdNumberForFarm(farmId).orElse(0);
        return String.format("P%04d", maxNumber + 1);
    }
}
```

### Paso 3: Crear PastureCreateController
```java
@RestController
@RequestMapping("/farms/{farmId}/pastures")
public class PastureCreateController {
    
    private final PastureCreateProcessor processor;
    
    @PostMapping
    public ResponseEntity<PastureDTO> createPasture(
        @PathVariable String farmId,
        @Valid @RequestBody PastureCreateRequest request) {
        
        PastureDTO result = processor.createPasture(farmId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(result);
    }
}
```

### Paso 4: Crear PastureCreateProcessor
```java
@Component
public class PastureCreateProcessor {
    
    private final PastureService service;
    private final PastureMapper mapper;
    
    public PastureDTO createPasture(String farmId, PastureCreateRequest request) {
        try {
            Pasture pasture = service.createPasture(farmId, request);
            return mapper.toDTO(pasture);
        } catch (RepositoryException e) {
            throw new ProcessingException("Failed to create pasture", e);
        }
    }
}
```

### Paso 5: Agregar createPasture() a PastureService
```java
public Pasture createPasture(String farmId, PastureCreateRequest request) {
    // 1. Validar que finca existe
    Farm farm = farmRepository.findById(farmId)
        .orElseThrow(() -> new NotFoundException("Finca no encontrada"));
    
    // 2. Validar que plan existe para la especie
    Plan plan = planRepository.findByFarmAndSpecies(farmId, request.getSpecies())
        .orElseThrow(() -> new IllegalArgumentException(
            "Especie no tiene plan de rotación"));
    
    // 3. Generar ID único
    String pastureId = idGenerator.generateNextId(farmId);
    
    // 4. Crear entidad Pasture con valores iniciales
    Pasture pasture = Pasture.builder()
        .id(pastureId)
        .farmId(farmId)
        .name(request.getName())
        .species(request.getSpecies())
        .areaHa(request.getAreaHa())
        .notes(request.getNotes())
        .status(PastureStatus.DISPONIBLE)
        .eta(0)
        .etaMessage("Disponible para uso")
        .currentHeightCm(0)  // o plan.getMinHeightRequired()
        .lastUseAt(null)
        .lastClosedAt(null)
        .residualHeightCm(null)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
    
    // 5. Guardar en BD
    repository.save(pasture);
    
    // 6. Retornar creado
    return pasture;
}
```

---

## 🧪 **Casos de Prueba**

### Test Unitarios (JUnit 5)

```java
@ExtendWith(MockitoExtension.class)
class PastureCreateProcessorTest {
    
    @Mock
    private PastureService service;
    
    @Mock
    private PastureMapper mapper;
    
    @InjectMocks
    private PastureCreateProcessor processor;
    
    @Test
    @DisplayName("debe crear potrero con datos válidos")
    void testCreatePastureSuccess() {
        // Arrange
        Pasture pasture = PastureBuilder.buildKikuyo();
        PastureCreateRequest request = PastureCreateRequest.builder()
            .name("Potrero Nuevo")
            .species("KIKUYO")
            .areaHa(1.5)
            .build();
        
        when(service.createPasture(any(), any())).thenReturn(pasture);
        when(mapper.toDTO(pasture)).thenReturn(createDTO());
        
        // Act
        PastureDTO result = processor.createPasture("F001", request);
        
        // Assert
        assertEquals("Potrero Nuevo", result.getName());
        assertEquals("KIKUYO", result.getSpecies());
        verify(service).createPasture("F001", request);
    }
    
    @Test
    @DisplayName("debe rechazar nombre vacío")
    void testRejectEmptyName() {
        PastureCreateRequest request = new PastureCreateRequest();
        request.setName("");
        
        assertThrows(ConstraintViolationException.class, () -> {
            processor.createPasture("F001", request);
        });
    }
    
    @Test
    @DisplayName("debe generar ID único")
    void testGenerateUniqueId() {
        // Arrange
        when(service.createPasture(any(), any()))
            .thenReturn(Pasture.builder().id("P0004").build());
        
        // Act
        processor.createPasture("F001", createValidRequest());
        
        // Assert
        // Verificar que ID generado es P0004
    }
}
```

### Test Integración (Spring Boot Test)

```java
@SpringBootTest
class PastureCreateControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PastureCreateProcessor processor;
    
    @Test
    @DisplayName("POST /pastures retorna 201 Created")
    void testCreatePastureSuccess() throws Exception {
        PastureDTO mockResponse = createMockDTO();
        PastureCreateRequest request = PastureCreateRequest.builder()
            .name("Potrero Nuevo")
            .species("KIKUYO")
            .areaHa(1.5)
            .build();
        
        when(processor.createPasture(any(), any())).thenReturn(mockResponse);
        
        mockMvc.perform(post("/farms/F001/pastures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("P0004"))
            .andExpect(jsonPath("$.status").value("DISPONIBLE"));
    }
    
    @Test
    @DisplayName("POST con payload inválido retorna 400")
    void testCreatePastureInvalidPayload() throws Exception {
        String invalidPayload = "{\"name\": \"\", \"areaHa\": -5}";
        
        mockMvc.perform(post("/farms/F001/pastures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest());
    }
}
```

### Test E2E (Cypress)

```javascript
describe('Crear Potrero - POST Endpoint', () => {
  
  beforeEach(() => {
    cy.login('admin@farm.com');
  });
  
  it('debe crear potrero exitosamente', () => {
    cy.intercept('POST', '/farms/*/pastures', {
      statusCode: 201,
      body: {
        id: 'P0004',
        name: 'Potrero Nuevo',
        status: 'DISPONIBLE',
        eta: 0
      }
    }).as('createPasture');
    
    cy.request('POST', '/farms/F001/pastures', {
      name: 'Potrero Nuevo',
      species: 'KIKUYO',
      areaHa: 1.5
    }).then((response) => {
      expect(response.status).to.equal(201);
      expect(response.body.id).to.equal('P0004');
      expect(response.body.status).to.equal('DISPONIBLE');
    });
  });
  
  it('debe rechazar especie inválida', () => {
    cy.request({
      method: 'POST',
      url: '/farms/F001/pastures',
      body: {
        name: 'Potrero',
        species: 'ESPECIE_DESCONOCIDA',
        areaHa: 1.0
      },
      failOnStatusCode: false
    }).then((response) => {
      expect(response.status).to.equal(400);
      expect(response.body.message).to.contain('plan de rotación');
    });
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD con Gherkin)**

### Escenario 1: Crear Potrero Nuevo Exitosamente
```gherkin
Scenario: Crear potrero con datos válidos
  Given existe finca F001
  And existe plan para especie KIKUYO
  When se envía:
    POST /farms/F001/pastures
    {
      "name": "Potrero Nuevo",
      "species": "KIKUYO",
      "areaHa": 1.5
    }
  Then retorna HTTP 201 Created
  And respuesta contiene:
    | id | P0004 |
    | name | Potrero Nuevo |
    | status | DISPONIBLE |
    | eta | 0 |
```

### Escenario 2: Validar Especie
```gherkin
Scenario: Rechazar especie sin plan
  Given no existe plan para ESPECIE_DESCONOCIDA
  When se envía especie inválida
  Then retorna HTTP 400:
    "Especie 'ESPECIE_DESCONOCIDA' no tiene plan de rotación"
```

### Escenario 3: Generar ID Único
```gherkin
Scenario: ID se genera secuencial
  Given finca F001 tiene potreros: P0001, P0002, P0003
  When se crea nuevo potrero
  Then recibe ID: P0004
  And no existe duplicado
```

### Escenario 4: Validación de Área
```gherkin
Scenario: Rechazar área inválida
  When se envía areaHa = 0
  Then retorna HTTP 400: "mayor a 0"
  
  When se envía areaHa = 150
  Then retorna HTTP 400: "no puede ser mayor a 100"
```

### Escenario 5: Estado Inicial
```gherkin
Scenario: Potrero creado en estado DISPONIBLE
  When se crea nuevo potrero
  Then:
    [ ] status: DISPONIBLE
    [ ] eta: 0
    [ ] etaMessage: "Disponible para uso"
    [ ] lastUseAt: null
    [ ] currentHeightCm: 0
```

---

## 📚 **Referencias y Dependencias**

**Dependencias de otros componentes**:
- ✅ `PlanRepository` (completado)
- ✅ `FarmRepository` (completado)
- ✅ `PastureRepository` (completado)
- ✅ Validador `@ValidSpecies` (HU#4)

**Documentación relacionada**:
- [HU#4: Backend PUT Editar Potrero](./PASTURES-HU-004-put-editar.md)
- [HU#1: Backend POST Eventos](../P0/PASTURES-HU-001-post-eventos.md)
- [Pastures Overview](../../pastures/pastures-overview.md)
- [Architecture Index](../../architecture/index.md)
- [Flujo Registro Bovino - ID Generation](../../architecture/flujo-registro-bovino.md)

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Layered Architecture + TABLE_COUNTERS Atomic ID Generation (reutiliza patrón bovinos)

**Justificación:** **Consistencia con bovinos**: Patrón TABLE_COUNTERS ya probado, garantiza IDs únicos y secuenciales incluso con requests concurrentes. **Atomicidad**: DynamoDB UpdateItem con expresión ADD (if_not_exists + incremento) es operación atómica - NO hay race conditions. **Reutilización máxima**: CountersRepository ya existe, solo necesita configurar para "PASTURE" en lugar de "BOVINE". **Escalabilidad**: Funciona para any throughput, sin coordinación externa. **Validación técnica completada**: CountersRepository verificado en bovinos, TABLE_COUNTERS estructura verificada, patrón probado en producción.

**Estrategia de Generación de IDs:**
- **Tabla**: TABLE_COUNTERS (existente, reutilizada de bovinos)
- **Key**: PK = "COUNTER#TABLE_PASTURE" (similar a "COUNTER#TABLE_FARM_BOVINES")
- **Operación**: `UpdateItem` con expresión `SET nextId = if_not_exists(nextId, :start) + :inc`
- **Resultado**: Retorna nuevo ID (ej: 4 → próximo potrero es P0004)
- **Format**: P{NNNN} (P0001, P0002, ..., P9999)
- **Atomicidad**: Garantizada por DynamoDB - no hay duplicados incluso con concurrencia

**Componentes Afectados:**

- **PastureCreateRequest.java (Nuevo - DTO entrada):** Campos: `name` (String, 1-100), `species` (String, requerido), `areaHa` (Double, 0.01-100), `notes` (String, 0-500, opcional). Validaciones: `@NotBlank`, `@Size`, `@DecimalMin/@DecimalMax`, `@ValidSpecies`. Todo requerido excepto notes.

- **PastureCreateController.java (Nuevo):** Endpoint POST. `@RequestMapping("/farms/{farmId}/pastures")`, método `@PostMapping`. Retorna HTTP 201 Created + Location header. Valida DTO automático. Delega a processor.

- **PastureCreateProcessor.java (Nuevo - Orquestador):** Orquesta creación. Recibe farmId, PastureCreateRequest. Delega a `PastureService.createPasture()`. Mapea Pasture → PastureDTO. Manejo de excepciones estándar.

- **PastureService (Modificación - Agregar método):** Nuevo método `createPasture(String farmId, PastureCreateRequest request)`. Valida finca existe (404). Valida plan para especie (400). **Usa `CountersRepository.getNextId("TABLE_PASTURE")`** para generar ID. Construye Pasture con valores iniciales. Llama `PastureRepository.save()`. Retorna creado.

- **CountersRepository (Verificación - Reutilizar):** Método `getNextId(String entityName)` ya existe ✅. Configurar para "TABLE_PASTURE" + formato P{NNNN}.

- **PastureRepository (Verificación):** Método `save(Pasture pasture)` debe existir ✅.

- **Pasture Entity (Verificación):** Campos necesarios ya existen ✅.

- **PastureDTO (Verificación):** Todos los campos ✅.

**Hitos de Implementación:**

1. **PastureCreateRequest.java** - DTO validador (depende: @ValidSpecies)
2. **PastureCreateController.java** - Endpoint HTTP (depende: PastureCreateProcessor)
3. **PastureCreateProcessor.java** - Orquestador (depende: PastureService)
4. **PastureService.createPasture()** - Agregar método (depende: CountersRepository, PastureRepository)
5. **Tests** - Unitarios, integración, E2E

### Validación de Impacto

**Hallazgos de validación técnica:**

✅ **TABLE_COUNTERS - Patrón Probado (Bovinos):**
- `CountersRepository.getNextId(entityName)` ya existe en bovinos ✅
- Operación: `SET nextId = if_not_exists(nextId, :start) + :inc` ✅
- Atómica: Garantiza NO duplicados incluso con concurrencia ✅
- Production-ready: Usado en sistema real de bovinos ✅

✅ **Generación de IDs - Estrategia:**
```
Tabla: TABLE_COUNTERS
PK: COUNTER#TABLE_PASTURE
SK: CURRENT (implícito o field "nextId")

Operación atómica:
1. Cliente: CountersRepository.getNextId("TABLE_PASTURE")
2. DynamoDB: UpdateItem (ADD nextId :inc, return UPDATED_NEW)
3. Retorna: 4 (nuevo contador)
4. Cliente: Format → P0004

Atomicidad garantizada por DynamoDB:
- Dos requests simultáneos → DynamoDB serializa internamente
- Request 1 obtiene nextId=4 → P0004
- Request 2 obtiene nextId=5 → P0005
- NO hay duplicados, garantizado
```

✅ **Impacto en Performance:**
- Generación de ID: Una operación UpdateItem a TABLE_COUNTERS (fast)
- Creación de potrero: Una operación PutItem a TABLE_PASTURE (fast)
- Total: 2 operaciones DynamoDB, sin latencia significativa

✅ **Seguridad:**
- IDs secuenciales, auditables
- Imposible predecir ID futuro (seguridad por oscuridad no - pero OK)
- Integridad garantizada por atomicidad DynamoDB

✅ **Testing:**
- Validadores: Funciones puras, 100% coverage
- Service: Mock CountersRepository, mock repositories
- Controller: MockMvc estándar
- E2E: Direct HTTP requests

✅ **Flujo Completo Verificado:**
```
POST /farms/{farmId}/pastures
→ PastureCreateController.createPasture()
  → Validar DTO (Spring automático)
  → PastureCreateProcessor.createPasture()
    → PastureService.createPasture()
      → Buscar farm (404 si no existe)
      → Buscar plan para species (400 si no existe)
      → CountersRepository.getNextId("TABLE_PASTURE")
         └─ DynamoDB: UpdateItem ADD nextId :inc → retorna 4
      → Construir Pasture: id="P0004", status=DISPONIBLE, eta=0
      → PastureRepository.save() → DynamoDB PutItem
      → Retornar creado
    → PasturesMapper.toDTO()
  ← HTTP 201 Created + Location: /farms/F001/pastures/P0004
```

✅ **Riesgos Mitigables:**
- Nombre vacío → `@NotBlank`
- Especie inválida → `@ValidSpecies`
- Área inválida → `@DecimalMin/@DecimalMax`
- Finca no existe → 404 en service
- **ID duplicado → NO posible** (DynamoDB atomic UpdateItem)
- Notes muy largo → `@Size(max=500)`
- Valores iniciales inconsistentes → Constructor con defaults

### Notas Técnicas

**TABLE_COUNTERS - Estructura (Reutilizada de Bovinos):**
```java
// Tabla: TABLE_COUNTERS
// PK: COUNTER#{entityName}  (ej: COUNTER#TABLE_PASTURE, COUNTER#TABLE_FARM_BOVINES)
// Attribute: nextId (número, comienza en 0)

// Ejemplo de registros:
{
  "COUNTER#TABLE_FARM_BOVINES": { "nextId": 127 },
  "COUNTER#TABLE_PASTURE": { "nextId": 5 }
}
```

**CountersRepository - Uso Reutilizado:**
```java
// Código IGUAL al de bovinos, solo cambiar entityName
String nextId = counterRepository.getNextId("TABLE_PASTURE");
// nextId = "4"

String pastureId = String.format("P%04d", Integer.parseInt(nextId));
// pastureId = "P0004"
```

**Operación Atómica - UpdateItem Expression:**
```java
// DynamoDB expression:
// SET nextId = if_not_exists(nextId, :start) + :inc
// 
// Garantías:
// - Si nextId no existe: inicializa en 0, suma 1 → retorna 1
// - Si nextId existe: suma 1 → retorna nuevo valor
// - Operación es ATÓMICA (transaction-like)
// - Dos requests simultáneos NO pueden obtener el mismo ID
```

**Valores Iniciales - Potrero Nuevo (Mismos que PUT):**
```java
Pasture pasture = Pasture.builder()
    .id(pastureId)           // "P0004" (de CountersRepository)
    .farmId(farmId)
    .name(request.getName())
    .species(request.getSpecies())
    .areaHa(request.getAreaHa())
    .notes(request.getNotes())
    .status(PastureStatus.DISPONIBLE)
    .eta(0)
    .etaMessage("Disponible para uso")
    .currentHeightCm(0)
    .lastUseAt(null)
    .lastClosedAt(null)
    .residualHeightCm(null)
    .createdAt(Instant.now())
    .updatedAt(Instant.now())
    .build();
```

**Auditoría Preparada (HU#13):**
- Campo `createdAt` registra cuándo se creó
- User desde JWT/Security context disponible
- Estructura lista para tabla de auditoría

### Referencias y Validación

**Documentación Consultada:**
- [flujo-registro-bovino.md](../../architecture/flujo-registro-bovino.md) - Patrón TABLE_COUNTERS verificado ✅
- [CountersRepository.java](../../../src/main/java/com/cattle/repository/CountersRepository.java) - Implementación existente ✅
- [BovinesService.java](../../../src/main/java/com/cattle/services/BovinesService.java) - Uso del patrón ✅
- [PASTURES-HU-004](./PASTURES-HU-004-put-editar.md) - Patrón base

**Historias Relacionadas:**
- ✅ PASTURES-HU-004: Backend PUT Editar (patrón base)
- → PASTURES-HU-005: Backend POST Crear (esta - reutiliza TABLE_COUNTERS de bovinos)
- → PASTURES-HU-013: Auditoría (usará createdAt)

**Patrón Utilizado:** TABLE_COUNTERS + DynamoDB Atomic UpdateItem - Production-ready en bovinos

**Stack Tecnológico Verificado:**
- DynamoDB TABLE_COUNTERS - Existente, probado
- CountersRepository - Existente, probado en bovinos
- Mismo patrón que bovinos - Consistencia arquitectónica

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Reutilización de patrón TABLE_COUNTERS probado en bovinos (atomicidad + sin duplicados garantizado)

---

## 🔧 **Refinamiento Técnico**

### API Contract

**Endpoint:**
```
POST /farms/{farmId}/pastures
Content-Type: application/json
Authorization: Bearer {jwt}
```

**Request Body:**
```json
{
  "name": "Potrero Nuevo",
  "species": "KIKUYO",
  "areHa": 5.5,
  "animalLoad": 20
}
```

**Response (201 Created):**
```json
{
  "id": "P000001",
  "farmId": "F001",
  "name": "Potrero Nuevo",
  "createdAt": "2026-01-09T10:30:45Z",
  "status": "DISPONIBLE"
}
```

### TABLE_COUNTERS - ID Generation

```pseudocode
POST /farms/{farmId}/pastures

// 1. Generar ID único (TABLE_COUNTERS)
counter = counterRepository.increment("PASTURE_ID_COUNTER")
pastureId = "P" + pad(counter, 6)  // P000001, P000002

// 2. Crear y guardar
pasture.id = pastureId
pasture.createdAt = now()
pasture.status = "DISPONIBLE"
pastureRepository.save(pasture)

// 3. Retornar 201 Created
RETURN 201, pasture
```

### DynamoDB Atomic UpdateItem

```java
UpdateItem(
  Key: {PK: "COUNTER#PASTURE_ID"},
  UpdateExpression: "SET #v = if_not_exists(#v, :zero) + :inc",
  ReturnValues: "ALL_NEW"
)
```

### Testing Strategy

**Tests Críticos:**
- POST crea pasture con ID único
- Counter incrementa (P000001, P000002, ...)
- 201 retorna entidad completa
- Parallel POST generan IDs únicos sin duplicados

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `PastureCreateRequest.java` con validaciones completas
- [ ] `PastureCreateController.java` implementado
- [ ] `PastureCreateProcessor.java` orquesta creación correctamente
- [ ] `PastureIdGenerator.java` genera IDs únicos secuenciales
- [ ] `PastureService.createPasture()` implementado
- [ ] Validación de especie con plan funciona
- [ ] Validación de área (0.01-100) funciona
- [ ] Nombre validado (1-100 caracteres)
- [ ] Estado inicial DISPONIBLE con eta=0
- [ ] Tests unitarios: cobertura >= 85%
- [ ] Tests integración: todos pasan
- [ ] Manual testing en Postman/Insomnia: crear con datos válidos
- [ ] Validaciones de error probadas (400, 404)
- [ ] ID generado es único y secuencial
- [ ] Respuesta 201 contiene potrero completo
- [ ] createdAt y updatedAt registrados correctamente
- [ ] Documentación actualizada (JSDoc/JavaDoc)
- [ ] Code review aprobado (2 approvals)
- [ ] Sin warnings de linting
- [ ] CI/CD green (build, test, coverage)
- [ ] Demostrable en staging environment
- [ ] Auditoría preparada (estructura para HU#13)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
