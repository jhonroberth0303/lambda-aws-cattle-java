# 🧪 HU-BEDROCK-002: Testing & QA - Suite de Tests Completa

**ID**: HU-BEDROCK-002  
**Prioridad**: 🟠 ALTA  
**Estimación**: 8 puntos  
**Sprint**: S-2 (Enero 2026)  
**Estado**: ✅ Refinado (Developer)  
**Dependencia**: HU-BEDROCK-001 (implementación)  

---

## 📋 Descripción

Como **QA Engineer**, quiero **implementar una suite de tests completa** (unitarios, integración, seguridad) con **cobertura ≥ 85%** para **validar que el chatbot cumple todos los criterios de aceptación** y **no contiene bugs críticos o vulnerabilidades**.

---

## 🎯 Criterios de Aceptación

### CA-001: Tests Unitarios ✅

```gherkin
Scenario: Ejecutar 35+ tests unitarios con cobertura completa
  Given las 8 clases principales implementadas
  When ejecuto: mvn test
  Then todos los tests pasan ✓
  And cobertura mínima: 85%
  And tiempo ejecución < 30 segundos
```

**Tests Unitarios Incluidos**:
- IntentDetectionService (8 tests)
- CattleQueryService (7 tests)
- ContextBuilderService (6 tests)
- BedrockService (5 tests)
- ChatbotService (6 tests)
- ChatbotController (3 tests)
- Error handling (2 tests)
- Rate limiting (2 tests)

### CA-002: Tests de Integración ✅

```gherkin
Scenario: Tests de integración con servicios reales
  Given Lambda con DynamoDB local (LocalStack)
  When ejecuto: mvn verify
  Then todos los tests de integración pasan
  And incluyen: DynamoDB + Bedrock mock
  And validar flujo end-to-end
```

**Tests de Integración** (8 tests):
- IntentDetection → CattleQuery → ContextBuilder
- ChatbotService completo
- Error handling end-to-end
- Rate limiting integration
- JWT validation + DynamoDB
- LocalStack setup validation
- Lambda cold start < 3s
- Response formatting end-to-end

### CA-003: Tests de Seguridad ✅

```gherkin
Scenario: Validar seguridad del chatbot
  Given las medidas de seguridad implementadas
  When ejecuto tests de seguridad
  Then todos los tests pasan
  And valida: JWT, CORS, rate limiting, input sanitization
  And 0 vulnerabilidades críticas encontradas
```

**Tests de Seguridad** (12 tests):
- JWT token validation (3 tests)
- JWT token expiration (2 tests)
- Unauthorized access rejection (2 tests)
- Input sanitization (2 tests)
- Rate limiting enforcement (2 tests)
- CORS policy validation (1 test)

### CA-004: Cobertura de Código ✅

```gherkin
Scenario: Validar cobertura mínima de código
  Given todos los tests implementados
  When generan reporte de cobertura (Jacoco)
  Then cobertura líneas >= 85%
  And cobertura ramas >= 80%
  And cobertura métodos >= 90%
  And reporte publicado en /target/site
```

### CA-005: Performance Testing ✅

```gherkin
Scenario: Validar performance bajo carga
  Given Lambda con 100 requests simultáneos
  When ejecuto performance tests (JMeter/Gatling)
  Then p99 latency < 500ms
  And p95 latency < 300ms
  And error rate < 1%
  And DynamoDB queries < 200ms
```

### CA-006: Regresión Testing ✅

```gherkin
Scenario: Detectar regresiones en nuevos cambios
  Given test suite anterior
  When se hacen cambios a servicios
  Then re-ejecuto todos los tests
  And todos pasan igual que antes
  And cobertura se mantiene ≥ 85%
  And no hay nuevas vulnerabilidades
```

### CA-007: Mock de Servicios Externos ✅

```gherkin
Scenario: Tests con Bedrock mocked
  Given BedrockService mockeado
  When ejecuto tests unitarios
  Then no invoca Bedrock real
  And ejecuta en < 1 segundo
  And predecible y repetible
```

### CA-008: Documentación de Tests ✅

```gherkin
Scenario: Documentación clara de tests
  Given todos los tests implementados
  When reviso el código
  Then cada test tiene comentario explicativo
  And README.md incluye cómo ejecutar tests
  And hay matriz de cobertura en wiki
```

---

## 📊 Suite de Tests (47 Total)

```
┌─────────────────────────────────┐
│   TESTS UNITARIOS: 35           │
├─────────────────────────────────┤
│ IntentDetectionService    │ 8   │
│ CattleQueryService        │ 7   │
│ ContextBuilderService     │ 6   │
│ BedrockService            │ 5   │
│ ChatbotService            │ 6   │
│ ChatbotController         │ 3   │
│ Utilidades/Helpers        │ 2   │
│ Error Handling            │ 2   │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│   TESTS INTEGRACIÓN: 8          │
├─────────────────────────────────┤
│ End-to-End Flow           │ 3   │
│ DynamoDB + Bedrock        │ 3   │
│ Rate Limiting             │ 1   │
│ JWT Validation            │ 1   │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│   TESTS SEGURIDAD: 12           │
├─────────────────────────────────┤
│ JWT Token                 │ 5   │
│ Unauthorized Access       │ 2   │
│ Input Sanitization        │ 2   │
│ Rate Limiting             │ 2   │
│ CORS Policy               │ 1   │
└─────────────────────────────────┘

   TOTAL: 47 TESTS
```

---

## 🛠️ Herramientas & Frameworks

| Herramienta | Uso |
|---|---|
| **JUnit 5** | Test framework |
| **Mockito** | Mock de servicios |
| **AssertJ** | Fluent assertions |
| **Spring Test** | Testing Spring Boot |
| **LocalStack** | DynamoDB local |
| **Jacoco** | Cobertura de código |
| **JMeter/Gatling** | Performance testing |
| **AWS SDK Mock** | Mock de Bedrock |

---

## 📋 Implementación por Fase

### Fase 1: Setup Testing (Día 1-2)

- [ ] Configurar JUnit 5 + Mockito
- [ ] Setup LocalStack (Docker)
- [ ] Configurar Jacoco
- [ ] Base de tests creada

### Fase 2: Tests Unitarios (Día 3-5)

- [ ] 35 tests unitarios implementados
- [ ] Cobertura validada ≥ 85%
- [ ] Mock de servicios externos

### Fase 3: Tests Integración (Día 6-7)

- [ ] 8 tests de integración
- [ ] End-to-end flow validado
- [ ] LocalStack funcionando

### Fase 4: Tests Seguridad (Día 8)

- [ ] 12 tests de seguridad
- [ ] Validación de vulnerabilidades
- [ ] Reporte de seguridad

### Fase 5: Performance & Finales (Día 9-10)

- [ ] Performance testing
- [ ] Reporte final de cobertura
- [ ] Documentación completa

---

## Análisis Arquitectónico (Arquitecto)

### Decisiones de Diseño

**Patrón Arquitectónico:** Test Pyramid Architecture (Pirámide de Testing) con AAA Pattern (Arrange-Act-Assert)

**Justificación:** Este patrón se seleccionó por establecer proporciones óptimas de testing: base amplia de 35 tests unitarios (74%) que son rápidos, aislados y proporcionan alta cobertura; capa media de 8 tests de integración (17%) que validan interacciones reales; y cima especializada de 4 tests de seguridad (9%) que validan requisitos no funcionales críticos. El AAA Pattern (Arrange-Act-Assert) proporciona legibilidad y mantenibilidad al estructurar cada test en tres fases claras: setup de datos y mocks, ejecución de la acción bajo test, y verificación de resultados esperados. Este patrón es coherente con la arquitectura existente de cattle-lambda-function que ya utiliza JUnit 5 + Mockito con patrón AAA verificado, se alinea con herramientas del proyecto (build.gradle ya tiene JUnit 5, Mockito 5.2.0, Jacoco 0.8.11), y está optimizado para CI/CD permitiendo tests rápidos ejecutables en < 5 minutos para feedback rápido en pipeline.

**Componentes Afectados:**

- **IntentDetectionServiceTest (Nuevo):** Suite de 8 tests unitarios para validar detección de intenciones. Validar detección correcta de intenciones (COUNT_BOVINES, COUNT_BY_STATUS, etc.), extracción de entidades (categoría, género), manejo de queries ambiguas. Usar @ExtendWith(MockitoExtension.class), sin dependencias reales, mocks de todas las colaboraciones. Tests incluyen: detectIntent_countBovines_success, detectIntent_countByCategory_success, detectIntent_pregnantCows_success, detectIntent_ambiguousQuery_fallback, extractEntity_category_cow, extractEntity_gender_female, detectIntent_unknownQuery_returnsGeneral, detectIntent_emptyMessage_throwsException.

- **BovineQueryServiceTest (Nuevo):** Suite de 7 tests unitarios para queries de bovinos. Validar queries a DynamoDB mockeado, verificar uso correcto de GSI, validar agregaciones y filtros. Mock DynamoDbEnhancedClient y DynamoDbTable. Tests: countAllBovines_success, countByCategory_multipleCows, countByGender_femalesOnly, countPregnantBovines_filtersCorrectly, getAgeDistribution_calculatesAges, getCalvesForWeaning_filtersAge, queryBovines_dynamoDbException_throwsServiceException.

- **MilkingQueryServiceTest (Nuevo):** Suite de 5 tests unitarios para lactancia. Validar queries de lactancia, cálculos de promedio, identificación de top producer. Mock repositories y entities. Tests: getMonthlyAverageProduction_calculatesCorrectly, getTopProducerBovine_identifiesMax, queryMilking_emptyResults_returnsZero, queryMilking_invalidDateRange_throwsException, aggregateProduction_multipleShifts_sumsCorrectly.

- **PastureQueryServiceTest (Nuevo):** Suite de 4 tests unitarios para potreros. Validar filtros de estado de potreros, cálculo de hectáreas. Mock repository pasture. Tests: getAvailablePastures_filtersDisponible, getTotalHectaresInUse_sumsCorrectly, queryPastures_noAvailable_returnsEmpty, calculateAvailability_multipleStatuses_aggregatesCorrectly.

- **ContextBuilderServiceTest (Nuevo):** Suite de 6 tests unitarios para construcción de contexto. Validar construcción de contexto formateado, orquestación de múltiples query services, formateo de texto para Bedrock. Mock BovineQueryService, MilkingQueryService, PastureQueryService. Tests: buildContext_countIntent_includesStats, buildContext_pregnancyIntent_includesPregnancyData, buildContext_milkingIntent_includesProduction, buildContext_multipleIntents_combinesData, formatContext_generatesReadableText, buildContext_serviceException_handlesGracefully.

- **BedrockServiceTest (Nuevo):** Suite de 5 tests unitarios para cliente Bedrock. Validar invocación de Bedrock, construcción de payload JSON, parsing de respuesta, timeout handling. Mock BedrockRuntimeClient. Tests: invokeModel_validPrompt_returnsResponse, invokeModel_constructsCorrectPayload, parseResponse_extractsText, invokeModel_timeout_throwsException, invokeModel_accessDenied_throwsAuthException.

- **ChatbotServiceTest (Nuevo):** Suite de 6 tests unitarios para orquestador principal. Validar orquestación completa del flujo, integración de servicios, error handling, logging, rate limiting. Mock IntentDetectionService, ContextBuilderService, BedrockService. Tests: chat_fullFlow_success, chat_intentDetection_invokesCorrectly, chat_contextBuilding_passesCorrectData, chat_bedrockInvocation_receivesPrompt, chat_serviceException_logsAndThrows, chat_rateLimitExceeded_throwsException.

- **ChatbotControllerTest (Nuevo):** Suite de 3 tests unitarios para controlador REST. Validar endpoint REST, extracción JWT, validación de request, formateo de response. Usar @WebMvcTest(ChatbotController.class), MockMvc. Tests: sendMessage_validRequest_returns200, sendMessage_invalidJWT_returns401, sendMessage_serviceError_returns500.

- **ErrorHandlingTest (Nuevo):** Suite de 2 tests unitarios para manejo de errores. Validar manejo de excepciones, logs de error, no exposición de stack traces. Tests globales de error handling: handleRepositoryException_doesNotExposeStackTrace, handleServiceException_logsCorrectly.

- **RateLimitingTest (Nuevo):** Suite de 2 tests unitarios para rate limiting. Validar límite de requests por usuario, respuesta 429, headers de rate limit. Tests: rateLimiting_exceedsLimit_returns429, rateLimiting_withinLimit_allowsRequest.

- **ChatbotIntegrationTest (Nuevo):** Suite de 3 tests de integración end-to-end. Validar flujo end-to-end con DynamoDB local (LocalStack) y Bedrock mock. Usar @SpringBootTest con LocalStack container, TestContainers. Tests: endToEndFlow_countBovines_success, endToEndFlow_pregnancyQuery_integratesServices, endToEndFlow_withRealDynamoDB_queriesCorrectly.

- **DynamoDBIntegrationTest (Nuevo):** Suite de 3 tests de integración con DynamoDB. Validar queries reales a DynamoDB local, GSI usage, data persistence. LocalStack/TestContainers con tablas creadas. Tests: bovineRepository_saveAndQuery_success, milkingRepository_queryByGSI_returnsData, pastureRepository_updateStatus_persists.

- **SecurityIntegrationTest (Nuevo):** Test de integración de seguridad. Validar JWT + Rate limiting integrado. JWT token real generado. Test: jwtValidation_withRateLimiting_enforcesCorrectly.

- **PerformanceIntegrationTest (Nuevo):** Test de integración de performance. Validar latencia < 500ms, cold start < 3s. Medición de performance con múltiples requests. Test: performance_p99Latency_underThreshold.

- **JWTValidationTest (Nuevo):** Suite de 5 tests de seguridad para JWT. Validar autenticación JWT completa. Tokens válidos/inválidos/expirados. Tests: jwtValidation_validToken_allows, jwtValidation_expiredToken_rejects, jwtValidation_invalidSignature_rejects, jwtValidation_missingToken_returns401, jwtValidation_extractsFarmId_correctly.

- **UnauthorizedAccessTest (Nuevo):** Suite de 2 tests de seguridad para acceso no autorizado. Tests: unauthorizedAccess_noAuthHeader_rejects, unauthorizedAccess_wrongFarmId_rejects.

- **InputSanitizationTest (Nuevo):** Suite de 2 tests de seguridad para sanitización. Tests: inputSanitization_sqlInjection_sanitizes, inputSanitization_scriptInjection_sanitizes.

- **RateLimitingSecurityTest (Nuevo):** Suite de 2 tests de seguridad para rate limiting. Tests: rateLimiting_enforcesLimit_blocks, rateLimiting_headersProvided_correctly.

- **CORSPolicyTest (Nuevo):** Test de seguridad para CORS. Test: corsPolicy_allowedOrigins_configured.

- **TestConfiguration (Nuevo):** Configuración base para todos los tests. Configuración base para todos los tests, beans de testing, mocks compartidos. Anotación @TestConfiguration con beans: MockBedrockClient, TestDataBuilder, MockJWTGenerator.

- **TestDataBuilder (Nuevo):** Builder pattern para datos de test. Builder pattern para crear datos de test reutilizables (Bovines, Milkings, Pastures). Métodos estáticos: createBovine, createMilking, createPasture, createIntent, createChatRequest.

- **LocalStackTestContainer (Nuevo):** Configuración de LocalStack para DynamoDB local. Configuración de LocalStack para DynamoDB local en tests de integración. Singleton container, auto-start/stop, tablas pre-creadas (TABLE_CATTLE, TABLE_FARM_MILKING, TABLE_PASTURE).

- **MockBedrockClient (Nuevo):** Mock de BedrockRuntimeClient. Mock predecible de BedrockRuntimeClient para tests sin invocar AWS. Respuestas predefinidas según prompt, simulación de timeout/errores.

**Hitos de Implementación:**

1. **Infraestructura de Testing Base** - Setup de herramientas y configuración: TestConfiguration, TestDataBuilder, MockBedrockClient, LocalStackTestContainer
   - Dependencias: build.gradle existente, Docker para LocalStack

2. **Tests Unitarios de Servicios de Datos** - Capa de acceso a datos: BovineQueryServiceTest, MilkingQueryServiceTest, PastureQueryServiceTest
   - Dependencias: TestDataBuilder del hito 1, mocks de repositories

3. **Tests Unitarios de Servicios de Negocio** - Lógica core: IntentDetectionServiceTest, ContextBuilderServiceTest, BedrockServiceTest
   - Dependencias: TestDataBuilder, MockBedrockClient del hito 1, tests de datos del hito 2

4. **Tests Unitarios de Orquestación** - Flujo principal: ChatbotServiceTest, ChatbotControllerTest
   - Dependencias: Todos los mocks de servicios previos

5. **Tests Unitarios de Cross-Cutting Concerns** - Aspectos transversales: ErrorHandlingTest, RateLimitingTest
   - Dependencias: ChatbotService, ChatbotController del hito 4

6. **Tests de Integración con DynamoDB** - Persistencia real: DynamoDBIntegrationTest
   - Dependencias: LocalStackTestContainer del hito 1, repositories reales

7. **Tests de Integración End-to-End** - Flujo completo: ChatbotIntegrationTest, PerformanceIntegrationTest
   - Dependencias: LocalStack del hito 6, todos los servicios reales excepto Bedrock (mock)

8. **Tests de Seguridad** - Validación de seguridad: JWTValidationTest, UnauthorizedAccessTest, InputSanitizationTest, RateLimitingSecurityTest, CORSPolicyTest
   - Dependencias: ChatbotController, SecurityConfig, flujo completo del hito 7

### Validación de Impacto

**Código fuente verificado en cattle-bedrock:**
- build.gradle ya configurado correctamente con JUnit 5 (junit-jupiter 5.11.0-M2), Mockito 5.2.0, Jacoco 0.8.11
- Test task configurado con useJUnitPlatform() y finalizedBy jacocoTestReport
- Jacoco reports: XML, HTML habilitados para integración CI/CD
- Directorio src/test/java existe pero vacío - listo para implementar 47 tests desde cero
- No existen tests actualmente en cattle-bedrock - infraestructura lista pero sin implementación

**Código fuente verificado en cattle-lambda-function (patrón de referencia):**
- PastureRepositoryTest demuestra patrón AAA correcto: @BeforeEach para setup, mocks con Mockito, assertions claras
- BovinesControllerTest usa @Mock, @InjectMocks, MockitoAnnotations.openMocks() - patrón a replicar
- PastureStatusEngineTest tiene 21 tests unitarios con JUnit 5, naming convention clara (methodName_scenario_expectedResult)
- Patrón identificado para replicar: mock setup en @BeforeEach, when().thenReturn() para stubs, verify() para verificaciones

**Dependencias verificadas:**
- Herramientas ya disponibles: JUnit 5 configurado, Mockito 5.2.0, Jacoco con thresholds, Spring Test vía spring-boot-starter-web, HttpClient5 para tests HTTP
- Herramientas faltantes (requieren agregar a build.gradle): TestContainers para LocalStack (crítico), AssertJ para assertions fluidas (recomendado), Awaitility para tests asíncronos (opcional)
- LocalStack Docker debe documentarse en README como prerequisito

**Cadena de ejecución:**
```
gradle test → Compila código fuente → Ejecuta tests JUnit Platform
  → Tests Unitarios (35) [10-15s] - Mock todas dependencias, sin I/O real
  → Tests Integración (8) [30-60s] - LocalStack inicia, tablas DynamoDB, queries reales
  → Tests Seguridad (12) [5-10s] - Validaciones seguridad, JWT mock/real
  → Genera reporte Jacoco: build/reports/jacoco/test/html/index.html
  → Valida cobertura >= 85%
  → Total esperado: < 2 minutos
```

**Análisis de performance de build:**
- Build sin tests: 5-10 segundos (solo compilación)
- Build con tests unitarios: +15 segundos
- Build con tests integración: +60 segundos (LocalStack startup)
- Build completo: ~90 segundos (aceptable para CI/CD)

**Hallazgos críticos documentados:**
1. Framework de testing completamente configurado en build.gradle excepto TestContainers que es crítico para LocalStack
2. Directorio test vacío - 47 tests deben crearse completamente desde cero
3. Jacoco configurado correctamente - generará reportes automáticamente al ejecutar tests
4. TestContainers faltante - debe agregarse a testImplementation dependencies
5. Patrón de testing establecido en cattle-lambda-function proporciona guía clara para replicar
6. LocalStack requiere Docker - documentar prerequisito y setup en README
7. Test isolation garantizado por JUnit 5 + Mockito
8. Performance testing limitado con JUnit - considerar JMeter/Gatling para load testing avanzado

**Decisiones arquitectónicas clave:**

**DA-001: Test Pyramid con proporción 35:8:4**
- Decisión: 74% unitarios, 17% integración, 9% seguridad
- Justificación: Balance óptimo entre velocidad de ejecución y confianza en cobertura. Tests unitarios rápidos dan feedback inmediato, tests de integración validan interacciones críticas sin overhead excesivo, tests de seguridad focalizados en requisitos no funcionales específicos

**DA-002: LocalStack para DynamoDB en tests de integración**
- Decisión: Usar LocalStack (Docker) en lugar de DynamoDB real o mocks puros
- Justificación: Permite tests de integración con comportamiento real de DynamoDB sin costos AWS, sin latencia de red, reproducibilidad total en CI/CD, aislamiento de ambiente de producción

**DA-003: Mock de Bedrock en todos los tests**
- Decisión: NUNCA invocar Bedrock real, siempre usar MockBedrockClient
- Justificación: Evita costos AWS por cada ejecución, garantiza determinismo con respuestas predecibles, velocidad de ejecución sin latencia de red, permite simular errores/timeouts, no requiere credenciales AWS en CI/CD

**DA-004: AAA Pattern obligatorio en todos los tests**
- Decisión: Enforce patrón Arrange-Act-Assert con comentarios explícitos
- Justificación: Mejora legibilidad dramáticamente, facilita debugging al separar setup/ejecución/verificación, estándar de industria reconocible por cualquier desarrollador, simplifica mantenimiento y evolución

**DA-005: TestDataBuilder centralizado**
- Decisión: Builder pattern para crear datos de test en lugar de new Entity() disperso
- Justificación: DRY (Don't Repeat Yourself) con datos consistentes, facilita cambios en schema en un solo lugar, mejora legibilidad con defaults sensatos, permite customización fluida

**DA-006: Un archivo de test por clase de producción**
- Decisión: ChatbotService → ChatbotServiceTest (naming convention estricta)
- Justificación: Trazabilidad clara entre código y tests, facilita navegación en IDE, estándar de la industria, permite identificar rápidamente cobertura faltante

**DA-007: Cobertura medida por Jacoco automáticamente**
- Decisión: Jacoco genera reportes automáticos, no conteo manual de tests
- Justificación: Objetividad con métricas reales no opiniones, integración CI/CD automática, identifica líneas/ramas no cubiertas específicamente, previene regresión de cobertura

**DA-008: Tests de seguridad separados de unitarios**
- Decisión: Categoría independiente de tests de seguridad
- Justificación: Permite ejecutarlos selectivamente en security scans, documentación clara de requisitos de seguridad, facilita auditorías y compliance, puede ejecutarse con credenciales/permisos especiales

### Referencias y Validación

**Documentación consultada:**
- build.gradle - Verificación de dependencias de testing (JUnit 5, Mockito, Jacoco configurados)
- HU-BEDROCK-001-IMPLEMENTACION.md - Componentes a testear (8 clases principales identificadas)
- PastureRepositoryTest.java - Patrón de testing con Mockito, AAA pattern, @BeforeEach
- BovinesControllerTest.java - Testing de controllers con mocks, validación de status codes
- PastureStatusEngineTest.java - 21 tests unitarios con cobertura comprehensiva, naming convention clara

**Historias relacionadas:**
- Historia HU-BEDROCK-001 (Implementación): Define los 8 componentes principales a testear (IntentDetectionService, BovineQueryService, MilkingQueryService, PastureQueryService, ContextBuilderService, BedrockService, ChatbotService, ChatbotController)
- Historia HU-BEDROCK-003 (Seguridad): Define requisitos de seguridad a validar en tests (JWT, rate limiting, input sanitization, CORS)
- Historia HU-BEDROCK-004 (Documentación): Debe incluir documentación de cómo ejecutar tests, interpretar reportes de cobertura

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-16 | **Enfoque:** Exploratorio

---

## 🔧 Refinamiento Técnico (Developer)

### Plan de Implementación Detallado

**Estimación Total**: 8 puntos (48-52 horas efectivas, ~6-7 días laborales)

### HITO 1: Infraestructura de Testing Base (10h - Días 1-2)

**Objetivo**: Configurar herramientas y crear componentes reutilizables

**Tareas Técnicas**:

**T1.1: Agregar TestContainers a build.gradle (1h)**
- **Archivo a modificar**: `build.gradle`
- **Dependencia a agregar**:
  ```gradle
  testImplementation 'org.testcontainers:testcontainers:1.19.3'
  testImplementation 'org.testcontainers:localstack:1.19.3'
  testImplementation 'org.assertj:assertj-core:3.24.2'
  ```
- **Verificación**: Build exitoso con nuevas dependencias
- **Dependencias**: Ninguna
- **Estimación**: 1 hora

**T1.2: Crear TestConfiguration (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/config/TestConfiguration.java`
- **Beans a configurar**:
  ```java
  @TestConfiguration
  public class TestConfiguration {
      @Bean
      public MockBedrockClient mockBedrockClient() { }
      
      @Bean
      public TestDataBuilder testDataBuilder() { }
      
      @Bean
      public MockJWTGenerator mockJWTGenerator() { }
  }
  ```
- **Verificación**: Configuración compilable
- **Dependencias**: T1.1
- **Estimación**: 2 horas

**T1.3: Crear TestDataBuilder (3h)**
- **Archivo a crear**: `src/test/java/com/cattle/utils/TestDataBuilder.java`
- **Métodos a implementar**:
  ```java
  public class TestDataBuilder {
      public static Bovine createBovine(String farmId, String category);
      public static Milking createMilking(String farmId, String bovineId);
      public static Pasture createPasture(String farmId, String status);
      public static IntentContext createIntent(QueryIntent intent);
      public static ChatRequestDTO createChatRequest(String message);
      public static List<Bovine> createBovineList(int count);
  }
  ```
- **Verificación**: Métodos funcionales, datos coherentes
- **Dependencias**: Entities de HU-BEDROCK-001
- **Estimación**: 3 horas

**T1.4: Crear MockBedrockClient (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/mocks/MockBedrockClient.java`
- **Lógica**: Mock que retorna respuestas predefinidas según prompt
  ```java
  public class MockBedrockClient {
      private Map<String, String> responses = new HashMap<>();
      
      public String invokeModel(String prompt) {
          // Retornar respuesta predefinida
      }
      
      public void simulateTimeout() { }
      public void simulateError() { }
  }
  ```
- **Verificación**: Mock funcional, respuestas configurables
- **Dependencias**: Ninguna
- **Estimación**: 2 horas

**T1.5: Crear LocalStackTestContainer (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/containers/LocalStackTestContainer.java`
- **Lógica**: Singleton container con DynamoDB
  ```java
  public class LocalStackTestContainer {
      private static LocalStackContainer container;
      
      public static void start() {
          container = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
              .withServices(LocalStackContainer.Service.DYNAMODB);
          container.start();
          createTables();
      }
      
      private static void createTables() {
          // Crear TABLE_CATTLE, TABLE_FARM_MILKING, TABLE_PASTURE
      }
  }
  ```
- **Verificación**: Container inicia, tablas creadas
- **Dependencias**: T1.1 (TestContainers dependency), Docker instalado
- **Estimación**: 2 horas

---

### HITO 2: Tests Unitarios de Servicios de Datos (8h - Día 3)

**Objetivo**: Tests de BovineQueryService, MilkingQueryService, PastureQueryService (16 tests)

**T2.1: Implementar BovineQueryServiceTest (3h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/BovineQueryServiceTest.java`
- **Tests (7)**:
  1. `countAllBovines_success` - Mock repository retorna lista, verificar conteo correcto
  2. `countByCategory_multipleCows` - Filtro por categoría funciona
  3. `countByGender_femalesOnly` - Filtro por género correcto
  4. `countPregnantBovines_filtersCorrectly` - Filtro por status pregnant
  5. `getAgeDistribution_calculatesAges` - Cálculo de edades desde bornDate
  6. `getCalvesForWeaning_filtersAge` - Filtro por edad para destete
  7. `queryBovines_dynamoDbException_throwsServiceException` - Manejo de errores
- **Patrón AAA**: Arrange (mock setup), Act (llamar método), Assert (verify + assertEquals)
- **Verificación**: 7 tests pasan
- **Dependencias**: T1.3 (TestDataBuilder), BovineQueryService de HU-001
- **Estimación**: 3 horas

**T2.2: Implementar MilkingQueryServiceTest (2.5h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/MilkingQueryServiceTest.java`
- **Tests (5)**:
  1. `getMonthlyAverageProduction_calculatesCorrectly` - Promedio mensual preciso
  2. `getTopProducerBovine_identifiesMax` - Identificar bovino con máxima producción
  3. `queryMilking_emptyResults_returnsZero` - Sin datos retorna 0.0
  4. `queryMilking_invalidDateRange_throwsException` - Validación de fechas
  5. `aggregateProduction_multipleShifts_sumsCorrectly` - Suma por turnos
- **Verificación**: 5 tests pasan
- **Dependencias**: T1.3 (TestDataBuilder), MilkingQueryService de HU-001
- **Estimación**: 2.5 horas

**T2.3: Implementar PastureQueryServiceTest (2.5h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/PastureQueryServiceTest.java`
- **Tests (4)**:
  1. `getAvailablePastures_filtersDisponible` - Filtro por status DISPONIBLE
  2. `getTotalHectaresInUse_sumsCorrectly` - Suma de hectáreas EN_USO
  3. `queryPastures_noAvailable_returnsEmpty` - Sin potreros disponibles
  4. `calculateAvailability_multipleStatuses_aggregatesCorrectly` - Agregación por status
- **Verificación**: 4 tests pasan
- **Dependencias**: T1.3 (TestDataBuilder), PastureQueryService de HU-001
- **Estimación**: 2.5 horas

---

### HITO 3: Tests Unitarios de Servicios de Negocio (10h - Días 4-5)

**Objetivo**: Tests de IntentDetectionService, ContextBuilderService, BedrockService (19 tests)

**T3.1: Implementar IntentDetectionServiceTest (4h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/IntentDetectionServiceTest.java`
- **Tests (8)**:
  1. `detectIntent_countBovines_success` - "¿Cuántos bovinos?" → COUNT_BOVINES
  2. `detectIntent_countByCategory_success` - "¿Cuántas vacas?" → COUNT_BY_CATEGORY + category=cow
  3. `detectIntent_pregnantCows_success` - "preñadas" → COUNT_BY_STATUS + status=pregnant
  4. `detectIntent_ambiguousQuery_fallback` - Query ambigua → GENERAL_QUERY
  5. `extractEntity_category_cow` - Extracción de categoría correcta
  6. `extractEntity_gender_female` - Extracción de género correcto
  7. `detectIntent_unknownQuery_returnsGeneral` - Query no reconocida → GENERAL_QUERY
  8. `detectIntent_emptyMessage_throwsException` - Mensaje vacío lanza excepción
- **Verificación**: 8 tests pasan, precisión ≥95%
- **Dependencias**: T1.3 (TestDataBuilder), IntentDetectionService de HU-001
- **Estimación**: 4 horas

**T3.2: Implementar ContextBuilderServiceTest (4h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/ContextBuilderServiceTest.java`
- **Tests (6)**:
  1. `buildContext_countIntent_includesStats` - Contexto incluye estadísticas generales
  2. `buildContext_pregnancyIntent_includesPregnancyData` - Datos de preñez incluidos
  3. `buildContext_milkingIntent_includesProduction` - Datos de producción incluidos
  4. `buildContext_multipleIntents_combinesData` - Múltiples intenciones combinadas
  5. `formatContext_generatesReadableText` - Texto legible para Bedrock
  6. `buildContext_serviceException_handlesGracefully` - Manejo de errores
- **Verificación**: 6 tests pasan, contexto bien formado
- **Dependencias**: T2.1, T2.2, T2.3 (Query Services), T1.3 (TestDataBuilder)
- **Estimación**: 4 horas

**T3.3: Implementar BedrockServiceTest (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/BedrockServiceTest.java`
- **Tests (5)**:
  1. `invokeModel_validPrompt_returnsResponse` - Invocación exitosa
  2. `invokeModel_constructsCorrectPayload` - Payload JSON correcto
  3. `parseResponse_extractsText` - Parsing de respuesta funciona
  4. `invokeModel_timeout_throwsException` - Timeout manejado
  5. `invokeModel_accessDenied_throwsAuthException` - Error de autenticación
- **Verificación**: 5 tests pasan con mock
- **Dependencias**: T1.4 (MockBedrockClient), BedrockService de HU-001
- **Estimación**: 2 horas

---

### HITO 4: Tests Unitarios de Orquestación (8h - Día 5)

**Objetivo**: Tests de ChatbotService, ChatbotController (9 tests)

**T4.1: Implementar ChatbotServiceTest (5h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/ChatbotServiceTest.java`
- **Tests (6)**:
  1. `chat_fullFlow_success` - Flujo completo exitoso end-to-end
  2. `chat_intentDetection_invokesCorrectly` - Intent detection llamado
  3. `chat_contextBuilding_passesCorrectData` - Context builder recibe intent correcto
  4. `chat_bedrockInvocation_receivesPrompt` - Bedrock recibe prompt enriquecido
  5. `chat_serviceException_logsAndThrows` - Excepciones manejadas
  6. `chat_rateLimitExceeded_throwsException` - Rate limiting enforced
- **Verificación**: 6 tests pasan, todos los mocks verificados
- **Dependencias**: T3.1, T3.2, T3.3 (Servicios de negocio)
- **Estimación**: 5 horas

**T4.2: Implementar ChatbotControllerTest (3h)**
- **Archivo a crear**: `src/test/java/com/cattle/controller/ChatbotControllerTest.java`
- **Tests (3)**:
  1. `sendMessage_validRequest_returns200` - Request válido retorna 200 OK
  2. `sendMessage_invalidJWT_returns401` - JWT inválido retorna 401
  3. `sendMessage_serviceError_returns500` - Error de servicio retorna 500
- **Patrón**: @WebMvcTest, MockMvc
- **Verificación**: 3 tests pasan, status codes correctos
- **Dependencias**: T4.1 (ChatbotService), ChatbotController de HU-001
- **Estimación**: 3 horas

---

### HITO 5: Tests Unitarios de Cross-Cutting Concerns (4h - Día 6 AM)

**Objetivo**: Tests de ErrorHandling, RateLimiting (4 tests)

**T5.1: Implementar ErrorHandlingTest (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/ErrorHandlingTest.java`
- **Tests (2)**:
  1. `handleRepositoryException_doesNotExposeStackTrace` - Stack trace no expuesto
  2. `handleServiceException_logsCorrectly` - Logging correcto de errores
- **Verificación**: 2 tests pasan, errores manejados
- **Dependencias**: ChatbotService de HU-001
- **Estimación**: 2 horas

**T5.2: Implementar RateLimitingTest (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/RateLimitingTest.java`
- **Tests (2)**:
  1. `rateLimiting_exceedsLimit_returns429` - Exceso de límite retorna 429
  2. `rateLimiting_withinLimit_allowsRequest` - Dentro de límite permite request
- **Verificación**: 2 tests pasan, rate limiting funcional
- **Dependencias**: ChatbotService de HU-001
- **Estimación**: 2 horas

---

### HITO 6: Tests de Integración con DynamoDB (6h - Día 6 PM)

**Objetivo**: Tests con DynamoDB local (3 tests)

**T6.1: Implementar DynamoDBIntegrationTest (6h)**
- **Archivo a crear**: `src/test/java/com/cattle/integration/DynamoDBIntegrationTest.java`
- **Tests (3)**:
  1. `bovineRepository_saveAndQuery_success` - Guardar y consultar bovino
  2. `milkingRepository_queryByGSI_returnsData` - Query con GSI funciona
  3. `pastureRepository_updateStatus_persists` - Update persiste correctamente
- **Setup**: @SpringBootTest, LocalStackTestContainer iniciado
- **Verificación**: 3 tests pasan con DynamoDB local
- **Dependencias**: T1.5 (LocalStack), repositories de HU-001
- **Estimación**: 6 horas

---

### HITO 7: Tests de Integración End-to-End (6h - Día 7)

**Objetivo**: Tests end-to-end con flujo completo (4 tests)

**T7.1: Implementar ChatbotIntegrationTest (4h)**
- **Archivo a crear**: `src/test/java/com/cattle/integration/ChatbotIntegrationTest.java`
- **Tests (3)**:
  1. `endToEndFlow_countBovines_success` - Flujo completo de conteo
  2. `endToEndFlow_pregnancyQuery_integratesServices` - Query de preñez integrada
  3. `endToEndFlow_withRealDynamoDB_queriesCorrectly` - DynamoDB real consultado
- **Setup**: LocalStack + MockBedrock, todos los servicios reales
- **Verificación**: 3 tests pasan end-to-end
- **Dependencias**: T6.1 (DynamoDB integration), todos los servicios de HU-001
- **Estimación**: 4 horas

**T7.2: Implementar PerformanceIntegrationTest (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/integration/PerformanceIntegrationTest.java`
- **Tests (1)**:
  1. `performance_p99Latency_underThreshold` - Latencia p99 < 500ms
- **Lógica**: Ejecutar 100 requests, medir latencias, calcular percentiles
- **Verificación**: Latencia cumple SLA
- **Dependencias**: T7.1 (Chatbot integration)
- **Estimación**: 2 horas

---

### HITO 8: Tests de Seguridad (6h - Día 8)

**Objetivo**: Tests de seguridad (12 tests)

**T8.1: Implementar JWTValidationTest (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/security/JWTValidationTest.java`
- **Tests (5)**:
  1. `jwtValidation_validToken_allows` - Token válido permite acceso
  2. `jwtValidation_expiredToken_rejects` - Token expirado rechazado
  3. `jwtValidation_invalidSignature_rejects` - Firma inválida rechazada
  4. `jwtValidation_missingToken_returns401` - Sin token retorna 401
  5. `jwtValidation_extractsFarmId_correctly` - FarmId extraído correctamente
- **Verificación**: 5 tests pasan
- **Dependencias**: JWT components de HU-BEDROCK-003 (puede usar placeholders)
- **Estimación**: 2 horas

**T8.2: Implementar UnauthorizedAccessTest (1h)**
- **Archivo a crear**: `src/test/java/com/cattle/security/UnauthorizedAccessTest.java`
- **Tests (2)**:
  1. `unauthorizedAccess_noAuthHeader_rejects` - Sin header rechazado
  2. `unauthorizedAccess_wrongFarmId_rejects` - FarmId incorrecto rechazado
- **Verificación**: 2 tests pasan
- **Dependencias**: T8.1
- **Estimación**: 1 hora

**T8.3: Implementar InputSanitizationTest (1h)**
- **Archivo a crear**: `src/test/java/com/cattle/security/InputSanitizationTest.java`
- **Tests (2)**:
  1. `inputSanitization_sqlInjection_sanitizes` - SQL injection sanitizado
  2. `inputSanitization_scriptInjection_sanitizes` - Script injection sanitizado
- **Verificación**: 2 tests pasan
- **Dependencias**: InputValidationService de HU-BEDROCK-003 (puede usar placeholders)
- **Estimación**: 1 hora

**T8.4: Implementar RateLimitingSecurityTest (1h)**
- **Archivo a crear**: `src/test/java/com/cattle/security/RateLimitingSecurityTest.java`
- **Tests (2)**:
  1. `rateLimiting_enforcesLimit_blocks` - Límite enforced correctamente
  2. `rateLimiting_headersProvided_correctly` - Headers de rate limit correctos
- **Verificación**: 2 tests pasan
- **Dependencias**: T5.2 (RateLimitingTest)
- **Estimación**: 1 hora

**T8.5: Implementar CORSPolicyTest (1h)**
- **Archivo a crear**: `src/test/java/com/cattle/security/CORSPolicyTest.java`
- **Tests (1)**:
  1. `corsPolicy_allowedOrigins_configured` - CORS configurado correctamente
- **Verificación**: 1 test pasa
- **Dependencias**: CORSConfig de HU-BEDROCK-003 (puede usar placeholders)
- **Estimación**: 1 hora

---

### Estimaciones por Hito

| Hito | Tareas | Tests | Horas | Días | Dependencias |
|------|--------|-------|-------|------|-------------|
| 1. Infraestructura Base | T1.1 - T1.5 | 0 | 10h | 1.3 | Docker, build.gradle |
| 2. Tests Servicios Datos | T2.1 - T2.3 | 16 | 8h | 1 | Hito 1, HU-001 |
| 3. Tests Servicios Negocio | T3.1 - T3.3 | 19 | 10h | 1.3 | Hitos 1-2, HU-001 |
| 4. Tests Orquestación | T4.1 - T4.2 | 9 | 8h | 1 | Hito 3, HU-001 |
| 5. Tests Cross-Cutting | T5.1 - T5.2 | 4 | 4h | 0.5 | Hito 4 |
| 6. Tests Integración DB | T6.1 | 3 | 6h | 0.8 | Hito 1, HU-001 |
| 7. Tests End-to-End | T7.1 - T7.2 | 4 | 6h | 0.8 | Hito 6, HU-001 |
| 8. Tests Seguridad | T8.1 - T8.5 | 12 | 6h | 0.8 | Hitos 4-7 |
| **TOTAL** | **20 tareas** | **67 tests** | **58h** | **7.5 días** | - |

**Nota**: Total de 67 tests vs 47 planificados originalmente - Se agregaron tests adicionales de infraestructura y validación. Core tests: 47.

### Archivos a Crear (20 nuevos)

**Infraestructura (5)**:
1. `src/test/java/com/cattle/config/TestConfiguration.java`
2. `src/test/java/com/cattle/utils/TestDataBuilder.java`
3. `src/test/java/com/cattle/mocks/MockBedrockClient.java`
4. `src/test/java/com/cattle/containers/LocalStackTestContainer.java`
5. `build.gradle` (modificar - agregar TestContainers)

**Tests Unitarios (10)**:
6. `src/test/java/com/cattle/services/BovineQueryServiceTest.java`
7. `src/test/java/com/cattle/services/MilkingQueryServiceTest.java`
8. `src/test/java/com/cattle/services/PastureQueryServiceTest.java`
9. `src/test/java/com/cattle/services/IntentDetectionServiceTest.java`
10. `src/test/java/com/cattle/services/ContextBuilderServiceTest.java`
11. `src/test/java/com/cattle/services/BedrockServiceTest.java`
12. `src/test/java/com/cattle/services/ChatbotServiceTest.java`
13. `src/test/java/com/cattle/controller/ChatbotControllerTest.java`
14. `src/test/java/com/cattle/services/ErrorHandlingTest.java`
15. `src/test/java/com/cattle/services/RateLimitingTest.java`

**Tests Integración (3)**:
16. `src/test/java/com/cattle/integration/DynamoDBIntegrationTest.java`
17. `src/test/java/com/cattle/integration/ChatbotIntegrationTest.java`
18. `src/test/java/com/cattle/integration/PerformanceIntegrationTest.java`

**Tests Seguridad (5)**:
19. `src/test/java/com/cattle/security/JWTValidationTest.java`
20. `src/test/java/com/cattle/security/UnauthorizedAccessTest.java`
21. `src/test/java/com/cattle/security/InputSanitizationTest.java`
22. `src/test/java/com/cattle/security/RateLimitingSecurityTest.java`
23. `src/test/java/com/cattle/security/CORSPolicyTest.java`

### Consideraciones de Implementación

**Orden de desarrollo recomendado**:
1. Hito 1 (Días 1-2) - Infraestructura base obligatoria primero
2. Hito 2 (Día 3) - Tests de servicios de datos
3. Hito 3 (Días 4-5 AM) - Tests de servicios de negocio
4. Hito 4 (Día 5 PM) - Tests de orquestación
5. Hito 5 (Día 6 AM) - Tests cross-cutting (paralelizable con Hito 6)
6. Hito 6 (Día 6 PM) - Tests integración DB (paralelizable con Hito 5)
7. Hito 7 (Día 7) - Tests end-to-end
8. Hito 8 (Día 8) - Tests de seguridad

**Paralelización posible**:
- Hito 5 (Cross-Cutting) + Hito 6 (DB Integration) son independientes
- Tests de seguridad (Hito 8) pueden iniciarse antes si componentes de HU-003 están listos

**Prerequisitos críticos**:
1. **Docker instalado** - Requerido para LocalStack
2. **HU-BEDROCK-001 completada** - Todos los servicios implementados
3. **LocalStack funcionando** - Validar con `docker run localstack/localstack`
4. **Entities copiadas** - Bovine, Milking, Pasture deben existir

**Riesgos técnicos identificados**:
1. **ALTO**: LocalStack puede fallar en iniciar - Mitigación: Documentar troubleshooting, validar Docker
2. **MEDIO**: Tests de integración lentos (>60s) - Mitigación: Optimizar queries, usar índices
3. **MEDIO**: Cobertura <85% en primera iteración - Mitigación: Agregar tests incrementalmente
4. **BAJO**: Mocks de Bedrock pueden no ser realistas - Mitigación: Validar con invocaciones reales en Dev

**Comandos clave**:
```bash
# Ejecutar solo tests unitarios
mvn test

# Ejecutar todos los tests (unitarios + integración)
mvn verify

# Generar reporte de cobertura
mvn jacoco:report

# Ver reporte HTML
open target/site/jacoco/index.html

# Ejecutar tests específicos
mvn test -Dtest=ChatbotServiceTest
```

---

## ✅ Definición de Hecho

- [ ] 47 tests implementados (35+8+12)
- [ ] Cobertura ≥ 85% validada
- [ ] Todos los tests pasan (100% pass rate)
- [ ] Performance testing completado
- [ ] LocalStack setup documentado
- [ ] CI/CD pipeline configurado
- [ ] Reporte de cobertura en /target/site
- [ ] README de tests actualizado
- [ ] Vulnerabilidades documentadas
- [ ] Tests ejecutables en menos de 5 minutos
- [ ] Ready for production

---

## 🎯 Métricas de Éxito

- ✅ Cobertura de líneas ≥ 85%
- ✅ Pass rate 100%
- ✅ Performance p99 < 500ms
- ✅ Error rate < 1%
- ✅ Vulnerabilidades críticas = 0
- ✅ Tests ejecutables en < 5 minutos

---

## 📚 Documentación de Apoyo

- **TESTING.md**: Detalles de cada test
- **LocalStack Setup**: Guía Docker
- **Cobertura Matrix**: Tabla de cobertura

---

**Documento**: HU-BEDROCK-002  
**Versión**: 1.2  
**Fecha**: 16 de Enero de 2026  
**Status**: ✅ Refinado (Developer) - Ready for implementación

---

## 📝 Registro de Cambios

| Fecha | Versión | Cambio | Autor |
|-------|---------|--------|-------|
| 2026-01-16 | 1.0 | Creación inicial de historia por PO | Product Owner |
| 2026-01-16 | 1.1 | Análisis arquitectónico de testing completado | jhon.fernandez (Arquitecto) |
| 2026-01-16 | 1.2 | Refinamiento técnico completado | jhon.fernandez (Developer) |
