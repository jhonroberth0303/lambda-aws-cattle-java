# 🌱 PASTURES-HU#9: Backend: Tests Unitarios EtaCalculator

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟠 ALTO (P1) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Boundary value analysis + Parametrized tests | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend developer**, quiero escribir tests unitarios completos para el `EtaCalculator`, de tal forma que:

1. Se valide la fórmula de cálculo de ETA con precisión
2. Se prueben todos los casos de uso (positivos, negativos, boundary)
3. Se validen especies con diferentes parámetros de crecimiento
4. Se manejen casos edge (valores nulos, infinito, cero, negativos)
5. Se garantice un mínimo de 85% de cobertura
6. Los tests sean rápidos, determinísticos y aislados
7. Se documente el comportamiento esperado del calculador

Esto habilitará que cambios futuros al calculador no rompan la lógica de rotación, y aumentará la confianza en las predicciones de disponibilidad.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Tests para ETA Positivo (Potrero en Descanso)
```gherkin
Scenario: Calcular ETA cuando potrero en descanso
  Given:
    | especie | KIKUYO |
    | altura residual | 15 cm |
    | altura mínima requerida | 25 cm |
    | velocidad crecimiento | 1.5 cm/día |
    | días descanso mínimo | 30 días |
    | días pasados desde cierre | 10 días |
  When se calcula ETA
  Then:
    [ ] ETA = MAX(30 - 10, (25 - 15) / 1.5)
    [ ] ETA = MAX(20, 6.67) = 21 días (aproximadamente 20-21)
    [ ] ETA > 0 (positivo)
    [ ] Sin errores
```

### AC#2: Tests para ETA Cero (Potrero Listo)
```gherkin
Scenario: Calcular ETA cuando potrero ya está listo
  Given:
    | altura residual | 25 cm |
    | altura mínima | 25 cm |
    | días descanso | 30 días |
    | días pasados | 30 días |
  When se calcula ETA
  Then:
    [ ] ETA = 0 (o negativo que se convierte a 0)
    [ ] etaMessage: "Disponible ahora"
    [ ] Potrero puede abrirse
```

### AC#3: Tests para ETA Negativo (Vencido)
```gherkin
Scenario: Calcular ETA cuando plazo ha vencido
  Given:
    | altura residual | 25 cm |
    | altura mínima | 20 cm |
    | días descanso | 30 días |
    | días pasados | 40 días (vencido hace 10)
  When se calcula ETA
  Then:
    [ ] ETA = -10 (calculado negativo)
    [ ] etaMessage: "Vencido hace 10 días"
    [ ] Debe ser ofrecido a operario para abrir
    [ ] Indica "sobrerreposo"
```

### AC#4: Tests para Diferentes Especies
```gherkin
Scenario: ETA varía según especie
  Given diferentes plantas:
    | Pasto | Min Rest | Min Height | Growth Rate |
    | KIKUYO | 30 días | 25 cm | 1.5 cm/día |
    | RAYGRASS | 25 días | 20 cm | 1.2 cm/día |
    | ANGLETON | 35 días | 30 cm | 1.0 cm/día |
  When se calcula ETA para cada una
  Then:
    [ ] KIKUYO: ETA rápido (bajo crecimiento)
    [ ] RAYGRASS: ETA intermedio
    [ ] ANGLETON: ETA lento (alto requerimiento)
    [ ] Cada una calcula correctamente
```

### AC#5: Tests para Crecimiento Lento
```gherkin
Scenario: Manejar especies de crecimiento muy lento
  Given:
    | altura residual | 10 cm |
    | altura mínima | 30 cm |
    | crecimiento | 0.2 cm/día |
    | descanso mínimo | 30 días |
  When se calcula ETA
  Then:
    [ ] Debe alcanzar altura: (30 - 10) / 0.2 = 100 días
    [ ] ETA = MAX(30, 100) = 100 días
    [ ] Calcula correctamente sin errores
    [ ] Captura el cuello de botella (altura >> descanso)
```

### AC#6: Tests para Altura Insuficiente
```gherkin
Scenario: Altura residual menor que mínima requerida
  Given:
    | altura residual | 5 cm |
    | altura mínima | 25 cm |
    | crecimiento | 2 cm/día |
    | descanso mínimo | 30 días |
  When se calcula ETA
  Then:
    [ ] Tiempo para altura: (25 - 5) / 2 = 10 días
    [ ] ETA = MAX(30, 10) = 30 días (descanso es cuello de botella)
    [ ] Correcto: altura se alcanza antes que descanso
```

### AC#7: Tests para Altura Cero o Negativa (Casos Edge)
```gherkin
Scenario: Manejar altura residual = 0 o negativa
  Given:
    | altura residual | 0 cm (o -5) |
    | altura mínima | 25 cm |
  When se calcula ETA
  Then:
    [ ] Trata 0 como "sin pasto"
    [ ] Calcula: (25 - 0) / growthRate
    [ ] Sin crash o excepción
    [ ] Retorna ETA válida o error explícito
```

### AC#8: Tests para Parámetros Nulos o Inválidos
```gherkin
Scenario: Manejar valores nulos o inválidos
  When altura residual = null
  Then:
    [ ] Lanza excepción descriptiva o retorna -1
    [ ] Error: "Altura residual requerida"
  
  When crecimiento = 0 o negativo
  Then:
    [ ] Lanza excepción o retorna infinito
    [ ] Error: "Crecimiento inválido"
  
  When plan = null
  Then:
    [ ] Lanza excepción
    [ ] Error: "Plan requerido"
```

### AC#9: Tests para Potrero Recién Cerrado
```gherkin
Scenario: Calcular ETA para potrero recién cerrado (día 0)
  Given:
    | días desde cierre | 0 días |
    | descanso mínimo | 30 días |
    | altura residual | 15 cm |
    | altura mínima | 25 cm |
    | crecimiento | 1.5 cm/día |
  When se calcula ETA
  Then:
    [ ] Descanso: MAX(30 - 0, (25-15)/1.5) = MAX(30, 6.67) = 30 días
    [ ] ETA = 30 días
    [ ] Correcto
```

### AC#10: Tests para Crecimiento Múltiple (Diferentes Rates)
```gherkin
Scenario: Validar cálculo con diferentes velocidades
  Given altura residual = 10, mínima = 25, descanso = 30
  When:
    | Crecimiento | Días para altura | ETA esperado |
    | 0.5 cm/día | 30 días | 30 (descanso) |
    | 1.0 cm/día | 15 días | 30 (descanso) |
    | 2.0 cm/día | 7.5 días | 30 (descanso) |
    | 5.0 cm/día | 3 días | 30 (descanso) |
  Then todos los cálculos correctos
```

### AC#11: Tests para Valores Muy Grandes (Boundary)
```gherkin
Scenario: Manejar valores muy grandes sin overflow
  Given:
    | altura residual | 1 cm |
    | altura mínima | 1000 cm (imaginario) |
    | crecimiento | 0.01 cm/día |
  When se calcula ETA
  Then:
    [ ] ETA = (1000 - 1) / 0.01 = 99,900 días
    [ ] Maneja correctamente sin overflow
    [ ] Retorna valor grande válido
```

### AC#12: Tests para Validar Mensaje de ETA
```gherkin
Scenario: Mensaje de ETA es descriptivo
  Given ETA = 5 días
  Then:
    [ ] etaMessage = "Disponible en 5 días" o similar
    [ ] Claro para operario
  
  Given ETA = 0
  Then:
    [ ] etaMessage = "Disponible ahora"
  
  Given ETA = -3
  Then:
    [ ] etaMessage = "Vencido hace 3 días"
    [ ] Indica que hay urgencia
```

### AC#13: Tests para Fórmula Compuesta
```gherkin
Scenario: Validar la fórmula completa
  # ETA = MAX(restDaysMin - daysSinceClosed, (minHeightRequired - residualHeight) / growthRate)
  
  Given:
    | restDaysMin | 30 |
    | daysSinceClosed | 10 |
    | minHeightRequired | 25 |
    | residualHeight | 15 |
    | growthRate | 1.5 |
  When se calcula ETA
  Then:
    [ ] Término A: 30 - 10 = 20
    [ ] Término B: (25 - 15) / 1.5 = 6.67 ≈ 7
    [ ] ETA = MAX(20, 7) = 20
    [ ] Sin errores
```

### AC#14: Tests para Redondeo de Decimales
```gherkin
Scenario: Manejar redondeo de decimales correctamente
  When ETA calculado = 6.67 días
  Then:
    [ ] Redondear a 7 días (ceil) o mantener decimal
    [ ] Decisión consistente en toda la app
    [ ] Documentado (ceil vs floor vs round)
```

### AC#15: Cobertura >= 85%
```gherkin
Scenario: Tests cubren toda la lógica de EtaCalculator
  When ejecuta coverage
  Then:
    [ ] Cobertura total: >= 85%
    [ ] Líneas no cubiertas: máximo 15%
    [ ] Branches: >= 80%
    [ ] Functions: >= 85%
    [ ] Statements: >= 85%
```

---

## 📊 **Especificación Técnica**

### Estructura de Tests

#### Test Suite - `EtaCalculator.test.java`

```java
@DisplayName("EtaCalculator - Cálculo de Disponibilidad")
class EtaCalculatorTest {
  
  private EtaCalculator calculator;
  private Plan plan;
  
  @BeforeEach
  void setUp() {
    calculator = new EtaCalculator();
    plan = createDefaultPlan(); // KIKUYO: 30 días, 25cm, 1.5 cm/día
  }
  
  // Tests para ETA positivo
  @Nested
  @DisplayName("ETA Positivo - Potrero en Descanso")
  class PositiveETATests { ... }
  
  // Tests para ETA cero
  @Nested
  @DisplayName("ETA Cero - Potrero Listo")
  class ZeroETATests { ... }
  
  // Tests para ETA negativo
  @Nested
  @DisplayName("ETA Negativo - Vencido")
  class NegativeETATests { ... }
  
  // Tests para diferentes especies
  @Nested
  @DisplayName("Diferentes Especies")
  class MultiSpeciesTests { ... }
  
  // Tests para casos edge
  @Nested
  @DisplayName("Casos Edge")
  class EdgeCaseTests { ... }
  
  // Tests para la fórmula completa
  @Nested
  @DisplayName("Fórmula Compuesta")
  class FormulaTests { ... }
}
```

### Casos de Prueba Detallados

#### ETA Positivo

```java
@Test
@DisplayName("ETA positivo: potrero en descanso normal")
void testPositiveETANormalRest() {
  // Arrange
  int residualHeight = 15;     // cm
  int minHeightRequired = 25;  // cm
  int minRestDays = 30;        // días
  int daysSinceClosed = 10;    // días
  double growthRate = 1.5;     // cm/día
  
  Plan plan = Plan.builder()
    .species("KIKUYO")
    .minHeightRequired(minHeightRequired)
    .growthRate(growthRate)
    .minRestDays(minRestDays)
    .build();
  
  Pasture pasture = Pasture.builder()
    .residualHeightCm(residualHeight)
    .lastClosedAt(LocalDateTime.now().minusDays(daysSinceClosed))
    .build();
  
  // Act
  ETAResult result = calculator.calculate(pasture, plan);
  
  // Assert
  // Término A: 30 - 10 = 20
  // Término B: (25 - 15) / 1.5 = 6.67 ≈ 7
  // ETA = MAX(20, 7) = 20
  assertThat(result.getEta()).isEqualTo(20);
  assertThat(result.getEtaMessage()).contains("20 días");
  assertTrue(result.isValid());
}

@Test
@DisplayName("ETA cuando altura es cuello de botella")
void testETAHeightIsBottleneck() {
  // Cuando crecimiento lento, altura domina el cálculo
  Pasture pasture = Pasture.builder()
    .residualHeightCm(10)
    .lastClosedAt(LocalDateTime.now().minusDays(5))
    .build();
  
  Plan plan = Plan.builder()
    .minHeightRequired(30)
    .minRestDays(30)
    .growthRate(0.5) // muy lento
    .build();
  
  ETAResult result = calculator.calculate(pasture, plan);
  
  // Descanso: 30 - 5 = 25 días
  // Altura: (30 - 10) / 0.5 = 40 días
  // ETA = MAX(25, 40) = 40
  assertThat(result.getEta()).isEqualTo(40);
}

@Test
@DisplayName("ETA cuando descanso es cuello de botella")
void testETARestIsBottleneck() {
  Pasture pasture = Pasture.builder()
    .residualHeightCm(20)
    .lastClosedAt(LocalDateTime.now().minusDays(5))
    .build();
  
  Plan plan = Plan.builder()
    .minHeightRequired(25)
    .minRestDays(30)
    .growthRate(5.0) // muy rápido
    .build();
  
  ETAResult result = calculator.calculate(pasture, plan);
  
  // Descanso: 30 - 5 = 25 días
  // Altura: (25 - 20) / 5.0 = 1 día
  // ETA = MAX(25, 1) = 25
  assertThat(result.getEta()).isEqualTo(25);
}
```

#### ETA Cero

```java
@Test
@DisplayName("ETA cero: potrero está listo")
void testZeroETAReady() {
  Pasture pasture = Pasture.builder()
    .residualHeightCm(25)
    .lastClosedAt(LocalDateTime.now().minusDays(30))
    .build();
  
  Plan plan = Plan.builder()
    .minHeightRequired(25)
    .minRestDays(30)
    .growthRate(1.5)
    .build();
  
  ETAResult result = calculator.calculate(pasture, plan);
  
  assertThat(result.getEta()).isEqualTo(0);
  assertThat(result.getEtaMessage()).containsIgnoringCase("disponible");
}

@Test
@DisplayName("ETA cero cuando ambos criterios se cumplen")
void testZeroETABothMet() {
  Pasture pasture = Pasture.builder()
    .residualHeightCm(30) // Mayor que mínimo
    .lastClosedAt(LocalDateTime.now().minusDays(35)) // Mayor que mínimo
    .build();
  
  Plan plan = Plan.builder()
    .minHeightRequired(25)
    .minRestDays(30)
    .growthRate(1.5)
    .build();
  
  ETAResult result = calculator.calculate(pasture, plan);
  
  // Descanso: 30 - 35 = -5 → 0
  // Altura: (25 - 30) / 1.5 = -3.33 → 0
  // ETA = MAX(0, 0) = 0
  assertThat(result.getEta()).isEqualTo(0);
}
```

#### ETA Negativo

```java
@Test
@DisplayName("ETA negativo: potrero vencido")
void testNegativeETAExpired() {
  Pasture pasture = Pasture.builder()
    .residualHeightCm(30)
    .lastClosedAt(LocalDateTime.now().minusDays(40))
    .build();
  
  Plan plan = Plan.builder()
    .minHeightRequired(25)
    .minRestDays(30)
    .growthRate(1.5)
    .build();
  
  ETAResult result = calculator.calculate(pasture, plan);
  
  // ETA = 30 - 40 = -10 (vencido hace 10 días)
  assertThat(result.getEta()).isLessThan(0);
  assertThat(result.getEtaMessage()).containsIgnoringCase("vencido");
}
```

#### Diferentes Especies

```java
@Test
@DisplayName("ETA varía según especie")
void testETADifferentSpecies() {
  Pasture pasture = Pasture.builder()
    .residualHeightCm(15)
    .lastClosedAt(LocalDateTime.now().minusDays(10))
    .build();
  
  // KIKUYO: 30 días, 25cm, 1.5 cm/día
  Plan kikuyo = Plan.builder()
    .species("KIKUYO")
    .minRestDays(30)
    .minHeightRequired(25)
    .growthRate(1.5)
    .build();
  
  // RAYGRASS: 25 días, 20cm, 1.2 cm/día
  Plan raygrass = Plan.builder()
    .species("RAYGRASS")
    .minRestDays(25)
    .minHeightRequired(20)
    .growthRate(1.2)
    .build();
  
  ETAResult etaKikuyo = calculator.calculate(pasture, kikuyo);
  ETAResult etaRaygrass = calculator.calculate(pasture, raygrass);
  
  // Ambas válidas pero diferentes
  assertTrue(etaKikuyo.isValid());
  assertTrue(etaRaygrass.isValid());
  assertNotEquals(etaKikuyo.getEta(), etaRaygrass.getEta());
}
```

#### Casos Edge

```java
@Test
@DisplayName("Altura residual cero")
void testZeroResidualHeight() {
  Pasture pasture = Pasture.builder()
    .residualHeightCm(0)
    .lastClosedAt(LocalDateTime.now())
    .build();
  
  Plan plan = Plan.builder()
    .minHeightRequired(25)
    .minRestDays(30)
    .growthRate(1.5)
    .build();
  
  ETAResult result = calculator.calculate(pasture, plan);
  
  // (25 - 0) / 1.5 = 16.67 días
  assertTrue(result.isValid());
  assertTrue(result.getEta() > 0);
}

@Test
@DisplayName("Parámetro nulo: plan")
void testNullPlan() {
  Pasture pasture = Pasture.builder()
    .residualHeightCm(15)
    .build();
  
  ETAResult result = calculator.calculate(pasture, null);
  
  assertFalse(result.isValid());
  assertThat(result.getError()).contains("Plan");
}

@Test
@DisplayName("Parámetro nulo: pasture")
void testNullPasture() {
  Plan plan = createDefaultPlan();
  
  ETAResult result = calculator.calculate(null, plan);
  
  assertFalse(result.isValid());
  assertThat(result.getError()).contains("Pasture");
}

@Test
@DisplayName("Crecimiento negativo o cero")
void testInvalidGrowthRate() {
  Pasture pasture = Pasture.builder()
    .residualHeightCm(15)
    .build();
  
  Plan planZeroGrowth = Plan.builder()
    .minHeightRequired(25)
    .minRestDays(30)
    .growthRate(0) // Inválido
    .build();
  
  ETAResult result = calculator.calculate(pasture, planZeroGrowth);
  
  assertFalse(result.isValid());
}

@Test
@DisplayName("Valores muy grandes sin overflow")
void testLargeValues() {
  Pasture pasture = Pasture.builder()
    .residualHeightCm(1)
    .lastClosedAt(LocalDateTime.now())
    .build();
  
  Plan plan = Plan.builder()
    .minHeightRequired(1000)
    .minRestDays(30)
    .growthRate(0.01)
    .build();
  
  ETAResult result = calculator.calculate(pasture, plan);
  
  // (1000 - 1) / 0.01 = 99,900 días
  assertTrue(result.isValid());
  assertTrue(result.getEta() > 0);
  assertTrue(result.getEta() < Integer.MAX_VALUE);
}
```

#### Fórmula Compuesta

```java
@Test
@DisplayName("Validar fórmula completa")
void testCompleteFormula() {
  // ETA = MAX(restDaysMin - daysSinceClosed, (minHeight - residual) / growthRate)
  
  Pasture pasture = Pasture.builder()
    .residualHeightCm(15)
    .lastClosedAt(LocalDateTime.now().minusDays(10))
    .build();
  
  Plan plan = Plan.builder()
    .minHeightRequired(25)
    .minRestDays(30)
    .growthRate(1.5)
    .build();
  
  ETAResult result = calculator.calculate(pasture, plan);
  
  // Término A: 30 - 10 = 20
  // Término B: (25 - 15) / 1.5 = 6.67
  // ETA = MAX(20, 6.67) = 20
  assertEquals(20, result.getEta());
}

@Test
@DisplayName("Parametrizado: múltiples casos")
@ParameterizedTest
@CsvSource({
  // restMin, daysSince, minHeight, residual, growthRate, expected
  "30, 10, 25, 15, 1.5, 20", // MAX(20, 7) = 20
  "30, 30, 25, 15, 1.5, 7",  // MAX(0, 7) = 7
  "30, 35, 25, 15, 1.5, 7",  // MAX(-5→0, 7) = 7
  "30, 10, 25, 25, 1.5, 20", // MAX(20, 0) = 20
  "30, 40, 25, 30, 1.5, 0",  // MAX(-10→0, -3.3→0) = 0
})
void testFormulaParametrized(
  int restMin, int daysSince, int minHeight, 
  int residual, double growthRate, int expected
) {
  Pasture pasture = Pasture.builder()
    .residualHeightCm(residual)
    .lastClosedAt(LocalDateTime.now().minusDays(daysSince))
    .build();
  
  Plan plan = Plan.builder()
    .minHeightRequired(minHeight)
    .minRestDays(restMin)
    .growthRate(growthRate)
    .build();
  
  ETAResult result = calculator.calculate(pasture, plan);
  
  assertEquals(expected, result.getEta());
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`EtaCalculator.test.java`**
   - Test suite principal (15+ métodos)
   - Tests parametrizados
   - Cobertura >= 85%

2. **`ETATestFixtures.java`** (helper)
   - Builders para Plan, Pasture
   - Métodos de conveniencia
   - Data de prueba común

### Archivos a Verificar

1. **`EtaCalculator.java`**
   - Asegurar cálculo es correcto
   - Validar manejos de edge cases

2. **`ETAResult.java`** o similar
   - DTO para resultado
   - Mensaje descriptivo

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear fixtures
```java
public class ETATestFixtures {
  public static Plan createDefaultPlan() {
    return Plan.builder()
      .species("KIKUYO")
      .minHeightRequired(25)
      .minRestDays(30)
      .growthRate(1.5)
      .build();
  }
}
```

### Paso 2: Tests para ETA positivo
```java
@Nested
class PositiveETATests {
  @Test
  void testNormalRest() { ... }
  
  @Test
  void testHeightBottleneck() { ... }
}
```

### Paso 3: Tests para ETA cero
```java
@Nested
class ZeroETATests {
  @Test
  void testReady() { ... }
}
```

### Paso 4: Tests para ETA negativo
```java
@Nested
class NegativeETATests {
  @Test
  void testExpired() { ... }
}
```

### Paso 5: Tests para especies
```java
@Nested
class MultiSpeciesTests {
  @Test
  void testDifferentSpecies() { ... }
}
```

### Paso 6: Tests edge cases
```java
@Nested
class EdgeCaseTests {
  @Test
  void testZeroHeight() { ... }
  
  @Test
  void testNullPlan() { ... }
  
  @Test
  void testLargeValues() { ... }
}
```

### Paso 7: Tests parametrizados
```java
@ParameterizedTest
@CsvSource({ ... })
void testFormulaParametrized(...) { ... }
```

---

## 🧪 **Cobertura de Tests**

**Métodos Críticos** en EtaCalculator:

```
- calculate(Pasture, Plan) ..................... 100%
  └─ validateInputs() ......................... 100%
  └─ calculateRestTerms() ..................... 100%
  └─ calculateHeightTerms() ................... 100%
  └─ computeMax() ............................ 100%
  └─ formatMessage() .......................... 95%
```

**Líneas No Cubiertas** (máximo 15%):
- Logs informativos
- Código defensivo raramente ejecutable
- Branches de excepciones

---

## 🔄 **Escenarios de Prueba (BDD)**

### Escenario 1: ETA Normal Positivo
```gherkin
Scenario: Calcular ETA para potrero en descanso
  Given potrero con altura residual 15cm
  And plan KIKUYO (30 días, 25cm mín, 1.5 cm/día)
  And 10 días desde cierre
  When se calcula ETA
  Then ETA = 20 días (MAX descanso vs altura)
```

### Escenario 2: ETA Cero (Listo)
```gherkin
Scenario: Potrero listo para abrir
  Given altura >= mínima requerida
  And descanso >= mínimo
  When se calcula ETA
  Then ETA = 0 y mensaje "Disponible"
```

### Escenario 3: ETA Negativo (Vencido)
```gherkin
Scenario: Potrero vencido (sobrerreposo)
  Given 40 días desde cierre con descanso = 30
  When se calcula ETA
  Then ETA = -10 días y mensaje "Vencido hace 10"
```

---

## 📚 **Referencias y Dependencias**

**Dependencias**:
- JUnit 5
- Mockito (si es necesario)
- AssertJ (recomendado)

**Componentes relacionados**:
- EtaCalculator.java
- Plan, Pasture entities
- ETAResult DTO

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Mathematical Property Testing + Boundary Value Analysis + Parametrized Tests

**Justificación:** **Fórmula crítica**: ETA = MAX(restDaysMin - daysSinceClosed, (minHeight - residualHeight) / growthRate). **Boundary testing**: Casos edge (altura 0, crecimiento lento, vencido). **Especies variadas**: Diferentes combinaciones parámetros. **Determinístico**: Sin dependencias time real. **85% coverage**: Cobertura completa rama.

**Componentes a Testear:**

- **EtaCalculator.java (existente):** Método: `calculateEta(pasture, plan, now) → Integer`. Entrada: species, residualHeight, daysSinceClosed, plan params. Salida: ETA days (0, positivo, o negativo si vencido).

**Test Casos Principales:**
- ETA positivo: Potrero en descanso, requiere días + altura
- ETA cero: Potrero listo ahora
- ETA negativo: Vencido hace X días
- Diferentes especies: KIKUYO vs RAYGRASS vs ANGLETON
- Crecimiento lento: Cuello de botella altura
- Altura insuficiente: MAX(rest, height)
- Edge cases: altura 0, crecimiento 0, null values

**Fórmula Validada:**
```
daysSinceClosed = now - lastClosedAt
daysToHeight = (minHeight - residualHeight) / growthRate
ETA = MAX(restDaysMin - daysSinceClosed, daysToHeight)

Si ETA < 0 → "Vencido hace X días"
Si ETA == 0 → "Disponible ahora"
Si ETA > 0 → "Disponible en X días"
```

**Hitos:**
1. Tests para ETA positivo (normal rest)
2. Tests para ETA cero (listo)
3. Tests para ETA negativo (vencido)
4. Tests para diferentes especies
5. Tests para edge cases (0, negativo, null)
6. Cobertura 85%+ verificada

### Validación de Impacto

✅ **Fórmula Matemática Validada:**
- Casos normales cubiertos
- Boundary cases testeados
- Edge cases (0, negativo) manejados

✅ **Especies Variadas Soportadas:**
- KIKUYO, RAYGRASS, ANGLETON, etc.
- Parámetros diferentes capturados
- Tests parametrizados

✅ **Cobertura 85%+:**
- Rama coverage completo
- Sin crashes con inputs extremos
- Determinístico

### Notas Técnicas

**Parametrized Test - Estructura:**
```java
@ParameterizedTest
@CsvSource({
  "15,25,1.5,30,10,20",  // altura res, min, growth, rest, days, expected=21
  "25,25,1.5,30,30,0",   // listo ahora
  "25,20,1.5,30,40,-10", // vencido
})
void testEtaCalculation(int res, int min, double growth, int rest, int days, int expected) {
  Pasture p = createPasture(res, days);
  Plan plan = createPlan(min, growth, rest);
  int eta = calculator.calculateEta(p, plan, NOW);
  assertEquals(expected, eta);
}
```

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-001: Backend POST Eventos (transiciones)
- ✅ PASTURES-HU-008: Backend Tests Engine (state machine)
- → PASTURES-HU-009: Backend Tests ETA (esta - mathematical testing)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Boundary value analysis + parametrized tests (cobertura 85%+)

---

## 🔧 **Refinamiento Técnico**

### Mathematical Property Testing - Boundary Values

```java
@ParameterizedTest
@CsvSource({
  // currentHeight, minHeight, growthRate, restDays, expectedETA
  "25,20,2.5,30,2",        // Casi listo
  "20,20,2.5,30,0",        // Exactamente listo
  "19,20,2.5,30,-1",       // Vencido (negativo)
  "10,20,2.5,30,4",        // Medio camino
  "5,20,2.5,30,6",         // Recién empezó
  "0,20,2.5,30,8",         // Altura cero (edge)
  "25,20,0.1,30,50",       // Crecimiento lento
})
void testETACalculation(int currentHeight, int minHeight, 
    double growthRate, int restDays, int expectedETA) {
  
  RotationPlan plan = RotationPlan.builder()
    .minHeight(minHeight)
    .growthRate(growthRate)
    .restDays(restDays)
    .build();
  
  int actualETA = etaCalculator.calculateETA(
    currentHeight, plan
  );
  
  assertEquals(expectedETA, actualETA);
}
```

### Test Cases - Boundary Analysis

```java
@Test
void testETAWithZeroHeight() {
  // Edge case: altura cero
  int eta = etaCalculator.calculateETA(0, standardPlan);
  assertEquals(8, eta);  // Debe recuperarse en 8 días
}

@Test
void testETAAlreadyAvailable() {
  // ETA = 0 cuando altura >= minHeight
  int eta = etaCalculator.calculateETA(25, planWithMin20);
  assertEquals(0, eta);
}

@Test
void testETANegativeWhenVencido() {
  // ETA negativo cuando vencido (altura > min)
  int eta = etaCalculator.calculateETA(30, planWithMin20);
  assertTrue(eta < 0);
}

@Test
void testETAWithSlowGrowth() {
  // Crecimiento lento = ETA mayor
  int eta = etaCalculator.calculateETA(10, planWithSlowGrowth);
  assertTrue(eta > 5);  // Más de 5 días
}
```

### Test Coverage Goals

- **Boundary Values**: Min, max, zero, negative
- **Growth Rates**: Normal, slow, fast
- **Height Scenarios**: Too low, exact min, above min
- **Recovery Times**: Various rest periods
- **Target**: 85%+ cobertura

### Testing Command

```bash
mvn test -Dtest=EtaCalculatorTest
mvn jacoco:report
```

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `EtaCalculator.test.java` con 15+ tests
- [ ] `ETATestFixtures.java` con builders
- [ ] Test para ETA positivo (normal rest)
- [ ] Test para ETA cero (listo)
- [ ] Test para ETA negativo (vencido)
- [ ] Tests para diferentes especies
- [ ] Tests para crecimiento lento
- [ ] Tests para altura insuficiente
- [ ] Tests para altura cero/negativa
- [ ] Tests para valores nulos/inválidos
- [ ] Tests para potrero recién cerrado
- [ ] Tests para diferentes velocidades
- [ ] Tests para valores muy grandes
- [ ] Tests para mensajes de ETA
- [ ] Tests para fórmula completa
- [ ] Tests parametrizados
- [ ] Cobertura >= 85% en EtaCalculator
- [ ] Todos los tests pasan
- [ ] Tests determinísticos
- [ ] Tests performantes (< 5s)
- [ ] Sin dependencias externas
- [ ] Documentación clara
- [ ] Fácil agregar nuevos tests
- [ ] Code review aprobado
- [ ] Sin warnings de linting
- [ ] CI/CD integrado (pasa en build)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
