# HU-ASEGURAMIENTO-CALIDAD-001: Incrementar Cobertura de Pruebas al 85-90%

## 📋 Información General

| Campo | Valor |
|-------|-------|
| **ID** | HU-ASEGURAMIENTO-CALIDAD-001 |
| **Título** | Incrementar cobertura de pruebas unitarias e integración del 17% al 87-90% |
| **Prioridad** | 🔴 CRÍTICA |
| **Estimación** | 13 puntos (86 horas / 11.5 días laborales) |
| **Sprint** | Sprint 3 - Q1 2026 |
| **Epic** | Calidad y Testing |
| **Tipo** | Technical Debt / Quality Assurance |
| **Versión** | 1.1 (Refinada por Arquitecto) |

---

## 📊 Estado Actual de Cobertura (Baseline)

### Reporte Jacoco - Fecha: 2026-01-20

```
Cobertura General del Proyecto: 17% ⚠️
├── Instrucciones: 1,475 / 8,466 (17%)
├── Ramas:        82 / 619 (13%)
├── Líneas:       349 / 1,909 (18%)
├── Métodos:      98 / 407 (24%)
└── Clases:       47 / 74 (64%)
```

### Cobertura por Paquete

| Paquete | Cobertura | Líneas | Métodos | Estado |
|---------|-----------|--------|---------|--------|
| **com.cattle.utils** | 82% ✅ | 99/123 | 12/17 | Buena |
| **com.cattle.enums** | 59% ⚠️ | 16/25 | 4/6 | Aceptable |
| **com.cattle.config** | 46% ⚠️ | 21/39 | 11/16 | Mejorable |
| **com.cattle.events** | 36% ⚠️ | 15/46 | 13/19 | Mejorable |
| **com.cattle.controller** | 35% ⚠️ | 23/57 | 9/22 | Mejorable |
| **com.cattle.builders** | 22% ❌ | 34/115 | 9/31 | Crítico |
| **com.cattle.repository** | 17% ❌ | 67/328 | 12/47 | Crítico |
| **com.cattle.services.chatbot** | 12% ❌ | 11/65 | 4/7 | Crítico |
| **com.cattle.processor** | 6% ❌ | 21/158 | 4/21 | Crítico |
| **com.cattle.services** | 4% ❌ | 39/693 | 13/105 | **MUY CRÍTICO** |
| **com.cattle.mapper** | 1% ❌ | 3/127 | 3/11 | **MUY CRÍTICO** |
| **com.cattle.entities** | 0% ❌ | 0/79 | 0/79 | **SIN COBERTURA** |
| **com.cattle.exceptions** | 0% ❌ | 0/24 | 0/12 | **SIN COBERTURA** |
| **com.cattle.dtos** | 0% ❌ | 0/8 | 0/4 | **SIN COBERTURA** |

---

## 🎯 Objetivo de la Historia

**Como** equipo de desarrollo de Cattle Platform  
**Quiero** incrementar la cobertura de pruebas del 17% actual al 85-90%  
**Para** garantizar la calidad del código, detectar bugs tempranamente, facilitar refactoring seguro y cumplir con estándares de calidad enterprise.

---

## ✅ Criterios de Aceptación

### Criterios Funcionales

1. **Cobertura General**
   - [ ] Cobertura de instrucciones ≥ 85%
   - [ ] Cobertura de ramas ≥ 80%
   - [ ] Cobertura de líneas ≥ 85%
   - [ ] Cobertura de métodos ≥ 90%

2. **Cobertura por Paquete (Mínimos)**
   - [ ] **Services:** ≥ 90% (actualmente 4%)
   - [ ] **Repository:** ≥ 85% (actualmente 17%)
   - [ ] **Controller:** ≥ 80% (actualmente 35%)
   - [ ] **Processor:** ≥ 85% (actualmente 6%)
   - [ ] **Mapper:** ≥ 70% (actualmente 1%)
   - [ ] **Entities:** ≥ 60% (actualmente 0%)
   - [ ] **DTOs:** ≥ 50% (actualmente 0%)

3. **Calidad de Tests**
   - [ ] Todos los tests siguen patrón AAA (Arrange-Act-Assert)
   - [ ] Sin tests duplicados o redundantes
   - [ ] Tests independientes (sin dependencias entre tests)
   - [ ] Naming convention: `methodName_scenario_expectedResult`
   - [ ] Uso de mocks apropiados (Mockito)
   - [ ] Tests parametrizados donde aplique (@ParameterizedTest)

4. **Tests de Integración**
   - [ ] Mínimo 15 tests de integración con DynamoDB (LocalStack)
   - [ ] Tests end-to-end del flujo completo (chatbot)
   - [ ] Tests de seguridad (JWT, rate limiting, CORS)

5. **Documentación**
   - [ ] README actualizado con instrucciones de testing
   - [ ] Documentación de casos edge cubiertos
   - [ ] Comentarios en tests complejos

### Criterios No Funcionales

6. **Performance**
   - [ ] Suite de tests unitarios se ejecuta en < 45 segundos
   - [ ] Suite completa (unitarios + integración) < 3 minutos
   - [ ] Tests en paralelo habilitados

7. **Mantenibilidad**
   - [ ] TestDataBuilder centralizado y reutilizable
   - [ ] Fixtures compartidos en package `test/fixtures`
   - [ ] No código duplicado en setup de tests

8. **CI/CD**
   - [ ] Tests se ejecutan automáticamente en build
   - [ ] Reporte Jacoco generado y publicado
   - [ ] Build falla si cobertura < 85%

---

## 📐 Análisis de Impacto y Priorización

### Prioridad 1: Paquetes Críticos (Mayor Impacto en Cobertura)

#### 1.1 Services (4% → 90%) - **+60% cobertura total**
- **Impacto:** CRÍTICO - 693 líneas sin cubrir
- **Clases:**
  - `BovinesService` (0%) - 18 métodos
  - `MilkingService` (22%) - 2/9 métodos
  - `PastureService` (0%) - 15 métodos
  - `PlanService` (0%) - 8 métodos
  - `IntentDetectionService` (4%) - 2/10 métodos
  - `ContextBuilderService` (0%) - 7 métodos
  - `BovineQueryService` (0%) - 12 métodos
  - `MilkingQueryService` (0%) - 12 métodos
  - `PastureQueryService` (0%) - 8 métodos

- **Tests a crear:** 65 tests unitarios

#### 1.2 Repository (17% → 85%) - **+45% cobertura total**
- **Impacto:** CRÍTICO - 261 líneas sin cubrir
- **Clases:**
  - `BovineRepository` (20%) - 12 métodos principales
  - `MilkingRepository` (15%) - 8 métodos
  - `PastureRepository` (30%) - Ya tiene base, expandir
  - `PlanRepository` (0%) - 4 métodos
  - `CountersRepository` (10%) - 2 métodos

- **Tests a crear:** 35 tests unitarios

#### 1.3 Processor (6% → 85%) - **+35% cobertura total**
- **Impacto:** ALTO - 137 líneas sin cubrir
- **Clases:**
  - `BovinesProcessor` (5%) - 5 métodos
  - `MilkingProcessor` (8%) - 3 métodos
  - `PastureProcessor` (5%) - 2 métodos
  - `RotationPlanProcessor` (5%) - 3 métodos

- **Tests a crear:** 25 tests unitarios

### Prioridad 2: Lógica de Negocio

#### 2.1 Builders (22% → 80%)
- **Impacto:** MEDIO - 81 líneas sin cubrir
- **Tests a crear:** 15 tests unitarios

#### 2.2 Events (36% → 75%)
- **Impacto:** MEDIO - 31 líneas sin cubrir
- **Tests a crear:** 10 tests unitarios

### Prioridad 3: Infraestructura

#### 3.1 Mapper (1% → 65%) ⭐ OPTIMIZADO
- **Impacto:** BAJO - Mappers auto-generados por MapStruct
- **Tests a crear:** 3 tests unitarios (edge cases solamente)
- **Justificación:** Código generado no requiere test exhaustivo, solo validar compilación y nulls
- **Ahorro:** 9 tests × 0.5h = 4.5h → reasignadas a integración

#### 3.2 Entities (0% → 60%)
- **Impacto:** BAJO - Mostly POJOs
- **Tests a crear:** 8 tests (getters/setters/builders)

#### 3.3 DTOs (0% → 50%)
- **Impacto:** BAJO
- **Tests a crear:** 6 tests (validaciones)

#### 3.4 Exceptions (0% → 60%)
- **Impacto:** BAJO
- **Tests a crear:** 4 tests

---

## 🔧 Plan de Implementación Detallado

### FASE 1: Services (Días 1-5) - +60% cobertura

#### HITO 1.1: BovinesService (Día 1) - 18 tests
**Cobertura esperada: 0% → 95%**

```java
// Tests a crear en BovinesServiceTest.java
@Test void findAll_returnsListOfBovines()
@Test void findAll_emptyRepository_returnsEmptyList()
@Test void findById_existingId_returnsBovine()
@Test void findById_nonExistingId_returnsEmpty()
@Test void save_validBovine_returnsCreatedBovine()
@Test void save_withFarmId_assignsFarmId()
@Test void save_generatesId_whenNotProvided()
@Test void update_existingBovine_returnsUpdated()
@Test void update_nonExisting_throwsNotFoundException()
@Test void deleteById_existingId_deletesSuccessfully()
@Test void deleteById_nonExisting_throwsException()
@Test void findByGender_male_returnsOnlyMales()
@Test void findByGender_female_returnsOnlyFemales()
@Test void countByFarmId_returnsCorrectCount()
@Test void findByCategory_cow_returnsOnlyCows()
@Test void findByStatus_pregnant_returnsPregnant()
@Test void repositoryException_throwsServiceException()
@Test void nullFarmId_throwsIllegalArgumentException()
```

**Archivo:** `src/test/java/com/cattle/services/BovinesServiceTest.java`

#### HITO 1.2: MilkingService (Día 1) - 10 tests
**Cobertura esperada: 22% → 95%**

```java
@Test void getMilkingData_validBovineAndDate_returnsMilking()
@Test void getMilkingData_invalidBovine_returnsEmpty()
@Test void getMilkingData_noDataForDate_returnsEmpty()
@Test void createMilking_validData_createsSuccessfully()
@Test void createMilking_duplicateEntry_throwsException()
@Test void createMilking_invalidLiters_throwsValidationException()
@Test void createMilking_setsPkSk_correctly()
@Test void createMilking_assignsLactationNumber()
@Test void repositoryFailure_throwsServiceException()
@Test void createMilking_nullValues_throwsException()
```

**Archivo:** `src/test/java/com/cattle/services/MilkingServiceTest.java`

#### HITO 1.3: PastureService (Día 2) - 15 tests
**Cobertura esperada: 0% → 95%**

```java
@Test void findPastures_validFarmId_returnsList()
@Test void findPastures_emptyFarm_returnsEmptyList()
@Test void applyEvent_openEvent_changesStatus()
@Test void applyEvent_closeEvent_updatesOccupancy()
@Test void applyEvent_maintenanceSet_setsSubstatus()
@Test void applyEvent_maintenanceClear_clearsSubstatus()
@Test void autoUpdateByHoldUntil_expired_changesStatus()
@Test void autoUpdateByHoldUntil_notExpired_noChange()
@Test void getAvailablePastures_filtersCorrectly()
@Test void calculateTotalHectares_sumsCorrectly()
@Test void validatePastureStatus_invalidTransition_throwsException()
@Test void applyPatch_validChanges_updates()
@Test void applyPatch_invalidField_throwsException()
@Test void findById_nonExisting_returnsEmpty()
@Test void repositoryException_propagatesCorrectly()
```

**Archivo:** `src/test/java/com/cattle/services/PastureServiceTest.java`

#### HITO 1.4: Query Services (Día 3) - 22 tests
**BovineQueryService (8 tests), MilkingQueryService (7 tests), PastureQueryService (7 tests)**

Ya definidos en HU-BEDROCK-002, referencia:
- `BovineQueryServiceTest.java` - 8 tests
- `MilkingQueryServiceTest.java` - 7 tests
- `PastureQueryServiceTest.java` - 7 tests

#### HITO 1.5: Chatbot Services (Días 4-5) - 19 tests

Ya definidos en HU-BEDROCK-002:
- `IntentDetectionServiceTest.java` - 8 tests
- `ContextBuilderServiceTest.java` - 6 tests
- `BedrockServiceTest.java` - 5 tests

#### HITO 1.6: PlanService (Día 5) - 8 tests
**Cobertura esperada: 0% → 90%**

```java
@Test void findPlans_validFarmId_returnsList()
@Test void findPlans_emptyFarm_returnsEmpty()
@Test void findPlanBySpecies_kikuyu_returnsCorrectPlan()
@Test void findPlanBySpecies_nonExisting_returnsNull()
@Test void validatePlan_validData_passes()
@Test void validatePlan_invalidDates_throwsException()
@Test void repositoryException_handlesGracefully()
@Test void cachePlans_improvePerformance()
```

**Archivo:** `src/test/java/com/cattle/services/PlanServiceTest.java`

---

### FASE 2: Repository (Días 6-7) - +45% cobertura

#### HITO 2.1: BovineRepository (Día 6) - 15 tests
**Cobertura esperada: 20% → 90%**

```java
@Test void findAll_withMockDynamoDB_returnsAllBovines()
@Test void findById_existingId_returnsBovine()
@Test void findById_nonExisting_returnsEmpty()
@Test void save_newBovine_savesSuccessfully()
@Test void save_setPkSk_correctly()
@Test void update_existingBovine_updates()
@Test void deleteById_existingId_deletes()
@Test void findByGender_usesGSI_correctly()
@Test void countByFarmId_returnsAccurateCount()
@Test void findByFarmIdAndCategory_filtersCorrectly()
@Test void findByFarmIdAndGender_filtersCorrectly()
@Test void findByFarmIdAndStatus_filtersCorrectly()
@Test void findAllByFarmId_paginates_correctly()
@Test void dynamoDbException_throwsRepositoryException()
@Test void nullKey_throwsIllegalArgumentException()
```

**Archivo:** `src/test/java/com/cattle/repository/BovineRepositoryTest.java`

#### HITO 2.2: MilkingRepository (Día 6) - 10 tests
**Cobertura esperada: 15% → 90%**

```java
@Test void save_newMilking_savesWithCorrectKeys()
@Test void getMilkingByPk_existingPk_returnsList()
@Test void getMilkingByPkAndSk_exactMatch_returnsSingle()
@Test void getMilkingBetweenDates_dateRange_returnsFiltered()
@Test void getMilkingBetweenDates_invalidRange_returnsEmpty()
@Test void queryByGSI3_lactationNumber_worksCorrectly()
@Test void save_duplicateKey_throwsException()
@Test void getMilking_emptyResult_returnsEmpty()
@Test void dynamoDbException_wrapsCorrectly()
@Test void nullParameters_throwsException()
```

**Archivo:** `src/test/java/com/cattle/repository/MilkingRepositoryTest.java`

#### HITO 2.3: PlanRepository y CountersRepository (Día 7) - 10 tests
**Cobertura esperada: 0-10% → 85%**

```java
// PlanRepositoryTest (6 tests)
@Test void findPlans_validFarmId_returnsList()
@Test void findPlans_emptyResult_returnsEmpty()
@Test void findPlans_queryGSI_correctly()
@Test void findPlans_sortsBySpecies()
@Test void dynamoException_handlesGracefully()
@Test void nullFarmId_throwsException()

// CountersRepositoryTest (4 tests)
@Test void getNextId_bovine_incrementsCorrectly()
@Test void getNextId_concurrent_handlesRaceCondition()
@Test void getNextId_firstTime_startsAtOne()
@Test void updateItemException_throwsRepositoryException()
```

**Archivos:**
- `src/test/java/com/cattle/repository/PlanRepositoryTest.java`
- `src/test/java/com/cattle/repository/CountersRepositoryTest.java`

---

### FASE 3: Processor (Día 8) - +35% cobertura

#### HITO 3.1: BovinesProcessor (8 tests)
**Cobertura esperada: 5% → 90%**

```java
@Test void findAll_callsService_returnsMappedDTOs()
@Test void findById_existingId_returnsMappedDTO()
@Test void findById_nonExisting_returnsEmpty()
@Test void save_validDTO_callsServiceAndMaps()
@Test void save_invalidDTO_throwsValidationException()
@Test void update_existingBovine_updatesSuccessfully()
@Test void update_nonExisting_throwsNotFoundException()
@Test void serviceException_propagatesCorrectly()
```

#### HITO 3.2: MilkingProcessor (6 tests)
**Cobertura esperada: 8% → 90%**

```java
@Test void getMilkingData_validRequest_returnsMappedData()
@Test void getMilkingData_emptyResult_returnsEmpty()
@Test void createMilking_validDTO_createsSuccessfully()
@Test void createMilking_setPkSk_calculatesCorrectly()
@Test void createMilking_invalidData_throwsException()
@Test void serviceException_handlesGracefully()
```

#### HITO 3.3: PastureProcessor (5 tests)
**Cobertura esperada: 5% → 85%**

```java
@Test void listPastures_validStatus_returnsFiltered()
@Test void listPastures_emptyResult_returnsEmpty()
@Test void listPastures_mapsToDTO_correctly()
@Test void listPastures_nullFarmId_throwsException()
@Test void serviceException_wrapsCorrectly()
```

#### HITO 3.4: RotationPlanProcessor (6 tests)
**Cobertura esperada: 5% → 85%**

```java
@Test void getRotationSemaphoreItems_validFarm_returnsSortedList()
@Test void getRotationSemaphoreItems_emptyPastures_returnsEmpty()
@Test void findPlanForSpecies_existingSpecies_returnsCorrectPlan()
@Test void buildRotationSemaphoreItemDTO_calculatesETA_correctly()
@Test void getRotationSemaphoreItems_sortsBy_availabilityDate()
@Test void serviceException_handlesGracefully()
```

---

### FASE 4: Builders, Events, Mapper (Día 9) - +20% cobertura

#### HITO 4.1: Builders (15 tests)
**Cobertura esperada: 22% → 80%**

Crear tests para:
- `BovineBuilder` (5 tests) - Builder pattern validation
- `MilkingBuilder` (4 tests) - Builder with required fields
- `PastureBuilder` (3 tests) - Immutability tests
- `PlanBuilder` (3 tests) - Complex object building

#### HITO 4.2: Events (10 tests)
**Cobertura esperada: 36% → 75%**

```java
// OpenEventTest, CloseEventTest, MaintenanceEventTest, etc.
@Test void openEvent_type_returnsCorrectType()
@Test void closeEvent_withParams_storesCorrectly()
@Test void maintenanceSetEvent_substatus_setsCorrectly()
@Test void patchApplier_applyLocal_updatesFields()
@Test void entityPatch_isEmpty_detectsCorrectly()
```

#### HITO 4.3: Mapper (12 tests)
**Cobertura esperada: 1% → 70%**

```java
// BovinesMapperTest, MilkingMapperTest, PasturesMapperTest
@Test void toDTO_entity_mapsAllFields()
@Test void toDTO_nullEntity_returnsNull()
@Test void toEntity_dto_mapsAllFields()
@Test void toEntityList_dtoList_mapsCorrectly()
```

---

### FASE 5: DTOs, Entities, Exceptions (Día 10) - +10% cobertura

#### HITO 5.1: Entities (8 tests)
**Cobertura esperada: 0% → 60%**

```java
// Tests para Bovine, Milking, Pasture, Plan
@Test void builder_allFields_buildsCorrectly()
@Test void gettersSetters_workCorrectly()
@Test void equals_sameValues_returnsTrue()
@Test void hashCode_consistent()
```

#### HITO 5.2: DTOs (6 tests)
**Cobertura esperada: 0% → 50%**

```java
// Tests para BovineDTO, MilkingDTO, PastureDTO, ChatRequestDTO
@Test void validation_validDTO_passes()
@Test void validation_invalidField_throwsException()
@Test void builder_createsValidDTO()
```

#### HITO 5.3: Exceptions (4 tests)
**Cobertura esperada: 0% → 60%**

```java
@Test void serviceException_message_storesCorrectly()
@Test void repositoryException_cause_wrapsCorrectly()
@Test void notFoundException_throwable_worksCorrectly()
@Test void validationException_details_includesFieldErrors()
```

---

### FASE 6: Tests de Integración (Día 11) - +5% cobertura

#### HITO 6.1: Integración DynamoDB (5 tests)

```java
// DynamoDBIntegrationTest
@Test void bovineRepository_saveAndQuery_withLocalStack()
@Test void milkingRepository_queryByGSI_withRealDB()
@Test void pastureRepository_updateStatus_persists()
@Test void transactionTest_rollback_onError()
@Test void concurrentAccess_handlesCorrectly()
```

#### HITO 6.2: Integración End-to-End (5 tests)

```java
// ChatbotIntegrationTest
@Test void endToEnd_countBovines_fullFlow()
@Test void endToEnd_pregnancyQuery_withContext()
@Test void endToEnd_milkingQuery_aggregations()
@Test void endToEnd_errorHandling_graceful()
@Test void endToEnd_performance_underThreshold()
```

#### HITO 6.3: Integración Seguridad (5 tests)

```java
// SecurityIntegrationTest
@Test void jwtValidation_withRealToken_works()
@Test void rateLimiting_exceedsLimit_blocks()
@Test void corsPolicy_allowedOrigins_permits()
@Test void inputSanitization_sqlInjection_blocks()
@Test void unauthorizedAccess_denies()
```

---

## 📊 Proyección de Cobertura por Fase

| Fase | Días | Tests | Cobertura Esperada | Incremento |
|------|------|-------|-------------------|-----------|
| **Inicial** | - | 12 | **17%** | - |
| **Fase 1 (Services)** | 1-5 | 92 | **77%** | +60% |
| **Fase 2 (Repository)** | 6-7 | 35 | **82%** | +5% |
| **Fase 3 (Processor)** | 8 | 25 | **85%** | +3% |
| **Fase 4 (Builders/Events/Mapper)** | 9 | 37 | **88%** | +3% |
| **Fase 5 (DTOs/Entities/Exceptions)** | 10 | 18 | **89%** | +1% |
| **Fase 6 (Integración)** | 11 | 15 | **90%** | +1% |
| **TOTAL** | 11 | **222 tests** | **90%** ✅ | +73% |

---

## 🛠️ Herramientas y Configuración

### Dependencias (ya configuradas en build.gradle)

```gradle
testImplementation 'org.junit.jupiter:junit-jupiter-engine:5.11.0-M2'
testImplementation 'org.mockito:mockito-core:5.2.0'
testImplementation 'org.mockito:mockito-junit-jupiter:5.2.0'
testImplementation 'org.testcontainers:testcontainers:1.19.3'
testImplementation 'org.testcontainers:localstack:1.19.3'
testImplementation 'org.assertj:assertj-core:3.24.2'
testImplementation 'org.springframework.boot:spring-boot-starter-test:3.4.5'
```

### Configuración Jacoco

```gradle
jacoco {
  toolVersion = "0.8.11"
}

jacocoTestReport {
  dependsOn test
  reports {
    xml.required = true
    html.required = true
    csv.required = false
  }
  
  afterEvaluate {
    classDirectories.setFrom(files(classDirectories.files.collect {
      fileTree(dir: it, exclude: [
        '**/entities/**',  // Reducir threshold para POJOs
        '**/dtos/**',
        '**/enums/**',
        '**/*MapperImpl.class' // Generados por MapStruct
      ])
    }))
  }
}

// Verificación de cobertura mínima
jacocoTestCoverageVerification {
  violationRules {
    rule {
      limit {
        minimum = 0.85 // 85% mínimo
      }
    }
    
    rule {
      element = 'CLASS'
      limit {
        minimum = 0.80 // Por clase
      }
      excludes = [
        '*.entities.*',
        '*.dtos.*'
      ]
    }
  }
}

check.dependsOn jacocoTestCoverageVerification
```

---

## 📝 Convenciones y Mejores Prácticas

### Naming Convention

```java
// Patrón: methodName_scenario_expectedResult
@Test
void findById_existingId_returnsBovine() { }

@Test
void findById_nonExistingId_returnsEmpty() { }

@Test
void save_nullFarmId_throwsIllegalArgumentException() { }
```

### Patrón AAA (Arrange-Act-Assert)

```java
@Test
void countByFarmId_withMultipleBovines_returnsCorrectCount() {
    // Arrange
    String farmId = "farm-001";
    List<Bovine> bovineIdentityItems = TestDataBuilder.createBovineList(farmId, 5);
    when(bovineRepository.findAllByFarmId(farmId)).thenReturn(bovineIdentityItems);
    
    // Act
    Long result = bovineQueryService.countAllBovines(farmId);
    
    // Assert
    assertThat(result).isEqualTo(5L);
    verify(bovineRepository).findAllByFarmId(farmId);
}
```

### Uso de TestDataBuilder

```java
// Centralizar creación de datos de test
Bovine bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
Milking milkingRecord = TestDataBuilder.createMilking("farm-001", 123);
List<Bovine> bovineIdentityItems = TestDataBuilder.createBovineList("farm-001", 10);
```

### Tests Parametrizados

```java
@ParameterizedTest
@ValueSource(strings = {"cow", "bull", "heifer", "calf"})
void findByCategory_validCategory_returnsFiltered(String category) {
    // Test implementation
}

@ParameterizedTest
@CsvSource({
    "male, 5",
    "female, 10",
    "unknown, 0"
})
void countByGender_variousGenders_returnsCorrectCounts(String gender, int expected) {
    // Test implementation
}
```

---

## 📈 Métricas de Éxito

### Métricas Principales (KPIs)

1. **Cobertura de Código**
   - Objetivo: ≥ 85% instrucciones
   - Medición: Reporte Jacoco
   - Frecuencia: Por cada commit

2. **Velocidad de Tests**
   - Objetivo: Suite completa < 3 minutos
   - Medición: Gradle test task duration
   - Frecuencia: Diaria

3. **Calidad de Tests**
   - Objetivo: 0 tests flakey (intermitentes)
   - Medición: CI/CD pipeline failures
   - Frecuencia: Semanal

4. **Deuda Técnica**
   - Objetivo: Reducir de 83% a 10%
   - Medición: SonarQube technical debt ratio
   - Frecuencia: Semanal

### Métricas Secundarias

5. **Mutation Testing**
   - Objetivo: ≥ 75% mutation score (PIT)
   - Medición: PIT report
   - Frecuencia: Quincenal

6. **Code Smells**
   - Objetivo: < 10 code smells
   - Medición: SonarQube
   - Frecuencia: Semanal

---

## 🚀 Comandos de Ejecución

### Ejecutar Tests y Generar Reporte

```bash
# Limpiar y ejecutar todos los tests
./gradlew clean test jacocoTestReport

# Solo tests unitarios
./gradlew test

# Solo tests de integración
./gradlew integrationTest

# Ver reporte de cobertura
start build/reports/jacoco/test/html/index.html
```

### Verificar Cobertura Mínima

```bash
# Falla si cobertura < 85%
./gradlew jacocoTestCoverageVerification

# Ver resumen en consola
./gradlew test jacocoTestReport --info | grep "Total"
```

### Ejecutar Tests en Paralelo

```bash
./gradlew test --parallel --max-workers=4
```

---

## 📅 Cronograma Detallado

### Semana 1 (Días 1-5)

| Día | Hito | Tests | Cobertura Acumulada | Horas |
|-----|------|-------|-------------------|-------|
| **Día 1** | Services: Bovines + Milking | 28 | 35% (+18%) | 8h |
| **Día 2** | Services: Pasture | 15 | 47% (+12%) | 8h |
| **Día 3** | Services: Query Services | 22 | 60% (+13%) | 8h |
| **Día 4** | Services: Intent + Context | 14 | 70% (+10%) | 8h |
| **Día 5** | Services: Bedrock + Plan | 13 | 77% (+7%) | 8h |

### Semana 2 (Días 6-11)

| Día | Hito | Tests | Cobertura Acumulada | Horas |
|-----|------|-------|-------------------|-------|
| **Día 6** | Repository: Bovine + Milking | 25 | 82% (+5%) | 8h |
| **Día 7** | Repository: Plan + Counters | 10 | 83% (+1%) | 6h |
| **Día 8** | Processor: Todos | 25 | 85% (+2%) | 8h |
| **Día 9** | Builders + Events + Mapper | 37 | 88% (+3%) | 8h |
| **Día 10** | DTOs + Entities + Exceptions | 18 | 89% (+1%) | 6h |
| **Día 11** | Integración + Validación | 15 | 90% (+1%) | 7h |

**Total:** 11 días, 83 horas efectivas

---

## ⚠️ Riesgos y Mitigaciones

### Riesgos Identificados

| # | Riesgo | Probabilidad | Impacto | Mitigación |
|---|--------|--------------|---------|------------|
| R1 | Tests lentos > 3 min | Media | Alto | Usar TestContainers singleton, tests en paralelo |
| R2 | Mocks complejos difíciles | Alta | Medio | Documentar patrones, reutilizar TestDataBuilder |
| R3 | Flaky tests (intermitentes) | Media | Alto | Evitar sleeps, usar Awaitility, no tests dependientes |
| R4 | Cobertura no alcanza 85% | Baja | Crítico | Priorizar Services y Repository primero |
| R5 | LocalStack no disponible | Baja | Medio | Fallback a mocks puros, documentar prerequisito |
| R6 | Refactoring rompe tests | Media | Medio | Tests sobre interfaces, no implementación |
| R7 | Time budget insuficiente | Media | Alto | Priorizar fases 1-3, fases 4-6 opcionales |

---

## 🔗 Referencias

### Documentación Interna
- [HU-BEDROCK-002-TESTING.md](./HU-BEDROCK-002-TESTING.md) - Tests iniciales de chatbot
- [TESTING.md](../../TESTING.md) - Guía de testing del proyecto
- [Reporte Jacoco](../../../build/reports/jacoco/test/html/index.html)

### Documentación Externa
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [TestContainers Documentation](https://www.testcontainers.org/)
- [Jacoco Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

### Mejores Prácticas
- [Google Testing Blog](https://testing.googleblog.com/)
- [Martin Fowler - Testing Strategies](https://martinfowler.com/articles/practical-test-pyramid.html)
- [Uncle Bob - Test Induced Design Damage](https://blog.cleancoder.com/uncle-bob/2014/05/14/TheLittleMocker.html)

---

## ✅ Definition of Done (DoD)

### Código
- [ ] Todos los tests pasan (100% success rate)
- [ ] Cobertura general ≥ 85%
- [ ] Cobertura por paquete crítico ≥ 90%
- [ ] Sin warnings de compilación en tests
- [ ] Sin código comentado
- [ ] Sin tests ignorados (@Disabled) sin justificación

### Calidad
- [ ] Todos los tests siguen patrón AAA
- [ ] Naming convention aplicada consistentemente
- [ ] Sin tests duplicados
- [ ] Tests independientes (orden no importa)
- [ ] Mocks apropiados (no over-mocking)

### Documentación
- [ ] README actualizado con instrucciones
- [ ] Comentarios en tests complejos
- [ ] Casos edge documentados

### CI/CD
- [ ] Tests ejecutándose en pipeline
- [ ] Reporte Jacoco generado automáticamente
- [ ] Build falla si cobertura < 85%
- [ ] Performance de tests < 3 minutos

### Revisión
- [ ] Code review aprobado por tech lead
- [ ] QA validó reporte de cobertura
- [ ] Demo de tests ejecutándose correctamente

---

## 📞 Contacto y Soporte

**Tech Lead:** Equipo Cattle Development  
**QA Lead:** QA Team  
**Fecha Creación:** 2026-01-20  
**Última Actualización:** 2026-01-20  
**Versión:** 1.0

---

## 🏁 Notas Finales

Esta historia de usuario es **crítica** para la calidad del proyecto. La cobertura del 17% actual representa un riesgo significativo:

- **Riesgo de Bugs:** Sin tests, los bugs llegan a producción
- **Refactoring Imposible:** No podemos mejorar código sin red de seguridad
- **Deuda Técnica:** Crece exponencialmente con cada línea sin tests
- **Confianza del Equipo:** Baja sin validación automática

**La inversión de 11 días en testing salvará semanas de debugging futuro.**

---

**Estado:** ✅ READY FOR DEVELOPMENT  
**Aprobado por:** Tech Lead  
**Fecha Inicio Estimada:** 2026-01-22  
**Fecha Fin Estimada:** 2026-02-05
