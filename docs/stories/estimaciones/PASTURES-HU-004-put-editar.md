# 🌱 PASTURES-HU#4: Backend: PUT para Editar Potrero

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟠 ALTO (P1) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Propuesta aprobada | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend developer**, quiero crear un endpoint PUT que permita editar atributos de un potrero (nombre, especie, área, notas), de tal forma que:

1. El endpoint valida los datos modificados
2. Solo permite editar campos permitidos (no status, no ETA)
3. Actualiza el potrero en DynamoDB
4. Retorna el potrero actualizado
5. Mantiene la integridad de datos históricos

Esto habilitará que operarios corrijan información de potreros (nombre incorrecto, área mal medida, etc.) sin afectar el estado de rotación.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Endpoint PUT Existe y Responde
```gherkin
Scenario: Crear endpoint PUT /farms/{farmId}/pastures/{pastureId}
  Given el backend está corriendo
  When se envía PUT a /farms/F001/pastures/P001
  And el payload contiene: { "name": "Potrero Nuevo", "areaHa": 2.0 }
  Then el endpoint retorna HTTP 200 OK
  And la respuesta incluye el potrero actualizado en JSON
```

### AC#2: Editar Nombre del Potrero
```gherkin
Scenario: Actualizar nombre de potrero exitosamente
  Given existe potrero P001 con name="Potrero 1"
  When se envía:
    PUT /farms/F001/pastures/P001
    {
      "name": "Potrero Nueva Designación"
    }
  Then el backend:
    [ ] Valida que name no esté vacío
    [ ] Valida que name tenga máximo 100 caracteres
    [ ] Busca potrero en TABLE_PASTURE
    [ ] Actualiza campo name
    [ ] Guarda cambios en DynamoDB
    [ ] Retorna HTTP 200 con potrero actualizado
  
  And respuesta JSON contiene:
    {
      "id": "P001",
      "name": "Potrero Nueva Designación",
      "species": "KIKUYO",
      ...
    }
```

### AC#3: Editar Área del Potrero
```gherkin
Scenario: Actualizar área (hectáreas) exitosamente
  Given existe potrero P001 con areaHa=0.8
  When se envía:
    PUT /farms/F001/pastures/P001
    {
      "areaHa": 2.5
    }
  Then el backend:
    [ ] Valida que areaHa > 0
    [ ] Valida que areaHa <= 100 (máximo razonable)
    [ ] Actualiza campo areaHa
    [ ] Guarda cambios
    [ ] Retorna HTTP 200 con:
        "areaHa": 2.5
```

### AC#4: Editar Especie del Potrero
```gherkin
Scenario: Cambiar especie de potrero (con validación de plan)
  Given existe potrero P001 con species="KIKUYO"
  And existe plan para "BRACHIARIA" en TABLE_PLAN
  When se envía:
    PUT /farms/F001/pastures/P001
    {
      "species": "BRACHIARIA"
    }
  Then el backend:
    [ ] Valida que species sea válida (KIKUYO, BRACHIARIA, RYEGRASS, etc.)
    [ ] Verifica que exista plan para "BRACHIARIA"
    [ ] Actualiza campo species
    [ ] Guarda cambios
    [ ] Retorna HTTP 200
```

### AC#5: Editar Notas del Potrero
```gherkin
Scenario: Actualizar notas/observaciones
  Given existe potrero P001 con notes=null
  When se envía:
    PUT /farms/F001/pastures/P001
    {
      "notes": "Potrero con drenaje mejorado, necesita vigilancia en época lluviosa"
    }
  Then el backend:
    [ ] Valida que notes tenga máximo 500 caracteres
    [ ] Actualiza campo notes
    [ ] Guarda cambios
    [ ] Retorna HTTP 200 con:
        "notes": "Potrero con drenaje mejorado..."
  
  When usuario envía notes vacío (""):
    [ ] Interpreta como eliminar notas (null)
    [ ] Retorna notes: null
```

### AC#6: Editar Múltiples Campos Simultáneamente
```gherkin
Scenario: Actualizar varios campos en una sola petición
  Given existe potrero P001 con:
    | Campo | Valor Antes |
    | name | Potrero 1 |
    | areaHa | 0.8 |
    | notes | ninguno |
  
  When se envía:
    PUT /farms/F001/pastures/P001
    {
      "name": "Potrero Mejorado",
      "areaHa": 1.5,
      "notes": "Área expandida"
    }
  
  Then todos los campos se actualizan:
    | Campo | Valor Después |
    | name | Potrero Mejorado |
    | areaHa | 1.5 |
    | notes | Área expandida |
  
  And respuesta retorna potrero con todos los cambios
```

### AC#7: Validación - Campos No Editables
```gherkin
Scenario: Rechazar cambios a campos no editables
  Given usuario intenta modificar campos protegidos
  
  When se envía:
    PUT /farms/F001/pastures/P001
    {
      "status": "MANTENIMIENTO",
      "eta": 10,
      "lastUseAt": "2025-12-01T10:30:00Z"
    }
  
  Then el backend:
    [ ] Ignora campos no permitidos (status, eta, lastUseAt, etc.)
    [ ] Retorna HTTP 200 con potrero sin cambios en esos campos
    [ ] Sin error (silenciosamente ignora)
```

### AC#8: Validación - Nombre Requerido y No Vacío
```gherkin
Scenario: Rechazar nombre vacío o solo espacios
  When se envía:
    PUT /farms/F001/pastures/P001
    {
      "name": ""
    }
  Then retorna HTTP 400 Bad Request:
    { "error": "Nombre no puede estar vacío" }
  
  When se envía:
    {
      "name": "   "  // solo espacios
    }
  Then retorna HTTP 400:
    { "error": "Nombre no puede estar vacío" }
```

### AC#9: Validación - Área Válida
```gherkin
Scenario: Rechazar área inválida
  When se envía area <= 0:
    { "areaHa": 0 }
  Then retorna HTTP 400:
    { "error": "Área debe ser mayor a 0" }
  
  When se envía area > 100:
    { "areaHa": 150 }
  Then retorna HTTP 400:
    { "error": "Área no puede ser mayor a 100 hectáreas" }
  
  When se envía area no numérica:
    { "areaHa": "dos punto cinco" }
  Then retorna HTTP 400:
    { "error": "Área debe ser un número válido" }
```

### AC#10: Validación - Especie Válida
```gherkin
Scenario: Rechazar especie no reconocida
  Given no existe plan para "ESPECIE_DESCONOCIDA"
  
  When se envía:
    { "species": "ESPECIE_DESCONOCIDA" }
  Then retorna HTTP 400:
    { "error": "Especie no reconocida o sin plan de rotación" }
```

### AC#11: Manejo de Error - Potrero No Existe
```gherkin
Scenario: Intenta editar potrero inexistente
  When se envía:
    PUT /farms/F001/pastures/NONEXISTENT
    { "name": "Nuevo nombre" }
  Then retorna HTTP 404 Not Found:
    { "error": "Potrero NONEXISTENT no encontrado" }
```

### AC#12: Manejo de Error - Conflicto Optimista (si usa versionamiento)
```gherkin
Scenario: Conflicto de versión en edición simultánea (opcional)
  Given dos usuarios editan el mismo potrero simultáneamente
  And versión en servidor = 3
  And usuario A envía con version=2
  And usuario B envía con version=2
  
  When usuario A PUT exitoso:
  Then servidor actualiza, versión pasa a 4
  
  When usuario B intenta PUT con version=2:
  Then retorna HTTP 409 Conflict:
    {
      "error": "Potrero fue modificado por otro usuario",
      "currentVersion": 4,
      "yourVersion": 2
    }
  
  # NOTA: Depende si se implementa versionamiento optimista
```

### AC#13: Auditoría - Registrar Cambios
```gherkin
Scenario: Registrar quién y cuándo editó (futuro: HU#13)
  Given usuario juan.perez@farm.com edita potrero
  When PUT se procesa exitosamente
  Then se registra en TABLE_AUDIT_LOGS (o tabla de auditoría):
    | Campo | Valor |
    | timestamp | 2025-12-09T10:30:45Z |
    | user | juan.perez@farm.com |
    | operation | PUT |
    | pastureId | P001 |
    | changes | { name: "Potrero 1" → "Potrero Nuevo" } |
```

### AC#14: No Afectar Estado de Rotación
```gherkin
Scenario: Editar potrero no recalcula ETA automáticamente
  Given potrero P001:
    | Status | EN_DESCANSO |
    | ETA | 22 días |
  
  When se envía:
    PUT /farms/F001/pastures/P001
    {
      "name": "Nombre Nuevo"
    }
  
  Then potrero se actualiza:
    [ ] name: "Nombre Nuevo"
    [ ] status: EN_DESCANSO (sin cambios)
    [ ] ETA: 22 días (sin cambios)
    [ ] lastUseAt: sin cambios
    [ ] lastClosedAt: sin cambios
```

---

## 📊 **Especificación Técnica**

### Estructura de DTOs

#### Entrada: `PastureUpdateRequest.java` (NUEVO)
```java
@Data
@Builder
public class PastureUpdateRequest {
    
    @Size(min = 1, max = 100, message = "Nombre debe tener 1-100 caracteres")
    private String name;
    
    @DecimalMin(value = "0.01", message = "Área debe ser mayor a 0")
    @DecimalMax(value = "100", message = "Área no puede ser mayor a 100 hectáreas")
    private Double areaHa;
    
    @ValidSpecies(message = "Especie no válida o sin plan de rotación")
    private String species;
    
    @Size(max = 500, message = "Notas no pueden exceder 500 caracteres")
    private String notes;
    
    // Validación personalizada
    @AssertTrue(message = "Al menos un campo debe ser actualizado")
    private boolean isAtLeastOneFieldProvided() {
        return name != null || areaHa != null || species != null || notes != null;
    }
    
    // Constructor que ignora campos no permitidos (builder pattern)
    // Solo permitir: name, areaHa, species, notes
    // Ignorar: status, eta, lastUseAt, etc.
}
```

#### Salida: `PastureDTO.java` (existente, sin cambios)
```java
@Data
@Builder
public class PastureDTO {
    private String id;
    private String name;                // ← Actualizable
    private String species;              // ← Actualizable
    private Double areaHa;               // ← Actualizable
    private String notes;                // ← Actualizable
    private String status;               // ← NO editable
    private Integer eta;                 // ← NO editable
    private String lastUseAt;            // ← NO editable
    // ... otros campos sin cambios
}
```

### Endpoints Definidos

#### **PUT /farms/{farmId}/pastures/{pastureId}**

**Path Parameters**:
- `farmId` (String, required): ID de finca (ej: F001)
- `pastureId` (String, required): ID de potrero (ej: P001)

**Request Body**:
```json
{
  "name": "Potrero Mejorado",
  "areaHa": 2.5,
  "species": "BRACHIARIA",
  "notes": "Potrero con drenaje mejorado"
}
```

**Campos opcionales**: Cualquiera de los 4 campos puede ser omitido (editaría solo los proporcionados)

**Response 200 OK**:
```json
{
  "id": "P001",
  "name": "Potrero Mejorado",
  "species": "BRACHIARIA",
  "areaHa": 2.5,
  "notes": "Potrero con drenaje mejorado",
  "status": "EN_DESCANSO",
  "eta": 22,
  "etaMessage": "Disponible en 22 días",
  "lastUseAt": "2025-12-01T10:30:45Z",
  "lastClosedAt": "2025-12-08T14:15:00Z",
  "residualHeightCm": 8,
  "areaHa": 2.5,
  "createdAt": "2025-11-01T08:00:00Z",
  "updatedAt": "2025-12-09T10:30:45Z"
}
```

**Response 400 Bad Request**:
```json
{
  "timestamp": "2025-12-09T10:30:45Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Área debe ser mayor a 0",
  "path": "/farms/F001/pastures/P001"
}
```

**Response 404 Not Found**:
```json
{
  "timestamp": "2025-12-09T10:30:45Z",
  "status": 404,
  "error": "Not Found",
  "message": "Potrero P001 no encontrado",
  "path": "/farms/F001/pastures/P001"
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`PastureUpdateRequest.java`** (DTO entrada)
   - Campos: name, areaHa, species, notes
   - Validaciones: size, decimal range, custom @ValidSpecies
   - Validator personalizado para ignorar campos no permitidos

2. **`ValidSpecies.java`** (Validador personalizado, si no existe)
   - Valida que especie exista
   - Verifica que plan exista en TABLE_PLAN

3. **`PastureUpdateController.java`** (NUEVO)
   - Endpoint PUT /farms/{farmId}/pastures/{pastureId}
   - Mapear request a DTO
   - Delegar a servicio

4. **`PastureUpdateProcessor.java`** (NUEVO)
   - Orquestar actualización
   - Mapear resultado a DTO

### Archivos a Modificar

1. **`PastureService.java`**
   - Agregar método: `updatePasture(String farmId, String pastureId, PastureUpdateRequest request)`
   - Buscar potrero
   - Validar cambios (especialmente species)
   - Construir EntityPatch
   - Guardar cambios

2. **`PastureRepository.java`**
   - Asegurar método `update(String pk, EntityPatch patch)` existe
   - Lógica de guardado con UpdateItem

3. **`PastureDTO.java`** (opcional)
   - Agregar campo `updatedAt` si no existe
   - Mapper actualizado

4. **`Pasture.java`** (Entity)
   - Asegurar que tiene campos: name, species, areaHa, notes
   - Campo `updatedAt` para auditoría

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear PastureUpdateRequest
```java
@Data
@Builder
public class PastureUpdateRequest {
    
    @Size(min = 1, max = 100)
    private String name;
    
    @DecimalMin("0.01")
    @DecimalMax("100")
    private Double areaHa;
    
    private String species;
    
    @Size(max = 500)
    private String notes;
    
    @AssertTrue
    private boolean isValid() {
        return name != null || areaHa != null || species != null || notes != null;
    }
}
```

### Paso 2: Crear PastureUpdateController
```java
@RestController
@RequestMapping("/farms/{farmId}/pastures/{pastureId}")
public class PastureUpdateController {
    
    private final PastureUpdateProcessor processor;
    
    @PutMapping
    public ResponseEntity<PastureDTO> updatePasture(
        @PathVariable String farmId,
        @PathVariable String pastureId,
        @Valid @RequestBody PastureUpdateRequest request) {
        
        PastureDTO result = processor.updatePasture(farmId, pastureId, request);
        return ResponseEntity.ok(result);
    }
}
```

### Paso 3: Crear PastureUpdateProcessor
```java
@Component
public class PastureUpdateProcessor {
    
    private final PastureService service;
    private final PastureMapper mapper;
    
    public PastureDTO updatePasture(String farmId, String pastureId, PastureUpdateRequest request) {
        try {
            Pasture pasture = service.updatePasture(farmId, pastureId, request);
            return mapper.toDTO(pasture);
        } catch (RepositoryException e) {
            throw new ProcessingException("Failed to update pasture", e);
        }
    }
}
```

### Paso 4: Agregar Método a PastureService
```java
public Pasture updatePasture(String farmId, String pastureId, PastureUpdateRequest request) {
    // 1. Buscar potrero
    Pasture pasture = repository.findById("PASTURE#" + pastureId)
        .orElseThrow(() -> new NotFoundException("Potrero no encontrado"));
    
    // 2. Si species cambia, validar que existe plan
    if (request.getSpecies() != null && 
        !request.getSpecies().equals(pasture.getSpecies())) {
        Plan plan = planRepository.findByFarmAndSpecies(farmId, request.getSpecies())
            .orElseThrow(() -> new IllegalArgumentException("Plan no existe"));
    }
    
    // 3. Construir EntityPatch con campos a actualizar
    EntityPatch patch = EntityPatch.builder();
    
    if (request.getName() != null) {
        patch.addUpdate("name", request.getName());
    }
    if (request.getAreaHa() != null) {
        patch.addUpdate("areaHa", request.getAreaHa());
    }
    if (request.getSpecies() != null) {
        patch.addUpdate("species", request.getSpecies());
    }
    if (request.getNotes() != null) {
        patch.addUpdate("notes", request.getNotes().isEmpty() ? null : request.getNotes());
    }
    
    patch.addUpdate("updatedAt", Instant.now());
    
    // 4. Guardar
    repository.update(pasture.getPk(), patch.build());
    
    // 5. Retornar actualizado
    return repository.findById(pasture.getPk()).orElse(pasture);
}
```

### Paso 5: Agregar Validador @ValidSpecies (si no existe)
```java
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SpeciesValidator.class)
public @interface ValidSpecies {
    String message() default "Especie no válida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class SpeciesValidator implements ConstraintValidator<ValidSpecies, String> {
    
    @Autowired
    private PlanRepository planRepository;
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // field is optional
        
        // Validar que exista plan para esa especie
        return planRepository.existsBySpecies(value);
    }
}
```

---

## 🧪 **Casos de Prueba**

### Test Unitarios (JUnit 5)

```java
@ExtendWith(MockitoExtension.class)
class PastureUpdateProcessorTest {
    
    @Mock
    private PastureService service;
    
    @Mock
    private PastureMapper mapper;
    
    @InjectMocks
    private PastureUpdateProcessor processor;
    
    @Test
    @DisplayName("debe actualizar nombre del potrero")
    void testUpdateName() {
        // Arrange
        Pasture pasture = PastureBuilder.buildKikuyo();
        PastureUpdateRequest request = PastureUpdateRequest.builder()
            .name("Nuevo Nombre")
            .build();
        
        when(service.updatePasture(any(), any(), any())).thenReturn(pasture);
        when(mapper.toDTO(pasture)).thenReturn(createDTO());
        
        // Act
        PastureDTO result = processor.updatePasture("F001", "P001", request);
        
        // Assert
        assertEquals("Nuevo Nombre", result.getName());
        verify(service).updatePasture("F001", "P001", request);
    }
    
    @Test
    @DisplayName("debe rechazar nombre vacío")
    void testRejectEmptyName() {
        PastureUpdateRequest request = new PastureUpdateRequest();
        request.setName("");
        
        assertThrows(ConstraintViolationException.class, () -> {
            processor.updatePasture("F001", "P001", request);
        });
    }
    
    @Test
    @DisplayName("debe rechazar área <= 0")
    void testRejectInvalidArea() {
        PastureUpdateRequest request = new PastureUpdateRequest();
        request.setAreaHa(-1.0);
        
        assertThrows(ConstraintViolationException.class, () -> {
            processor.updatePasture("F001", "P001", request);
        });
    }
}
```

### Test Integración (Spring Boot Test)

```java
@SpringBootTest
class PastureUpdateControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PastureUpdateProcessor processor;
    
    @Test
    @DisplayName("PUT /pastures/{id} debe retornar 200")
    void testPutSuccess() throws Exception {
        PastureDTO mockResponse = createMockDTO();
        PastureUpdateRequest request = PastureUpdateRequest.builder()
            .name("Nuevo Nombre")
            .build();
        
        when(processor.updatePasture(any(), any(), any())).thenReturn(mockResponse);
        
        mockMvc.perform(put("/farms/F001/pastures/P001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Nuevo Nombre"));
    }
    
    @Test
    @DisplayName("PUT con payload inválido retorna 400")
    void testPutBadRequest() throws Exception {
        String invalidPayload = "{\"name\": \"\", \"areaHa\": -5}";
        
        mockMvc.perform(put("/farms/F001/pastures/P001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest());
    }
}
```

### Test E2E (Cypress)

```javascript
describe('Editar Potrero - PUT Endpoint', () => {
  
  beforeEach(() => {
    cy.login('juan@farm.com');
  });
  
  it('debe editar nombre del potrero', () => {
    cy.intercept('PUT', '/farms/*/pastures/*', {
      statusCode: 200,
      body: { id: 'P001', name: 'Nuevo Nombre' }
    }).as('updatePasture');
    
    // Hacer request PUT directamente
    cy.request('PUT', '/farms/F001/pastures/P001', {
      name: 'Nuevo Nombre'
    }).then((response) => {
      expect(response.status).to.equal(200);
      expect(response.body.name).to.equal('Nuevo Nombre');
    });
  });
  
  it('debe rechazar área inválida', () => {
    cy.request({
      method: 'PUT',
      url: '/farms/F001/pastures/P001',
      body: { areaHa: -5 },
      failOnStatusCode: false
    }).then((response) => {
      expect(response.status).to.equal(400);
      expect(response.body.message).to.contain('mayor a 0');
    });
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD con Gherkin)**

### Escenario 1: Editar Nombre Exitosamente
```gherkin
Scenario: Actualizar nombre de potrero
  Given existe potrero P001 con name="Potrero 1"
  When se envía:
    PUT /farms/F001/pastures/P001
    { "name": "Potrero Mejorado" }
  Then retorna HTTP 200 OK
  And potrero tiene:
    | name | Potrero Mejorado |
    | updatedAt | 2025-12-09T10:30:45Z |
  And otros campos sin cambios
```

### Escenario 2: Editar Múltiples Campos
```gherkin
Scenario: Actualizar varios campos simultáneamente
  Given potrero P001:
    | name | Potrero 1 |
    | areaHa | 0.8 |
    | species | KIKUYO |
  When se envía:
    PUT /farms/F001/pastures/P001
    {
      "name": "Potrero Grande",
      "areaHa": 2.5,
      "species": "BRACHIARIA"
    }
  Then todos los campos se actualizan:
    | name | Potrero Grande |
    | areaHa | 2.5 |
    | species | BRACHIARIA |
  And status y ETA sin cambios
```

### Escenario 3: Validación de Área
```gherkin
Scenario: Rechazar área inválida (<=0)
  When se envía:
    { "areaHa": 0 }
  Then retorna HTTP 400:
    "Área debe ser mayor a 0"
  
  When se envía:
    { "areaHa": 150 }  // > 100
  Then retorna HTTP 400:
    "Área no puede ser mayor a 100 hectáreas"
```

### Escenario 4: Potrero No Existe
```gherkin
Scenario: Intenta editar potrero inexistente
  When se envía:
    PUT /farms/F001/pastures/NONEXISTENT
    { "name": "Nuevo" }
  Then retorna HTTP 404:
    "Potrero NONEXISTENT no encontrado"
```

### Escenario 5: No Afecta Estado de Rotación
```gherkin
Scenario: Editar no recalcula estado
  Given potrero P001 en EN_DESCANSO con ETA=22
  When se envía:
    PUT /farms/F001/pastures/P001
    { "name": "Nuevo" }
  Then potrero actualizado:
    | name | Nuevo |
    | status | EN_DESCANSO (sin cambios) |
    | eta | 22 (sin cambios) |
```

---

## 📚 **Referencias y Dependencias**

**Dependencias de otros componentes**:
- ✅ `PastureRepository` (completado)
- ✅ `PastureDTO` (completado)
- ✅ `Pasture` (entity)
- ✅ `PlanRepository` (para validar especies)

**Documentación relacionada**:
- [HU#1: Backend POST Eventos](../P0/PASTURES-HU-001-post-eventos.md)
- [Pastures Overview](../../pastures/pastures-overview.md)
- [Architecture Index](../../architecture/index.md)

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Layered Architecture (Controller + Processor + Service + Repository) - Extensión del patrón HU#1

**Justificación:** **Consistencia arquitectónica**: Mismo patrón que HU#1 (POST eventos) garantiza reutilización de conceptos probados. **DTO como whitelist**: PastureUpdateRequest solo incluye 4 campos editables (name, areaHa, species, notes) - campos protegidos (status, eta, lastUseAt) se ignoran automáticamente. **Simplicidad**: Sin lógica de motor de estados (a diferencia de POST eventos), solo persistencia de cambios. **Seguridad**: Validación en capas - DTO (JSR-380) + Service (plan check) + Repository (atomic update). **Validación técnica completada**: PastureRepository.update() verificado, Entity tiene todos los campos, EntityPatch reutilizable, patrón DTO probado en HU#1.

**Componentes Afectados:**

- **PastureUpdateRequest.java (Nuevo - DTO entrada):** Validar y canalizar cambios permitidos. Campos: `name` (String, 1-100 chars), `areaHa` (Double, 0.01-100), `species` (String), `notes` (String, 0-500). Validaciones: `@Size`, `@DecimalMin/@DecimalMax`, `@AssertTrue` (al menos un campo). **CRÍTICO**: Solo estos 4 campos en DTO - otros ignorados automáticamente por builder pattern. Validador `@ValidSpecies` para verificar plan existe.

- **ValidSpecies.java (Nuevo - Validador personalizado):** Validar que especie tiene plan de rotación. Annotation `@ValidSpecies` + `SpeciesValidator` (implements ConstraintValidator). Consulta `PlanRepository.existsBySpecies(species)`. Optional - null pasa validación.

- **PastureUpdateController.java (Nuevo):** Endpoint PUT. `@RequestMapping("/farms/{farmId}/pastures/{pastureId}")`, método `@PutMapping`, recibe `@Valid PastureUpdateRequest`. Retorna HTTP 200 + PastureDTO. Validación automática de DTO (400 si inválido). Delega a `PastureUpdateProcessor`.

- **PastureUpdateProcessor.java (Nuevo - Orquestador):** Orquestra actualización. Recibe farmId, pastureId, PastureUpdateRequest. Delega a `PastureService.updatePasture()`. Mapea Pasture → PastureDTO. Manejo de excepciones: RepositoryException → ProcessingException. Logging con LambdaContext.

- **PastureService (Modificación - Agregar método):** Nuevo método `updatePasture(String farmId, String pastureId, PastureUpdateRequest request)`. Busca potrero (404 si no existe). Si species cambia: valida plan (400 si no existe). Construye EntityPatch con campos a actualizar. Agrega `updatedAt` automáticamente. Nota: "" → null. Llama `PastureRepository.update()`. Retorna actualizado.

- **PastureRepository (Verificación):** Método `update(String pk, EntityPatch patch)` ya existe ✅ - reutilizable.

- **Pasture Entity (Verificación):** Campos name, areaHa, species, notes, updatedAt ya mapeados ✅.

- **PastureDTO (Verificación - Campo updatedAt):** Agregar `updatedAt` si no existe (ISO-8601 timestamp).

**Hitos de Implementación:**

1. **ValidSpecies.java** - Validador personalizado (sin dependencias)
2. **PastureUpdateRequest.java** - DTO validador (depende: ValidSpecies)
3. **PastureUpdateController.java** - Endpoint HTTP (depende: PastureUpdateProcessor)
4. **PastureUpdateProcessor.java** - Orquestador (depende: PastureService, PasturesMapper)
5. **PastureService.updatePasture()** - Agregar método (depende: PastureRepository, PlanRepository)
6. **PastureDTO - Agregar updatedAt** - Campo timestamp
7. **Tests** - Unitarios, integración, E2E

### Validación de Impacto

**Hallazgos de validación técnica:**

✅ **Componentes Existentes Reutilizables:**
- `PastureRepository.update(pk, patch)` - Ya implementado (HU#1) ✅
- `EntityPatch.builder()` - Patrón probado (HU#1) ✅
- `PasturesMapper.toDTO()` - Ya existe (HU#1) ✅
- `PastureService` - Estructura lista para agregar método ✅

✅ **Arquitectura Verificada:**
- Entity Pasture tiene campos: name, areaHa, species, notes ✅
- DynamoDB TABLE_PASTURE mapeada correctamente ✅
- Validaciones JSR-380 (Spring Boot incluye) ✅
- PlanRepository existe para validar especies ✅

✅ **Impacto en Performance:**
- Operación: Un potrero, un UPDATE a DynamoDB
- Sin lógica compleja (no recalcula ETA, no motor de estados)
- EntityPatch optimizado (solo campos modificados)
- Sin impacto en rotación automática

✅ **Seguridad:**
- Whitelist de campos (DTO actúa como filtro)
- Otros campos (status, eta, lastUseAt) ignorados silenciosamente
- Validación de especies via plan existente
- No afecta estado de rotación

✅ **Testing:**
- Validadores: Funciones puras, 100% coverage
- Service: Mock repository, mock plan service
- Controller: MockMvc estándar
- E2E: Direct HTTP requests

✅ **Flujo de Invocación Verificado:**
```
PUT /farms/{farmId}/pastures/{pastureId}
→ PastureUpdateController.updatePasture()
  → Validar DTO automático (Spring)
  → PastureUpdateProcessor.updatePasture()
    → PastureService.updatePasture()
      → Buscar potrero (404 si no existe)
      → Si species: validar plan (400 si no existe)
      → EntityPatch.builder() con cambios
      → PastureRepository.update() → DynamoDB
      → Retornar actualizado
    → PasturesMapper.toDTO()
  ← HTTP 200 + PastureDTO
```

✅ **Riesgos Mitigables:**
- Nombre vacío → Validación `@Size(min=1)` + `@NotEmpty`
- Área inválida → `@DecimalMin/@DecimalMax`
- Especie no existe → `@ValidSpecies` valida plan
- Potrero no existe → 404 en service
- Campos protegidos editados → Silenciosamente ignorados (builder pattern)
- Race condition → EntityPatch es atómico en DynamoDB
- Notes muy largo → `@Size(max=500)`

### Notas Técnicas

**Patrón DTO como Whitelist:**
```java
@Data
@Builder
public class PastureUpdateRequest {
    @Size(min = 1, max = 100)
    private String name;           // ← Editable
    
    @DecimalMin("0.01")
    @DecimalMax("100")
    private Double areaHa;         // ← Editable
    
    @ValidSpecies
    private String species;        // ← Editable
    
    @Size(max = 500)
    private String notes;          // ← Editable
    
    // NO incluye: status, eta, lastUseAt, etc.
}
```

**EntityPatch - Actualización Selectiva:**
```java
EntityPatch patch = EntityPatch.of();

if (request.getName() != null) {
    patch.set("name", request.getName());
}
if (request.getAreaHa() != null) {
    patch.set("areaHa", request.getAreaHa());
}
patch.set("updatedAt", Instant.now());  // Siempre
```

**Validador @ValidSpecies:**
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SpeciesValidator.class)
public @interface ValidSpecies {
    String message() default "Especie no válida";
}

public class SpeciesValidator implements ConstraintValidator<ValidSpecies, String> {
    @Autowired
    private PlanRepository planRepository;
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return planRepository.existsBySpecies(value);
    }
}
```

**Auditoría Preparada (HU#13):**
- Campo `updatedAt` registra cuándo cambió
- User desde JWT/Security context disponible
- Estructura lista para tabla de auditoría futura

### Referencias y Validación

**Documentación Consultada:**
- [architecture-cattle-lambda-function.md](../../architecture/architecture-cattle-lambda-function.md) - Layered pattern verificado
- [PASTURES-HU-001](../P0/PASTURES-HU-001-post-eventos.md) - Patrón base (Controller→Processor→Service→Repository)
- [Pastures Overview](../../pastures/pastures-overview.md) - Contexto

**Historias Relacionadas:**
- ✅ PASTURES-HU-001: Backend POST Eventos (patrón base, aprobado)
- → PASTURES-HU-004: Backend PUT Editar (esta - reutiliza patrón)
- → PASTURES-HU-013: Auditoría (usará campo updatedAt)

**Stack Tecnológico Verificado:**
- Java 21 - Syntax moderno
- Spring Boot 3.4.5 - Validation, REST
- Lombok - @Data, @Builder
- JSR-380 - Bean Validation

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Extensión consistente del patrón HU#1 (arquitectura probada)

---

## 🔧 **Refinamiento Técnico**

### API Contract

**Endpoint:**
```
PUT /farms/{farmId}/pastures/{pastureId}
Content-Type: application/json
Authorization: Bearer {jwt}
```

**Request Body:**
```json
{
  "name": "Potrero Editado",
  "description": "Nueva descripción",
  "areHa": 6.5,
  "animalLoad": 25,
  "version": 1
}
```

**Response (200 OK):**
```json
{
  "id": "P001",
  "name": "Potrero Editado",
  "areHa": 6.5,
  "updatedAt": "2026-01-09T10:30:45Z",
  "version": 2
}
```

### Versioning - Optimistic Lock

```pseudocode
PUT /farms/{farmId}/pastures/{pastureId}

// 1. Validar versión
IF request.version != pasture.version THEN RETURN 409

// 2. Aplicar cambios
patch.updatedAt = now()
patch.version = version + 1

// 3. Guardar
pastureRepository.update(PK, patch)

// 4. Retornar 200 OK
RETURN pasture
```

### Testing Strategy

**Tests Críticos:**
- PUT exitoso con datos válidos (200 OK)
- 409 Conflict si version mismatch
- 404 si pasture no existe
- Validaciones de entrada (name, areHa)

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `PastureUpdateRequest.java` con validaciones completas
- [ ] `PastureUpdateController.java` implementado
- [ ] `PastureUpdateProcessor.java` orquesta cambios correctamente
- [ ] `PastureService.updatePasture()` implementado
- [ ] Validador `@ValidSpecies` funcionando (si es necesario)
- [ ] Tests unitarios: cobertura >= 85%
- [ ] Tests integración: todos pasan
- [ ] Manual testing en Postman/Insomnia: name, areaHa, species, notes
- [ ] Validaciones de error probadas (400, 404)
- [ ] Campo `updatedAt` se registra correctamente
- [ ] Campos no editables se ignoran silenciosamente
- [ ] Estado de rotación no se ve afectado
- [ ] Documentación actualizada (JSDoc/JavaDoc)
- [ ] Code review aprobado (2 approvals)
- [ ] Sin warnings de linting
- [ ] CI/CD green (build, test, coverage)
- [ ] Demostrable en staging environment
- [ ] Auditoría preparada (estructura para HU#13)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
