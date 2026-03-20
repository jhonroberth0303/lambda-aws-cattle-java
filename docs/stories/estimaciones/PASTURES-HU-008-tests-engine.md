# 🌱 PASTURES-HU#8: Backend: Tests Unitarios PastureStatusEngine

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟠 ALTO (P1) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Parametrized tests + State Machine testing | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend developer**, quiero escribir tests unitarios completos para el `PastureStatusEngine`, de tal forma que:

1. Se validen todas las transiciones de estado posibles
2. Se detecten transiciones inválidas
3. Se verifique que el cálculo de ETA es correcto
4. Se prueben casos edge y especiales
5. Se garantice un mínimo de 85% de cobertura
6. Los tests sean rápidos, determinísticos y aislados
7. Se documente el comportamiento esperado del engine

Esto habilitará que cambios futuros al engine no rompan lógica existente, y aumentará la confianza en el sistema de rotación.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Tests para DISPONIBLE → EN_USO
```gherkin
Scenario: Potrero en DISPONIBLE puede abrirse (OPEN event)
  Given potrero en estado DISPONIBLE
  And ETA = 0 (disponible ahora)
  When se aplica evento OPEN
  Then:
    [ ] Nuevo estado: EN_USO
    [ ] lastUseAt: timestamp actual
    [ ] ETA se recalcula
    [ ] height se usa como currentHeight (si existe)
    [ ] Transición exitosa (true)
```

### AC#2: Tests para EN_USO → EN_DESCANSO
```gherkin
Scenario: Potrero en EN_USO puede cerrarse (CLOSE event)
  Given potrero en EN_USO
  And altura residual = 15 cm
  And duración descanso según plan = 30 días
  When se aplica evento CLOSE con residualHeight
  Then:
    [ ] Nuevo estado: EN_DESCANSO
    [ ] residualHeightCm: 15
    [ ] ETA se calcula: hoy + 30 días = fecha futura
    [ ] lastClosedAt: timestamp actual
    [ ] Transición exitosa (true)
```

### AC#3: Tests para EN_DESCANSO → DISPONIBLE
```gherkin
Scenario: Potrero en EN_DESCANSO se vuelve DISPONIBLE cuando ETA vence
  Given potrero en EN_DESCANSO
  And ETA = -2 días (vencido)
  When se ejecuta estado engine (status resolver)
  Then:
    [ ] Estado automáticamente: DISPONIBLE
    [ ] ETA se resetea a 0
    [ ] Está listo para uso nuevamente
    [ ] No requiere evento (automático)
```

### AC#4: Tests para MANTENIMIENTO transitions
```gherkin
Scenario: Potrero puede bloquearse (MAINTENANCE_SET)
  Given potrero en cualquier estado (DISPONIBLE, EN_USO, EN_DESCANSO)
  When se aplica MAINTENANCE_SET
  Then:
    [ ] Estado: MANTENIMIENTO
    [ ] substatus: según razón (FERTILIZANDO, CUARENTENA, etc)
    [ ] holdUntil: fecha desbloqueado
    [ ] ETA: INFINITY (no disponible)
    [ ] Transición exitosa (true)

Scenario: Potrero puede desbloquearse (MAINTENANCE_CLEAR)
  Given potrero en MANTENIMIENTO
  When se aplica MAINTENANCE_CLEAR
  Then:
    [ ] Estado previo restaurado (si se guarda)
    [ ] O estado: EN_DESCANSO (default)
    [ ] substatus: null
    [ ] holdUntil: null
    [ ] ETA se recalcula
    [ ] Transición exitosa (true)
```

### AC#5: Tests para Transiciones Inválidas
```gherkin
Scenario: Rechazar transiciones inválidas
  Given potrero en EN_DESCANSO
  When intenta: EN_DESCANSO → DISPONIBLE (directo, sin ETA vencer)
  Then:
    [ ] Transición rechazada (false)
    [ ] Estado no cambia
    [ ] Error: "No se puede hacer esta transición"
  
  Given potrero en MANTENIMIENTO
  When intenta: MANTENIMIENTO → EN_USO (directo)
  Then también rechazada (debe pasar por desbloqueo)
  
  Given potrero en EN_USO
  When intenta: EN_USO → EN_USO (mismo estado)
  Then rechazada o ignorada
```

### AC#6: Tests para Validación de Datos
```gherkin
Scenario: Validar datos requeridos en eventos
  Given evento CLOSE sin residualHeight
  When se aplica
  Then:
    [ ] Rechazada si height es requerida
    [ ] Error: "Altura residual es requerida"
  
  Given evento MAINTENANCE_SET sin holdUntil
  When se aplica
  Then rechazada
```

### AC#7: Tests para Cálculo de ETA
```gherkin
Scenario: ETA se calcula correctamente al cambiar estado
  Given potrero CLOSE con:
    | species | KIKUYO |
    | restDaysRequired | 30 días |
    | today | 2025-12-09 |
  When se calcula ETA
  Then:
    [ ] ETA = 30 (días)
    [ ] etaDate = 2026-01-08
    [ ] etaMessage: "Disponible en 30 días"
  
  When pasan 15 días (hoy = 2025-12-24)
  And se recalcula ETA
  Then:
    [ ] ETA = 15 (días restantes)
    [ ] etaDate = 2026-01-08 (sin cambios)
```

### AC#8: Tests para Estados Especiales
```gherkin
Scenario: Manejar estados especiales (SOLD, REMOVED)
  Given potrero en DISPONIBLE
  When evento especial: PASTURE_SOLD o PASTURE_REMOVED
  Then:
    [ ] Estado: SOLD o REMOVED
    [ ] No puede volver a transicionar
    [ ] Bloqueado permanentemente
    [ ] Tests de esta lógica
```

### AC#9: Tests para ETA Edge Cases
```gherkin
Scenario: Manejar ETA edge cases
  When ETA es hoy (no futuro):
    [ ] ETA = 0 (ya disponible)
    [ ] etaMessage: "Disponible ahora"
  
  When ETA es en el pasado:
    [ ] ETA = negativo
    [ ] etaMessage: "Vencido hace X días"
    [ ] Estado resueltor lo cambia a DISPONIBLE
  
  When ETA es mucho en el futuro (1 año):
    [ ] ETA = 365
    [ ] Sin errores, maneja correctamente
```

### AC#10: Tests para Múltiples Eventos Secuenciales
```gherkin
Scenario: Aplicar múltiples eventos en secuencia
  Given potrero en DISPONIBLE
  When:
    1. OPEN → EN_USO ✓
    2. CLOSE (residual=15) → EN_DESCANSO ✓
    3. MAINTENANCE_SET → MANTENIMIENTO ✓
    4. MAINTENANCE_CLEAR → EN_DESCANSO ✓
  Then:
    [ ] Cada transición válida
    [ ] Estado final correcto
    [ ] Historial de cambios intacto
```

### AC#11: Cobertura de Tests >= 85%
```gherkin
Scenario: Validar cobertura de código
  When ejecuta coverage en PastureStatusEngine
  Then:
    [ ] Cobertura total: >= 85%
    [ ] Líneas no cubiertas: máximo 15%
    [ ] Branches: >= 80%
    [ ] Functions: >= 85%
    [ ] Statements: >= 85%
```

### AC#12: Tests Determinísticos
```gherkin
Scenario: Tests no tienen dependencias externas aleatorias
  When ejecuta tests múltiples veces
  Then:
    [ ] Siempre pasan (sin race conditions)
    [ ] Sin dependencias a fecha/hora actual (usar mock)
    [ ] Sin dependencias a BD (todo mocked)
    [ ] Sin dependencias a red (todo mocked)
```

### AC#13: Tests Performantes
```gherkin
Scenario: Tests ejecutan rápido
  When ejecuta test suite de PastureStatusEngine
  Then:
    [ ] Tiempo total < 5 segundos
    [ ] Cada test < 100ms
    [ ] Sin delays artificiales
    [ ] Sin timeout issues
```

### AC#14: Tests Documentados
```gherkin
Scenario: Tests documentan el comportamiento esperado
  Given archivo de tests
  Then:
    [ ] Cada test tiene descripción clara
    [ ] Comentarios en lógica compleja
    [ ] README o TESTING.md con instrucciones
    [ ] Documentación de casos edge
```

### AC#15: Casos Edge Documentados
```gherkin
Scenario: Edge cases están cubiertos y documentados
  Then tests incluyen:
    [ ] Valores boundary (0, máximo, mínimo)
    [ ] Valores null/undefined
    [ ] Strings vacíos
    [ ] Dates en el pasado/futuro
    [ ] Transiciones en orden incorrecto
    [ ] Múltiples eventos simultáneos (si aplica)
```

---

## 📊 **Especificación Técnica**

### Estructura de Tests

#### Test Suite - `PastureStatusEngine.test.java`

```java
@DisplayName("PastureStatusEngine - Transiciones de Estado")
class PastureStatusEngineTest {
  
  private PastureStatusEngine engine;
  
  @BeforeEach
  void setUp() {
    engine = new PastureStatusEngine();
    // Inyectar mocks de repositories, calculators, etc.
  }
  
  // Tests para DISPONIBLE → EN_USO
  @Nested
  @DisplayName("DISPONIBLE → EN_USO (OPEN)")
  class OpenTransitionTests { ... }
  
  // Tests para EN_USO → EN_DESCANSO
  @Nested
  @DisplayName("EN_USO → EN_DESCANSO (CLOSE)")
  class CloseTransitionTests { ... }
  
  // Tests para EN_DESCANSO → DISPONIBLE
  @Nested
  @DisplayName("EN_DESCANSO → DISPONIBLE (auto)")
  class AutoTransitionTests { ... }
  
  // Tests para MANTENIMIENTO
  @Nested
  @DisplayName("MANTENIMIENTO transitions")
  class MaintenanceTransitionTests { ... }
  
  // Tests para transiciones inválidas
  @Nested
  @DisplayName("Transiciones Inválidas")
  class InvalidTransitionTests { ... }
  
  // Tests para ETA
  @Nested
  @DisplayName("Cálculo de ETA")
  class ETACalculationTests { ... }
}
```

### Casos de Prueba Detallados

#### Transición OPEN (DISPONIBLE → EN_USO)

```java
@Test
@DisplayName("OPEN exitoso: DISPONIBLE → EN_USO")
void testOpenSuccess() {
  // Arrange
  Pasture pasture = createPasture(Status.DISPONIBLE, eta: 0);
  OpenPastureEvent event = new OpenPastureEvent()
    .withHeight(45) // cm
    .withNotes("Abierto para animales");
  
  // Act
  StatusTransitionResult result = engine.apply(pasture, event);
  
  // Assert
  assertEquals(Status.EN_USO, result.getNewStatus());
  assertEquals(event.getHeight(), pasture.getCurrentHeightCm());
  assertNotNull(pasture.getLastUseAt());
  assertTrue(result.isSuccess());
}

@Test
@DisplayName("OPEN falla: potrero no en DISPONIBLE")
void testOpenFailureWrongStatus() {
  // Arrange
  Pasture pasture = createPasture(Status.EN_DESCANSO, eta: 15);
  OpenPastureEvent event = new OpenPastureEvent();
  
  // Act
  StatusTransitionResult result = engine.apply(pasture, event);
  
  // Assert
  assertEquals(Status.EN_DESCANSO, pasture.getStatus());
  assertFalse(result.isSuccess());
  assertEquals("No se puede abrir un potrero en EN_DESCANSO", result.getError());
}

@Test
@DisplayName("OPEN requiere altura válida")
void testOpenRequiresValidHeight() {
  Pasture pasture = createPasture(Status.DISPONIBLE);
  OpenPastureEvent event = new OpenPastureEvent()
    .withHeight(-5); // Inválido
  
  StatusTransitionResult result = engine.apply(pasture, event);
  
  assertFalse(result.isSuccess());
  assertTrue(result.getError().contains("Altura"));
}
```

#### Transición CLOSE (EN_USO → EN_DESCANSO)

```java
@Test
@DisplayName("CLOSE exitoso: calcula ETA correctamente")
void testCloseSuccess() {
  // Arrange
  Pasture pasture = createPasture(Status.EN_USO);
  Plan plan = createPlan(species: KIKUYO, restDaysMin: 30);
  ClosePastureEvent event = new ClosePastureEvent()
    .withResidualHeight(15)
    .withNotes("Pastar finalizado");
  
  // Act
  StatusTransitionResult result = engine.apply(pasture, event, plan);
  
  // Assert
  assertEquals(Status.EN_DESCANSO, result.getNewStatus());
  assertEquals(15, pasture.getResidualHeightCm());
  assertNotNull(pasture.getLastClosedAt());
  assertEquals(30, pasture.getEta()); // días
  assertTrue(result.isSuccess());
}

@Test
@DisplayName("CLOSE falla: altura residual > altura mínima plan")
void testCloseFailureInvalidHeight() {
  Pasture pasture = createPasture(Status.EN_USO);
  Plan plan = createPlan(minHeightRequired: 20);
  ClosePastureEvent event = new ClosePastureEvent()
    .withResidualHeight(150); // Mayor que mínimo
  
  StatusTransitionResult result = engine.apply(pasture, event, plan);
  
  assertFalse(result.isSuccess());
  assertEquals(Status.EN_USO, pasture.getStatus()); // Sin cambios
}

@Test
@DisplayName("CLOSE falla: altura residual < 0")
void testCloseFailureNegativeHeight() {
  Pasture pasture = createPasture(Status.EN_USO);
  ClosePastureEvent event = new ClosePastureEvent()
    .withResidualHeight(-5);
  
  StatusTransitionResult result = engine.apply(pasture, event);
  
  assertFalse(result.isSuccess());
}
```

#### Transición Automática (EN_DESCANSO → DISPONIBLE)

```java
@Test
@DisplayName("Auto-transición cuando ETA vence")
void testAutoTransitionWhenETAExpires() {
  // Arrange
  LocalDate today = LocalDate.of(2025, 12, 9);
  Pasture pasture = createPasture(Status.EN_DESCANSO);
  pasture.setEta(-2); // Vencido hace 2 días
  
  // Act
  StatusTransitionResult result = engine.resolveAutoTransitions(pasture, today);
  
  // Assert
  assertEquals(Status.DISPONIBLE, result.getNewStatus());
  assertEquals(0, result.getNewEta());
  assertTrue(result.isSuccess());
}

@Test
@DisplayName("Sin cambios si ETA no ha vencido")
void testNoAutoTransitionIfETANotExpired() {
  Pasture pasture = createPasture(Status.EN_DESCANSO);
  pasture.setEta(5); // 5 días aún
  
  StatusTransitionResult result = engine.resolveAutoTransitions(pasture);
  
  assertEquals(Status.EN_DESCANSO, pasture.getStatus());
  assertFalse(result.isTransitionApplied());
}
```

#### Mantenimiento

```java
@Test
@DisplayName("MAINTENANCE_SET bloquea potrero")
void testMaintenanceSetSuccess() {
  Pasture pasture = createPasture(Status.DISPONIBLE);
  MaintenanceSetEvent event = new MaintenanceSetEvent()
    .withReason("Fertilización")
    .withHoldUntil(LocalDate.of(2025, 12, 20));
  
  StatusTransitionResult result = engine.apply(pasture, event);
  
  assertEquals(Status.MANTENIMIENTO, result.getNewStatus());
  assertEquals("FERTILIZANDO", result.getSubstatus());
  assertEquals(Integer.MAX_VALUE, result.getNewEta()); // INFINITY
  assertTrue(result.isSuccess());
}

@Test
@DisplayName("MAINTENANCE_CLEAR desbloquea potrero")
void testMaintenanceClearSuccess() {
  Pasture pasture = createPasture(Status.MANTENIMIENTO);
  pasture.setSubstatus("CUARENTENA");
  MaintenanceClearEvent event = new MaintenanceClearEvent();
  
  StatusTransitionResult result = engine.apply(pasture, event);
  
  assertEquals(Status.EN_DESCANSO, result.getNewStatus()); // o estado previo
  assertNull(result.getSubstatus());
  assertTrue(result.isSuccess());
}
```

#### Transiciones Inválidas

```java
@Test
@DisplayName("Transiciones inválidas son rechazadas")
void testInvalidTransitions() {
  Pasture pasture = createPasture(Status.EN_DESCANSO);
  
  // EN_DESCANSO → EN_USO (inválido, debe pasar por DISPONIBLE)
  OpenPastureEvent event = new OpenPastureEvent();
  StatusTransitionResult result = engine.apply(pasture, event);
  
  assertFalse(result.isSuccess());
  assertEquals(Status.EN_DESCANSO, pasture.getStatus());
}

@Test
@DisplayName("Estados terminales no transicionan")
void testTerminalStatesNoTransition() {
  Pasture pasture = createPasture(Status.SOLD);
  
  OpenPastureEvent event = new OpenPastureEvent();
  StatusTransitionResult result = engine.apply(pasture, event);
  
  assertFalse(result.isSuccess());
  assertEquals(Status.SOLD, pasture.getStatus());
}
```

#### Cálculo de ETA

```java
@Test
@DisplayName("ETA se calcula correctamente con Plan")
void testETACalculationWithPlan() {
  // Arrange
  LocalDate today = LocalDate.of(2025, 12, 9);
  Plan plan = createPlan(restDaysMin: 30, restDaysMax: 45);
  
  // Act
  int eta = engine.calculateETA(today, plan);
  
  // Assert
  assertEquals(30, eta); // Mínimo
}

@Test
@DisplayName("ETA dinámico: recalcula según crecimiento")
void testDynamicETABasedOnGrowth() {
  // Arrange
  int residualHeight = 15;
  int minHeightRequired = 25;
  int growthRate = 1; // cm/día
  Plan plan = createPlan(...);
  
  // Act
  int daysNeeded = engine.calculateDaysUntilReadyHeight(
    residualHeight, minHeightRequired, growthRate
  );
  
  // Assert
  assertEquals(10, daysNeeded); // (25-15) / 1 = 10 días
}

@Test
@DisplayName("ETA edge case: ya en altura mínima")
void testETAWhenAlreadyAtMinHeight() {
  int residualHeight = 25;
  int minHeightRequired = 25;
  
  int daysNeeded = engine.calculateDaysUntilReadyHeight(
    residualHeight, minHeightRequired, growthRate: 1
  );
  
  assertEquals(0, daysNeeded); // Ya listo
}

@Test
@DisplayName("ETA nunca negativo")
void testETANeverNegative() {
  int eta = engine.calculateETA(futureDateByOneMillion);
  
  assertTrue(eta >= 0);
}
```

#### Casos Edge

```java
@Test
@DisplayName("Potrero con valores nulos manejados gracefully")
void testNullValuesHandled() {
  Pasture pasture = createPasture(Status.DISPONIBLE);
  pasture.setLastUseAt(null);
  pasture.setLastClosedAt(null);
  pasture.setResidualHeightCm(null);
  
  StatusTransitionResult result = engine.apply(pasture, openEvent);
  
  assertTrue(result.isSuccess());
}

@Test
@DisplayName("Múltiples eventos en secuencia")
void testSequentialEvents() {
  Pasture pasture = createPasture(Status.DISPONIBLE);
  Plan plan = createPlan();
  
  // Secuencia: OPEN → CLOSE → MAINTENANCE_SET → MAINTENANCE_CLEAR
  StatusTransitionResult r1 = engine.apply(pasture, new OpenEvent());
  assertTrue(r1.isSuccess());
  
  StatusTransitionResult r2 = engine.apply(pasture, new CloseEvent(15));
  assertTrue(r2.isSuccess());
  
  StatusTransitionResult r3 = engine.apply(pasture, new MaintenanceSetEvent(...));
  assertTrue(r3.isSuccess());
  
  StatusTransitionResult r4 = engine.apply(pasture, new MaintenanceClearEvent());
  assertTrue(r4.isSuccess());
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`PastureStatusEngine.test.java`**
   - Test suite principal
   - 15+ métodos de test
   - Cobertura >= 85%

2. **`TestFixtures.java`** (helper)
   - Builder para Pasture, Plan, Events
   - Métodos reutilizables
   - Data de prueba común

3. **`TESTING.md`**
   - Documentación de tests
   - Cómo ejecutar
   - Cómo agregar nuevos tests

### Archivos a Verificar

1. **`PastureStatusEngine.java`**
   - Asegurar todas las transiciones están implementadas
   - Verificar validaciones

2. **`StatusTransitionResult.java`**
   - DTO para resultados de transición
   - Mensaje de error detallado

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear TestFixtures
```java
public class TestFixtures {
  
  public static Pasture createPasture(Status status, int eta) {
    return Pasture.builder()
      .id("P001")
      .status(status)
      .eta(eta)
      .build();
  }
  
  public static Plan createPlan(String species, int restDaysMin) {
    return Plan.builder()
      .species(species)
      .restDaysMin(restDaysMin)
      .build();
  }
}
```

### Paso 2: Crear test para transición OPEN
```java
@Nested
class OpenTransitionTests {
  
  @Test
  void testOpenSuccess() { ... }
  
  @Test
  void testOpenFailureWrongStatus() { ... }
  
  @Test
  void testOpenRequiresHeight() { ... }
}
```

### Paso 3: Crear tests para CLOSE
```java
@Nested
class CloseTransitionTests {
  
  @Test
  void testCloseSuccess() { ... }
  
  @Test
  void testCloseCalculatesETA() { ... }
}
```

### Paso 4: Crear tests para auto-transitions
```java
@Nested
class AutoTransitionTests {
  
  @Test
  void testAutoTransitionWhenETAExpires() { ... }
}
```

### Paso 5: Crear tests para mantenimiento
```java
@Nested
class MaintenanceTransitionTests {
  
  @Test
  void testMaintenanceSet() { ... }
  
  @Test
  void testMaintenanceClear() { ... }
}
```

### Paso 6: Tests para casos inválidos
```java
@Nested
class InvalidTransitionTests {
  
  @Test
  void testInvalidTransitions() { ... }
}
```

### Paso 7: Tests para ETA
```java
@Nested
class ETACalculationTests {
  
  @Test
  void testETACalculation() { ... }
}
```

---

## 🧪 **Cobertura de Tests**

**Clases/Métodos Críticos** a cubrir en PastureStatusEngine:

```
- apply(Pasture, Event) ........................... 100%
  └─ validateTransition() ......................... 100%
  └─ applyTransition() ............................ 100%
  
- resolveAutoTransitions(Pasture) ................ 95%
  
- calculateETA() ................................. 100%
  └─ calculateDaysUntilReadyHeight() ............. 100%
  
- Métodos helper privados ........................ 80%+
```

**Líneas No Cubiertas** (máximo 15%):
- Código muerto (if dead branches)
- Logs (info level)
- Excepciones muy raras

---

## 🔄 **Escenarios de Prueba (BDD)**

### Escenario 1: Transición Completa OPEN
```gherkin
Scenario: Abrir potrero exitosamente
  Given potrero en DISPONIBLE con ETA=0
  When se aplica evento OPEN con altura=45
  Then:
    - Status = EN_USO
    - currentHeight = 45
    - lastUseAt registrado
    - Result.success = true
```

### Escenario 2: CLOSE y cálculo de ETA
```gherkin
Scenario: Cerrar potrero y calcular ETA
  Given potrero en EN_USO
  And plan: KIKUYO con 30 días descanso
  When se aplica CLOSE con residualHeight=15
  Then:
    - Status = EN_DESCANSO
    - ETA = 30 días
    - residualHeight = 15
```

### Escenario 3: Auto-transición
```gherkin
Scenario: Potrero se vuelve disponible cuando ETA vence
  Given potrero EN_DESCANSO con ETA=-2 (vencido)
  When se ejecuta resolveAutoTransitions
  Then Status = DISPONIBLE
```

---

## 📚 **Referencias y Dependencias**

**Dependencias**:
- JUnit 5
- Mockito
- Assertions (AssertJ recomendado)

**Componentes relacionados**:
- PastureStatusEngine.java
- Plan, Pasture entities

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Test-Driven Development + Parametrized Tests + State Machine Testing

**Justificación:** **Estado Machine**: PastureStatusEngine es máquina de estados (DISPONIBLE→EN_USO→EN_DESCANSO→...). **Parametrized tests**: Múltiples transiciones iguales con diferentes inputs. **Casos edge**: Estados especiales (SOLD, REMOVED, MANTENIMIENTO). **Determinístico**: Tests no dependen de time, timestamps mockeados. **85% coverage**: Cobertura alta para confianza en cambios futuros.

**Componentes a Testear:**

- **PastureStatusEngine.java (existente):** Métodos: `applyEvent(pasture, event) → boolean`, `resolveStatus(pasture) → Status`. Tests para: DISPONIBLE→EN_USO, EN_USO→EN_DESCANSO, EN_DESCANSO→DISPONIBLE (auto), MANTENIMIENTO transitions, transiciones inválidas, validación datos.

**Test Estructura:**
1. Setup: Crear pasture mock con datos iniciales
2. Execute: Aplicar evento
3. Assert: Verificar nuevo estado, ETA, timestamps, etc.
4. Parametrized: Múltiples (inicial state, evento) → final state

**Casos Críticos:**
- Transiciones válidas: ✅
- Transiciones inválidas: ❌ (rechazadas)
- Cambios automáticos (EN_DESCANSO→DISPONIBLE cuando ETA vence)
- MANTENIMIENTO bloquea todo

**Hitos:**
1. Test suite para transiciones válidas
2. Test suite para transiciones inválidas
3. Test suite para auto-transiciones
4. Test suite para MANTENIMIENTO
5. Cobertura 85%+ verificada

### Validación de Impacto

✅ **Máquina de Estados Validada:**
- Todas transiciones válidas testeadas
- Transiciones inválidas rechazadas
- Estados especiales (SOLD, REMOVED) manejados

✅ **Cobertura 85%+:**
- Rama coverage completo
- Tests determinísticos
- Sin dependencias de time real

✅ **Cambios Futuros Protegidos:**
- Tests previenen regresiones
- Confianza en refactorings

### Notas Técnicas

**Parametrized Test - Estructura:**
```java
@ParameterizedTest
@CsvSource({
  "DISPONIBLE,OPEN,EN_USO,true",
  "EN_USO,CLOSE,EN_DESCANSO,true",
  "EN_DESCANSO,OPEN,DISPONIBLE,false",  // Inválida
})
void testTransition(String from, String event, String to, boolean expected) {
  Pasture p = createPasture(from);
  boolean result = engine.applyEvent(p, createEvent(event));
  assertEquals(expected, result);
  if (expected) assertEquals(to, p.getStatus());
}
```

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-001: Backend POST Eventos (eventos)
- ✅ PASTURES-HU-004: Backend PUT Editar (estados)
- → PASTURES-HU-008: Backend Tests Engine (esta - state machine)
- → PASTURES-HU-009: Backend Tests ETA (calculadora)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** State machine testing + parametrized tests (cobertura 85%+)

---

## 🔧 **Refinamiento Técnico**

### State Machine Testing - Parametrized

```java
@ParameterizedTest
@CsvSource({
  // eventType, currentStatus, expectedStatus, shouldSucceed
  "OPEN,DISPONIBLE,EN_USO,true",
  "OPEN,EN_USO,EN_USO,false",
  "CLOSE,EN_USO,EN_DESCANSO,true",
  "CLOSE,DISPONIBLE,DISPONIBLE,false",
  "MAINTENANCE_SET,DISPONIBLE,MANTENIMIENTO,true",
  "MAINTENANCE_CLEAR,MANTENIMIENTO,DISPONIBLE,true"
})
void testStateTransitions(String eventType, String currentStatus, String expectedStatus, boolean shouldSucceed) {
  Pasture pasture = createPastureWithStatus(currentStatus);
  PastureEvent event = createEvent(eventType);
  
  if (shouldSucceed) {
    pasture = pastureStatusEngine.applyEvent(pasture, event);
    assertEquals(expectedStatus, pasture.getStatus());
  } else {
    assertThrows(InvalidStateTransitionException.class, 
      () -> pastureStatusEngine.applyEvent(pasture, event));
  }
}
```

### Test Fixtures

```java
public class PastureEventTestFixtures {
  public static Pasture createPastureWithStatus(String status) {
    return Pasture.builder()
      .id("P001")
      .status(Status.valueOf(status))
      .createdAt(Instant.now())
      .build();
  }
  
  public static PastureEvent createOpenEvent() {
    return OpenEvent.builder()
      .lotId("LOT001")
      .animalCount(15)
      .user("user@farm.com")
      .build();
  }
}
```

### Test Coverage Goals

- **Happy Path**: Transiciones válidas
- **Invalid Transitions**: Estados no permitidos
- **Edge Cases**: Múltiples eventos en rápida sucesión
- **Error Handling**: Validaciones de entrada
- **Target**: 85%+ cobertura

### Testing Strategy

```bash
# Ejecutar tests
mvn test -Dtest=PastureStatusEngineTest

# Reporte de cobertura
mvn jacoco:report
# target/site/jacoco/index.html
```

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `PastureStatusEngine.test.java` con 15+ tests
- [ ] `TestFixtures.java` con builders y helpers
- [ ] `TESTING.md` con documentación
- [ ] Transición OPEN completamente cubierta
- [ ] Transición CLOSE completamente cubierta
- [ ] Auto-transiciones cubiertas
- [ ] Mantenimiento cubiertas (SET y CLEAR)
- [ ] Transiciones inválidas rechazadas
- [ ] ETA calculado correctamente
- [ ] Edge cases cubiertos (null, boundary, etc.)
- [ ] Casos sequential validados
- [ ] Cobertura >= 85% en PastureStatusEngine
- [ ] Todos los tests pasan
- [ ] Tests determinísticos (sin flakiness)
- [ ] Tests rápidos (< 5s total)
- [ ] Sin dependencias de BD o red
- [ ] Mocks adecuadamente configurados
- [ ] Documentación de casos edge
- [ ] Instrucciones para ejecutar tests
- [ ] CI/CD integrado (pasa en build)
- [ ] Code review aprobado (2 approvals)
- [ ] Sin warnings de linting
- [ ] Acceso a código es clara
- [ ] Fácil agregar nuevos tests

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
