# 🤖 HU-BEDROCK-001: Chatbot Inteligente - Implementación

**ID**: HU-BEDROCK-001  
**Prioridad**: 🔴 CRÍTICA  
**Estimación**: 13 puntos  
**Sprint**: S-1 (Enero 2026)  
**Estado**: ✅ Refinado (Developer)  

---

## 📋 Descripción

Como **usuario de la plataforma Cattle**, quiero **hacer consultas en lenguaje natural sobre mis bovinos** (cantidad, edades, estado reproductivo, etc.) y recibir **respuestas inteligentes contextualizadas basadas en datos reales de DynamoDB**, para poder **tomar decisiones de gestión ganadera sin necesidad de navegar interfaces complejas**.

---

## 🎯 Criterios de Aceptación

### CA-001: Detección de Intención ✅

```gherkin
Scenario: Detectar intención de consulta sobre cantidad de bovinos
  Given el usuario pregunta "¿Cuántas vacas tengo?"
  When la Lambda procesa la intención
  Then identifica correctamente "COUNT_BOVINES"
  And detecta categoría "cow"
  And confirma filtro correcto
```

**Detalles**:
- Detectar 5+ tipos de intención: COUNT_BOVINES, GET_BOVINE, COUNT_BY_STATUS, AGGREGATE_MILKING, PASTURE_STATUS
- Precisión mínima: 95%

### CA-002: Consulta a DynamoDB ✅

```gherkin
Scenario: Ejecutar query optimizado a DynamoDB
  Given el sistema detectó intención COUNT_PREGNANT_COWS
  When Lambda ejecuta query con GSI1
  Then obtiene resultados en menos de 200ms
  And retorna solo campos necesarios
  And no devuelve datos sensibles no autorizados
```

### CA-003: Integración con Bedrock ✅

```gherkin
Scenario: Generar respuesta con Claude 3 Haiku
  Given tengo datos de DynamoDB (45 vacas preñadas)
  When CallBedrockService.invoke()
  Then genera respuesta en español < 5 segundos
  And incluye contexto ganadero
  And no devuelve información no autorizada
```

### CA-004: Error Handling ✅

```gherkin
Scenario: Manejar errores sin exponer detalles técnicos
  Given una query falla en DynamoDB
  When Lambda captura la excepción
  Then retorna error genérico al usuario
  And registra detalle en CloudWatch
  And no expone stack trace
```

### CA-005: Logging Estructurado ✅

```gherkin
Scenario: Registrar todas las operaciones
  Given cada query ejecutada
  When sistema completa operación
  Then genera log estructurado JSON
  And incluye: timestamp, userID, intent, duration, result
  And es consultable en CloudWatch
```

### CA-006: Autenticación JWT ✅

```gherkin
Scenario: Validar token JWT en cada request
  Given request con Authorization header
  When Lambda valida token
  Then verifica firma correctamente
  And extrae farmID del token
  And rechaza requests sin token válido
```

### CA-007: Rate Limiting ✅

```gherkin
Scenario: Limitar requests por usuario
  Given usuario realiza 100+ queries en 1 hora
  When Lambda recibe request 101
  Then rechaza con error 429 (Too Many Requests)
  And registra en CloudWatch
  And información disponible en headers de respuesta
```

### CA-008: Respuesta Contextualizada ✅

```gherkin
Scenario: Incluir contexto ganadero en respuesta
  Given query sobre "producción promedio"
  When Bedrock genera respuesta
  Then incluye datos estadísticos
  And compara con promedio de rebaño
  And sugiere acciones si es necesario
```

---

## 🏗️ Arquitectura

```
Usuario Request
    ↓
[AWS API Gateway]
    ↓
[Lambda: cattle-lambda-function]
    ├─→ IntentDetectionService (NLP)
    ├─→ CattleQueryService (DynamoDB)
    ├─→ ContextBuilderService (Data formatting)
    ├─→ BedrockService (Claude 3 Haiku)
    ├─→ ChatbotService (Orchestrator)
    └─→ ChatbotController (Response)
    ↓
Response (JSON)
```

---

## 💻 Componentes a Implementar

### 1. **DynamoDbConfig**
- Configuración de cliente DynamoDB
- Inicialización de tablas
- GSI (Global Secondary Indexes)

### 2. **QueryIntent (Enum)**
- COUNT_BOVINES
- GET_BOVINE
- COUNT_BY_STATUS
- AGGREGATE_MILKING
- PASTURE_STATUS

### 3. **IntentDetectionService**
- Análisis de intención de usuario
- Extracción de entidades (categoría, filtros)
- Validación de intención

### 4. **CattleQueryService**
- Consultas a DynamoDB
- Optimización con GSI
- Paginación si es necesario

### 5. **ContextBuilderService**
- Formateo de datos para Bedrock
- Resumen de información
- Contexto ganadero

### 6. **BedrockService**
- Invocación de Claude 3 Haiku
- Manejo de timeout (< 5 segundos)
- Fallback responses

### 7. **ChatbotService**
- Orquestación de flujo
- Manejo de errores
- Logging estructurado

### 8. **ChatbotController**
- Endpoint REST
- Validación JWT
- Rate limiting
- Response formatting

---

## 📊 Fase 1: Setup (Día 1-2)

- [ ] Crear estructura Maven/Gradle
- [ ] Configurar dependencias Spring Boot, AWS SDK
- [ ] Setup local DynamoDB con LocalStack
- [ ] Configurar variables de entorno

## 📊 Fase 2: Servicios Base (Día 3-5)

- [ ] Implementar DynamoDbConfig
- [ ] Implementar IntentDetectionService
- [ ] Implementar CattleQueryService
- [ ] Implementar ContextBuilderService

## 📊 Fase 3: Integración Bedrock (Día 6)

- [ ] Implementar BedrockService
- [ ] Integración con Claude 3 Haiku
- [ ] Testing en Dev

## 📊 Fase 4: Orquestación (Día 7)

- [ ] Implementar ChatbotService
- [ ] Implementar ChatbotController
- [ ] Agregar seguridad (JWT, rate limiting)

## 📊 Fase 5: Testing (Día 8-9)

- [ ] Tests unitarios (35+)
- [ ] Tests de integración (8+)
- [ ] Tests de seguridad (12+)
- [ ] Cobertura ≥ 85%

## 📊 Fase 6: Validación (Día 10)

- [ ] Code review
- [ ] Smoke tests
- [ ] Deploy a Dev
- [ ] Validación de criterios de aceptación

---

## Análisis Arquitectónico (Arquitecto)

### Decisiones de Diseño

**Patrón Arquitectónico:** Layered Architecture con Orchestration Pattern (Arquitectura en Capas con Orquestador)

**Justificación:** Este patrón se seleccionó por coherencia con la arquitectura existente de cattle-lambda-function que ya utiliza Spring Boot con separación clara de responsabilidades (Controller → Service → Repository). Proporciona separación de responsabilidades claras entre capa de presentación (ChatbotController), capa de orquestación (ChatbotService), capa de servicios de dominio (IntentDetectionService, ContextBuilderService, BedrockService) y capa de acceso a datos (BovineQueryService, MilkingQueryService, PastureQueryService + Repositories). Permite escalabilidad modular para agregar nuevos tipos de consultas sin modificar el núcleo, mejora la testabilidad al permitir que cada capa se testee independientemente con mocks, y se alinea perfectamente con el modelo serverless de AWS Lambda donde cada invocación es stateless.

**Componentes Afectados:**

- **IntentDetectionService (Nuevo):** Servicio de detección de intención del usuario. Analizar mensaje del usuario y detectar intención (COUNT_BOVINES, COUNT_BY_STATUS, GET_BOVINE, etc.), extraer entidades (categoría, género, filtros). Implementación basada en palabras clave y expresiones regulares. Retorna enum QueryIntent con entidades extraídas en IntentContext.

- **BovineQueryService (Nuevo):** Servicio de consultas de bovinos a DynamoDB. Ejecutar queries optimizadas a DynamoDB tabla TABLE_CATTLE, usar GSI1 para consultas por farm/status/categoría. Métodos: countAllBovines(farmId), countByCategory(farmId), countByGender(farmId), countPregnantBovines(farmId), getAgeDistribution(farmId), getCalvesForWeaning(farmId).

- **MilkingQueryService (Nuevo):** Servicio de consultas de lactancia a DynamoDB. Consultas a TABLE_FARM_MILKING para estadísticas de producción láctea. Métodos: getMonthlyAverageProduction(farmId), getTopProducerBovine(farmId) con agregación de producción por bovineId.

- **PastureQueryService (Nuevo):** Servicio de consultas de potreros a DynamoDB. Consultas a TABLE_PASTURE sobre disponibilidad y rotación. Métodos: getAvailablePastures(farmId) filtrando por status="DISPONIBLE", getTotalHectaresInUse(farmId) sumando areaHa donde status="EN_USO".

- **ContextBuilderService (Nuevo):** Constructor de contexto para enriquecer prompts de Bedrock. Orquestar llamadas a query services (BovineQueryService, MilkingQueryService, PastureQueryService), formatear datos en texto contextualizado para Bedrock, enriquecer prompt del usuario con datos reales. Método principal buildContext(IntentContext intent, String farmId) retorna String con contexto formateado.

- **BovineRepository (Extensión):** Repositorio de acceso a datos de bovinos en DynamoDB. Extender repository existente con métodos optimizados para queries del chatbot. Usar GSI1 para consultas por farmId. Métodos a agregar: countByFarmId, findByFarmIdAndCategory, findByFarmIdAndGender, findByFarmIdAndStatus.

- **MilkingRepository (Nuevo):** Repositorio de acceso a datos de lactancia en DynamoDB. Acceso a datos de lactancia con queries sobre TABLE_FARM_MILKING usando entity Milking.java existente.

- **PastureRepository (Nuevo):** Repositorio de acceso a datos de potreros en DynamoDB. Acceso a datos de potreros con queries sobre TABLE_PASTURE usando entity Pasture.java existente.

- **ChatbotService (Nuevo):** Orquestador principal del flujo del chatbot.
  - Nivel de cambio: Creación
  - Especificaciones: Crear servicio para orquestar flujo completo: Intent Detection → Context Building → Bedrock Invocation → Response Formatting. Inyectar IntentDetectionService, ContextBuilderService, BedrockService. Implementar rate limiting básico con contador en memoria. Agregar métricas de performance (duración). Manejo centralizado de errores y logging estructurado.

- **BedrockService (Nuevo):** Cliente especializado de Amazon Bedrock.
  - Nivel de cambio: Creación
  - Especificaciones: Crear servicio para invocación de Claude 3 Haiku. Método principal invokeModel(String prompt). Construcción de payload JSON según API de Bedrock, parsing de respuesta. Mantener timeout < 5 segundos.

- **ChatbotController (Nuevo):** Controlador REST de endpoints del chatbot.
  - Nivel de cambio: Creación
  - Especificaciones: Crear endpoint POST /api/chat/message. Validación de JWT token en Authorization header, extraer farmId del JWT para pasarlo a ChatbotService, aplicar rate limiting, agregar headers CORS adecuados, retornar headers con información de rate limit.

- **ChatRequestDTO (Nuevo):** DTO de entrada del chatbot.
  - Nivel de cambio: Creación
  - Especificaciones: Campos: String userMessage, String farmId (opcional, se extrae de JWT en controller).

- **ChatResponseDTO (Nuevo):** DTO de salida del chatbot.
  - Nivel de cambio: Creación
  - Especificaciones: Campos: String response, String intent (intención detectada), Long durationMs (tiempo de procesamiento total).

**Hitos de Implementación:**

1. **Entidades y Enums de Dominio** - Verificar entities existentes y crear estructuras: QueryIntent (enum), IntentContext (clase), DTOs de contexto. Entities Bovine, Milking, Pasture YA EXISTEN.
   - Dependencias: Ninguna (base del sistema)

2. **Capa de Repositorios** - Extender BovineRepository existente y crear MilkingRepository, PastureRepository
   - Dependencias: Entidades verificadas en hito 1

3. **Servicios de Consulta de Dominio** - Lógica de negocio de queries: BovineQueryService, MilkingQueryService, PastureQueryService
   - Dependencias: Repositorios del hito 2

4. **Servicio de Detección de Intención** - Análisis NLP básico: IntentDetectionService
   - Dependencias: QueryIntent enum del hito 1

5. **Servicio de Construcción de Contexto** - Orquestador de datos: ContextBuilderService
   - Dependencias: Servicios de consulta del hito 3, IntentContext del hito 1

6. **Refactoring de BedrockService** - Cliente IA aislado: BedrockService extraído de ChatbotService
   - Dependencias: BedrockConfig existente

7. **Orquestador Principal ChatbotService** - Flujo end-to-end: ChatbotService refactorizado
   - Dependencias: IntentDetectionService (hito 4), ContextBuilderService (hito 5), BedrockService (hito 6)

8. **Controlador y DTOs** - Capa de API REST: ChatbotController modificado, ChatRequestDTO, ChatResponseDTO actualizados
   - Dependencias: ChatbotService del hito 7

### Validación de Impacto

**Código fuente verificado en cattle-lambda-function:**
- Entities existentes: Bovine.java, Milking.java, Pasture.java ya implementadas con anotaciones DynamoDB
- BovineRepository.java existe con patrón repository funcional usando DynamoDbEnhancedClient
- Servicios básicos: BovinesService, MilkingService implementados
- Componentes del chatbot NO existentes: ChatbotService, ChatbotController, BedrockService, IntentDetectionService, ContextBuilderService deben crearse
- BovineQueryService, MilkingQueryService, PastureQueryService deben crearse como servicios especializados de consulta

**Código fuente verificado en cattle-lambda-function (patrón de referencia):**
- BovineRepository.java implementa patrón repository con DynamoDbEnhancedClient funcional, utiliza queries con GSI1 optimizadas
- Bovine.java es entity completa con anotaciones @DynamoDbBean, incluye PK, SK, GSI1-GSI5 para queries multi-índice
- Milking.java es entity de lactancia con GSI3 para queries eficientes por bovineId
- Pasture.java es entity de potreros con GSI1 y GSI2 para queries por farm/species

**Dependencias verificadas:**
- build.gradle ya incluye todas las dependencias necesarias: software.amazon.awssdk:dynamodb:2.32.16, software.amazon.awssdk:dynamodb-enhanced:2.32.16, software.amazon.awssdk:bedrockruntime:2.29.6
- No se requieren nuevas dependencias externas en build.gradle
- Acción requerida: Las entities (Bovine, Milking, Pasture) YA existen en cattle-lambda-function/src/main/java/com/cattle/entities/ y pueden ser reutilizadas directamente

**Cadena de invocación completa:**
```
API Gateway → ChatbotController.sendMessage()
  → ChatbotService.chat()
    → IntentDetectionService.detectIntent() [NUEVO]
    → ContextBuilderService.buildContext() [NUEVO]
      → BovineQueryService.countX() [NUEVO]
      → MilkingQueryService.getAverage() [NUEVO]
      → PastureQueryService.getAvailable() [NUEVO]
        → BovineRepository.query() [NUEVO]
        → MilkingRepository.query() [NUEVO]
        → PastureRepository.query() [NUEVO]
          → DynamoDB (TABLE_CATTLE, TABLE_FARM_MILKING, TABLE_PASTURE)
    → BedrockService.invokeModel() [REFACTOR]
      → Amazon Bedrock Claude 3 Haiku
  → ChatResponseDTO
→ Response HTTP 200
```

**Análisis de performance:**
- Queries a DynamoDB: < 200ms según criterio de aceptación CA-002 utilizando GSI optimizados
- Invocación Bedrock: < 5 segundos según criterio de aceptación CA-003
- Overhead de orquestación estimado: ~50ms (negligible)
- Total esperado: 300ms - 5.3 segundos (dentro del timeout de Lambda de 30 segundos)

**Consumidores identificados:**
- cattle-front (frontend React): consumirá endpoint /api/chat/message desde interfaz de chatbot
- Otros clientes HTTP/REST: API pública expuesta vía API Gateway

**Hallazgos críticos documentados:**
1. DynamoDB Enhanced Client ya configurado correctamente - No requiere setup adicional de infraestructura
2. No existe autenticación JWT en ChatbotController actual - Requiere implementación completa para cumplir CA-006
3. No existe rate limiting implementado - Debe implementarse a nivel de servicio o API Gateway para cumplir CA-007
4. Bedrock invocation funcional verificado - Código actual ya invoca Claude 3 Haiku exitosamente con modelo anthropic.claude-3-haiku-20240307-v1:0
5. Logging no estructurado actualmente - ChatbotService usa log.error() simple, requiere migración a JSON estructurado para cumplir CA-005
6. Entities YA existen en cattle-lambda-function - Bovine.java, Milking.java, Pasture.java están implementadas y se reutilizarán directamente

**Decisiones arquitectónicas clave:**

**DA-001: Separación de BedrockService del ChatbotService**
- Decisión: Extraer lógica de invocación Bedrock a servicio dedicado
- Justificación: Facilita testing con mocks, permite reutilización para otras funcionalidades futuras, cumple Single Responsibility Principle

**DA-002: Query Services por dominio (Bovine, Milking, Pasture)**
- Decisión: Crear servicios especializados en lugar de un único "QueryService"
- Justificación: Alta cohesión por dominio, facilita mantenimiento independiente, permite escalar cada dominio según necesidades

**DA-003: ContextBuilderService como orquestador de datos**
- Decisión: Servicio intermedio que orquesta múltiples query services
- Justificación: Desacopla IntentDetectionService de las fuentes de datos específicas, permite agregar nuevas fuentes sin modificar detección de intención, centraliza lógica de formateo de contexto

**DA-004: IntentDetectionService basado en keywords (no ML)**
- Decisión: Implementación inicial con expresiones regulares y palabras clave
- Justificación: Simplicidad para MVP, 95% de precisión alcanzable con patrones bien definidos, evita overhead de modelo ML adicional, puede evolucionar a ML en futuras versiones

**DA-005: Reutilización de Repositories en cattle-lambda-function**
- Decisión: Reutilizar entities existentes y extender repositories con métodos optimizados para chatbot
- Justificación: Evita duplicación de código, mantiene consistencia en acceso a datos, BovineRepository existente ya implementa patrón correcto con DynamoDbEnhancedClient, permite agregar métodos especializados de lectura para queries del chatbot

**DA-006: FarmId desde JWT en ChatbotController**
- Decisión: Extraer farmId del token JWT en la capa de controller
- Justificación: Seguridad multi-tenant (cada usuario solo ve sus datos), evita pasar farmId en body (manipulable), alineado con patrón OAuth2 existente en cattle-front

### Referencias y Validación

**Documentación consultada:**
- ARCHITECTURE.md - Diagramas C4 del sistema, arquitectura Lambda + Spring Boot
- ARQUITECTURA-ECOSISTEMA-CATTLE.md - GPS arquitectónico, esquemas de DynamoDB (TABLE_CATTLE, TABLE_FARM_MILKING, TABLE_PASTURE), casos de uso del chatbot
- GUIA-INTEGRACION-CHATBOT-DYNAMODB.md - Roadmap de integración técnica, ejemplos de queries DynamoDB, flujo objetivo del chatbot
- Código fuente cattle-lambda-function - Verificación de componentes existentes: BovineRepository (patrón repository funcional), entities (Bovine, Milking, Pasture), servicios de dominio (BovinesService)
- Componentes del chatbot a crear: ChatbotService, ChatbotController, BedrockService, IntentDetectionService, ContextBuilderService

**Historias relacionadas:**
- Historia HU-BEDROCK-002 (Testing): Suite de tests para validar implementación (35+ unit tests, 8+ integration tests, cobertura ≥85%)
- Historia HU-BEDROCK-003 (Seguridad): Validación JWT, rate limiting, logging estructurado, análisis de vulnerabilidades
- Historia HU-BEDROCK-004 (Documentación): Documentación técnica completa, guías de API, troubleshooting

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-16 | **Enfoque:** Exploratorio

---

## 🔧 Refinamiento Técnico (Developer)

### Plan de Implementación Detallado

**Estimación Total**: 12 puntos (72-76 horas efectivas, ~10 días laborales)

### HITO 1: Entidades y Enums de Dominio (6h - Día 1)

**Objetivo**: Crear estructuras de datos base del sistema

**Tareas Técnicas**:

**T1.1: Verificar Entities existentes (0.5h)**
- **Archivos a verificar**:
  - `src/main/java/com/cattle/entities/Bovine.java` (YA EXISTE)
  - `src/main/java/com/cattle/entities/Milking.java` (YA EXISTE)
  - `src/main/java/com/cattle/entities/Pasture.java` (YA EXISTE)
- **Verificación**: Entities compilables con anotaciones @DynamoDbBean
- **Dependencias**: Ninguna
- **Estimación**: 0.5 horas

**T1.2: Crear Enum QueryIntent (1h)**
- **Archivo a crear**: `src/main/java/com/cattle/enums/QueryIntent.java`
- **Valores del enum**:
  ```java
  public enum QueryIntent {
      COUNT_BOVINES,           // "¿Cuántos bovinos tengo?"
      COUNT_BY_CATEGORY,       // "¿Cuántas vacas tengo?"
      COUNT_BY_STATUS,         // "¿Cuántos bovinos preñados?"
      COUNT_BY_GENDER,         // "¿Cuántos machos tengo?"
      GET_BOVINE_DETAILS,      // "Detalles del bovino 123"
      AGGREGATE_MILKING,       // "Producción promedio"
      PASTURE_STATUS,          // "¿Qué potreros están disponibles?"
      GENERAL_QUERY            // Fallback para queries no clasificadas
  }
  ```
- **Verificación**: Enum compilable
- **Dependencias**: Ninguna
- **Estimación**: 1 hora

**T1.3: Crear clase IntentContext (3h)**
- **Archivo a crear**: `src/main/java/com/cattle/dtos/IntentContext.java`
- **Campos**:
  ```java
  @Data
  @Builder
  public class IntentContext {
      private QueryIntent intent;           // Intención detectada
      private String category;              // "cow", "bull", "calf", etc.
      private String gender;                // "male", "female"
      private String status;                // "pregnant", "lactating", etc.
      private Map<String, String> filters;  // Filtros adicionales extraídos
      private Double confidenceScore;       // Confianza de detección (0-1)
  }
  ```
- **Verificación**: Clase compilable con Lombok
- **Dependencias**: T1.2 (QueryIntent)
- **Estimación**: 3 horas

**T1.4: Crear DTOs de contexto para queries (2h)**
- **Archivos a crear**:
  - `src/main/java/com/cattle/dtos/BovineContextDTO.java` - Datos simplificados de bovino para contexto
  - `src/main/java/com/cattle/dtos/MilkingContextDTO.java` - Datos de lactancia para contexto
  - `src/main/java/com/cattle/dtos/PastureContextDTO.java` - Datos de potrero para contexto
- **Verificación**: DTOs compilables con Lombok
- **Dependencias**: T1.1 (Entities)
- **Estimación**: 2 horas

---

### HITO 2: Capa de Repositorios (8h - Días 2-3)

**Objetivo**: Extender repositorios existentes con métodos optimizados para chatbot

**Tareas Técnicas**:

**T2.1: Extender BovineRepository con métodos de consulta (2h)**
- **Archivo a modificar**: `src/main/java/com/cattle/repository/BovineRepository.java` (YA EXISTE)
- **Métodos a agregar**:
  ```java
  // Métodos adicionales para chatbot
  public Long countByFarmId(String farmId);
  public List<Bovine> findByFarmIdAndCategory(String farmId, String category);
  public List<Bovine> findByFarmIdAndGender(String farmId, String gender);
  public List<Bovine> findByFarmIdAndStatus(String farmId, String status);
  ```
- **Consideraciones**: Usar GSI1 para queries optimizadas por farmId
- **Verificación**: Métodos compilables, queries funcionales
- **Dependencias**: T1.1 (Bovine entity verificada)
- **Estimación**: 2 horas

**T2.2: Crear MilkingRepository (3h)**
- **Archivo a crear**: `src/main/java/com/cattle/repository/MilkingRepository.java`
- **Métodos a implementar**:
  ```java
  @Repository
  public class MilkingRepository {
      public Optional<List<Milking>> findByFarmId(String farmId);
      public Optional<List<Milking>> findByFarmIdAndDateRange(String farmId, 
                                                                LocalDate start, 
                                                                LocalDate end);
      public Optional<List<Milking>> findByBovineId(String bovineId);
      public Double calculateAverageProduction(String farmId, LocalDate start, LocalDate end);
  }
  ```
- **Consideraciones**: Queries por rango de fechas, agregaciones de producción
- **Verificación**: Métodos compilables, cálculos correctos
- **Dependencias**: T1.1 (Milking entity verificada)
- **Estimación**: 3 horas

**T2.3: Crear PastureRepository (3h)**
- **Archivo a crear**: `src/main/java/com/cattle/repository/PastureRepository.java`
- **Métodos a implementar**:
  ```java
  @Repository
  public class PastureRepository {
      public Optional<List<Pasture>> findByFarmId(String farmId);
      public Optional<List<Pasture>> findByFarmIdAndStatus(String farmId, String status);
      public Double calculateTotalHectares(String farmId, String status);
      public Optional<Pasture> findById(String pk, String sk);
  }
  ```
- **Consideraciones**: Filtros por status ("DISPONIBLE", "EN_USO"), cálculo de hectáreas
- **Verificación**: Métodos compilables, filtros funcionales
- **Dependencias**: T1.1 (Pasture entity verificada)
- **Estimación**: 3 horas

---

### HITO 3: Servicios de Consulta de Dominio (18h - Días 4-5)

**Objetivo**: Implementar lógica de negocio de queries

**Tareas Técnicas**:

**T3.1: Implementar BovineQueryService (6h)**
- **Archivo a crear**: `src/main/java/com/cattle/services/BovineQueryService.java`
- **Métodos a implementar**:
  ```java
  @Service
  public class BovineQueryService {
      @Autowired
      private BovineRepository bovineRepository;
      
      public Long countAllBovines(String farmId);
      public Map<String, Long> countByCategory(String farmId);
      public Map<String, Long> countByGender(String farmId);
      public Long countPregnantBovines(String farmId);
      public Map<String, Integer> getAgeDistribution(String farmId);
      public List<BovineContextDTO> getCalvesForWeaning(String farmId);
      public List<BovineContextDTO> getBovinesNearCalving(String farmId, int daysThreshold);
  }
  ```
- **Lógica de negocio**:
  - Agregaciones y conteos
  - Cálculo de edades desde `bornDate`
  - Filtros por categoría, género, status
- **Verificación**: Métodos compilables, lógica de negocio correcta
- **Dependencias**: T2.1 (BovineRepository), T1.4 (BovineContextDTO)
- **Estimación**: 6 horas

**T3.2: Implementar MilkingQueryService (6h)**
- **Archivo a crear**: `src/main/java/com/cattle/services/MilkingQueryService.java`
- **Métodos a implementar**:
  ```java
  @Service
  public class MilkingQueryService {
      @Autowired
      private MilkingRepository milkingRepository;
      
      public Double getMonthlyAverageProduction(String farmId);
      public Double getWeeklyAverageProduction(String farmId);
      public MilkingContextDTO getTopProducerBovine(String farmId);
      public Map<String, Double> getProductionByShift(String farmId);
      public List<MilkingContextDTO> getRecentMilkings(String farmId, int days);
  }
  ```
- **Lógica de negocio**:
  - Cálculo de promedios de producción
  - Identificación de top producer
  - Agregación por turno
- **Verificación**: Métodos compilables, cálculos correctos
- **Dependencias**: T2.2 (MilkingRepository), T1.4 (MilkingContextDTO)
- **Estimación**: 6 horas

**T3.3: Implementar PastureQueryService (6h)**
- **Archivo a crear**: `src/main/java/com/cattle/services/PastureQueryService.java`
- **Métodos a implementar**:
  ```java
  @Service
  public class PastureQueryService {
      @Autowired
      private PastureRepository pastureRepository;
      
      public List<PastureContextDTO> getAvailablePastures(String farmId);
      public List<PastureContextDTO> getPasturesInUse(String farmId);
      public Double getTotalHectaresInUse(String farmId);
      public Double getTotalAvailableHectares(String farmId);
      public Map<String, Integer> getPastureCountByStatus(String farmId);
  }
  ```
- **Lógica de negocio**:
  - Filtros por status
  - Cálculo de hectáreas totales
  - Agregación por status
- **Verificación**: Métodos compilables, filtros funcionales
- **Dependencias**: T2.3 (PastureRepository), T1.4 (PastureContextDTO)
- **Estimación**: 6 horas

---

### HITO 4: Servicio de Detección de Intención (10h - Día 6)

**Objetivo**: Implementar análisis NLP básico con keywords

**Tareas Técnicas**:

**T4.1: Implementar IntentDetectionService (8h)**
- **Archivo a crear**: `src/main/java/com/cattle/services/IntentDetectionService.java`
- **Métodos a implementar**:
  ```java
  @Service
  public class IntentDetectionService {
      public IntentContext detectIntent(String userMessage);
      private QueryIntent classifyIntent(String message);
      private String extractCategory(String message);
      private String extractGender(String message);
      private String extractStatus(String message);
      private Map<String, String> extractFilters(String message);
      private Double calculateConfidence(String message, QueryIntent intent);
  }
  ```
- **Lógica de detección**:
  - Patterns para COUNT_BOVINES: "cuántos", "cuántas", "cantidad de"
  - Patterns para categorías: "vacas", "toros", "terneros", "novillas"
  - Patterns para status: "preñadas", "lactando", "disponibles"
  - Patterns para gender: "machos", "hembras"
  - Confidence score basado en match exacto de patterns
- **Verificación**: Precisión ≥ 95% en test cases conocidos
- **Dependencias**: T1.2 (QueryIntent), T1.3 (IntentContext)
- **Estimación**: 8 horas

**T4.2: Crear tests unitarios de IntentDetectionService (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/IntentDetectionServiceTest.java`
- **Casos de test**:
  - "¿Cuántas vacas tengo?" → COUNT_BY_CATEGORY, category=cow
  - "¿Cuántos bovinos preñados?" → COUNT_BY_STATUS, status=pregnant
  - "¿Cuántos machos tengo?" → COUNT_BY_GENDER, gender=male
  - "Producción promedio" → AGGREGATE_MILKING
  - "Potreros disponibles" → PASTURE_STATUS, status=disponible
- **Verificación**: Tests pasan con ≥95% precisión
- **Dependencias**: T4.1 (IntentDetectionService)
- **Estimación**: 2 horas

---

### HITO 5: Servicio de Construcción de Contexto (12h - Día 7)

**Objetivo**: Orquestar queries y formatear contexto para Bedrock

**Tareas Técnicas**:

**T5.1: Implementar ContextBuilderService (10h)**
- **Archivo a crear**: `src/main/java/com/cattle/services/ContextBuilderService.java`
- **Métodos a implementar**:
  ```java
  @Service
  public class ContextBuilderService {
      @Autowired
      private BovineQueryService bovineQueryService;
      @Autowired
      private MilkingQueryService milkingQueryService;
      @Autowired
      private PastureQueryService pastureQueryService;
      
      public String buildContext(IntentContext intent, String farmId);
      private String buildBovineContext(IntentContext intent, String farmId);
      private String buildMilkingContext(String farmId);
      private String buildPastureContext(String farmId);
      private String formatContextForBedrock(Map<String, Object> data);
  }
  ```
- **Lógica de construcción**:
  - Según intent, llamar servicios correspondientes
  - Formatear datos en texto legible para Bedrock
  - Incluir estadísticas relevantes
  - Mantener contexto < 2000 caracteres
- **Verificación**: Contexto generado es coherente y completo
- **Dependencias**: T3.1, T3.2, T3.3 (Query Services), T1.3 (IntentContext)
- **Estimación**: 10 horas

**T5.2: Crear tests unitarios de ContextBuilderService (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/ContextBuilderServiceTest.java`
- **Casos de test**:
  - Intent COUNT_BOVINES genera contexto con estadísticas generales
  - Intent COUNT_BY_STATUS incluye conteos específicos
  - Intent AGGREGATE_MILKING incluye promedios de producción
  - Contexto formateado es parseable y < 2000 caracteres
- **Verificación**: Tests pasan, contexto bien formado
- **Dependencias**: T5.1 (ContextBuilderService)
- **Estimación**: 2 horas

---

### HITO 6: Refactoring de BedrockService (6h - Día 8 mañana)

**Objetivo**: Extraer lógica de Bedrock a servicio dedicado

**Tareas Técnicas**:

**T6.1: Refactorizar BedrockService (4h)**
- **Archivo a modificar**: `src/main/java/com/cattle/services/BedrockService.java` (crear nuevo)
- **Código a extraer**: Desde ChatbotService.chat() actual
- **Métodos a implementar**:
  ```java
  @Service
  public class BedrockService {
      @Autowired
      private BedrockRuntimeClient bedrockRuntimeClient;
      
      public String invokeModel(String prompt);
      private String buildBedrockPayload(String prompt);
      private String parseBedrockResponse(String responseBody);
      private void handleBedrockTimeout();
  }
  ```
- **Consideraciones**:
  - Timeout < 5 segundos
  - Manejo de errores de Bedrock
  - Logging de invocaciones
- **Verificación**: Servicio funcional, timeout enforced
- **Dependencias**: BedrockConfig existente
- **Estimación**: 4 horas

**T6.2: Crear tests unitarios de BedrockService (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/BedrockServiceTest.java`
- **Casos de test**:
  - invokeModel retorna respuesta válida con mock
  - Timeout detectado y manejado
  - Error de Bedrock capturado correctamente
- **Verificación**: Tests pasan con mocks de Bedrock
- **Dependencias**: T6.1 (BedrockService)
- **Estimación**: 2 horas

---

### HITO 7: Orquestador Principal ChatbotService (10h - Día 8-9)

**Objetivo**: Refactorizar flujo end-to-end del chatbot

**Tareas Técnicas**:

**T7.1: Refactorizar ChatbotService (6h)**
- **Archivo a modificar**: `src/main/java/com/cattle/services/ChatbotService.java`
- **Cambios principales**:
  - Inyectar IntentDetectionService, ContextBuilderService, BedrockService
  - Refactorizar método chat() para orquestar flujo completo
  - Agregar rate limiting básico (contador en memoria)
  - Agregar logging estructurado JSON
  - Agregar métricas de duración
- **Flujo refactorizado**:
  ```java
  public ChatResponseDTO chat(String farmId, ChatRequestDTO request) {
      long startTime = System.currentTimeMillis();
      
      // 1. Detectar intención
      IntentContext intent = intentDetectionService.detectIntent(request.getUserMessage());
      
      // 2. Construir contexto
      String context = contextBuilderService.buildContext(intent, farmId);
      
      // 3. Invocar Bedrock con contexto
      String enrichedPrompt = context + "\n\nPregunta: " + request.getUserMessage();
      String response = bedrockService.invokeModel(enrichedPrompt);
      
      // 4. Formatear respuesta
      long duration = System.currentTimeMillis() - startTime;
      return ChatResponseDTO.builder()
          .response(response)
          .intent(intent.getIntent().name())
          .durationMs(duration)
          .timestamp(LocalDateTime.now())
          .build();
  }
  ```
- **Verificación**: Flujo funcional end-to-end
- **Dependencias**: T4.1, T5.1, T6.1 (Servicios)
- **Estimación**: 6 horas

**T7.2: Crear tests unitarios de ChatbotService (4h)**
- **Archivo a crear**: `src/test/java/com/cattle/services/ChatbotServiceTest.java`
- **Casos de test**:
  - Flujo completo con mocks funciona
  - Intent detection invocado correctamente
  - Context building recibe intent correcto
  - Bedrock invocado con prompt enriquecido
  - Duración medida correctamente
  - Rate limiting enforced
- **Verificación**: Tests pasan con mocks
- **Dependencias**: T7.1 (ChatbotService refactorizado)
- **Estimación**: 4 horas

---

### HITO 8: Controlador y DTOs (6h - Día 10 mañana)

**Objetivo**: Actualizar capa de API REST

**Tareas Técnicas**:

**T8.1: Modificar ChatbotController (2h)**
- **Archivo a modificar**: `src/main/java/com/cattle/controller/ChatbotController.java`
- **Cambios**:
  - Agregar extracción de farmId de JWT (placeholder hasta HU-BEDROCK-003)
  - Agregar headers de rate limit en response
  - Pasar farmId a ChatbotService
  - Mejorar manejo de errores
- **Método modificado**:
  ```java
  @PostMapping("/message")
  public ResponseEntity<ChatResponseDTO> sendMessage(
      @RequestHeader("Authorization") String authHeader,
      @RequestBody @Valid ChatRequestDTO request) {
      
      // TODO: Extraer farmId real de JWT en HU-BEDROCK-003
      String farmId = "FARM_DEFAULT"; // Placeholder
      
      ChatResponseDTO response = chatbotService.chat(farmId, request);
      
      return ResponseEntity.ok()
          .header("X-RateLimit-Limit", "100")
          .header("X-RateLimit-Remaining", "99") // TODO: Implementar real
          .body(response);
  }
  ```
- **Verificación**: Endpoint funcional con placeholder
- **Dependencias**: T7.1 (ChatbotService refactorizado)
- **Estimación**: 2 horas

**T8.2: Modificar ChatRequestDTO y ChatResponseDTO (2h)**
- **Archivos a modificar**:
  - `src/main/java/com/cattle/dtos/ChatRequestDTO.java` - Agregar validaciones @Valid
  - `src/main/java/com/cattle/dtos/ChatResponseDTO.java` - Agregar campos intent, durationMs
- **Cambios**:
  ```java
  // ChatRequestDTO
  @NotBlank(message = "User message is required")
  @Size(max = 1000, message = "Message too long")
  private String userMessage;
  
  // ChatResponseDTO
  private String intent;        // Intención detectada
  private Long durationMs;      // Duración del procesamiento
  ```
- **Verificación**: DTOs compilables con validaciones
- **Dependencias**: Ninguna (independiente)
- **Estimación**: 2 horas

**T8.3: Crear tests de ChatbotController (2h)**
- **Archivo a crear**: `src/test/java/com/cattle/controller/ChatbotControllerTest.java`
- **Casos de test**:
  - POST /api/chat/message retorna 200 OK
  - Response incluye campos intent y durationMs
  - Headers de rate limit presentes
  - Validación de request body funciona
- **Verificación**: Tests pasan con MockMvc
- **Dependencias**: T8.1 (ChatbotController modificado)
- **Estimación**: 2 horas

---

### Estimaciones por Hito

| Hito | Tareas | Horas | Días | Dependencias |
|------|--------|-------|------|--------------|
| 1. Entidades y Enums | T1.1 - T1.4 | 6h | 0.8 | Ninguna |
| 2. Repositorios | T2.1 - T2.3 | 8h | 1.0 | Hito 1 |
| 3. Query Services | T3.1 - T3.3 | 18h | 2.5 | Hito 2 |
| 4. Intent Detection | T4.1 - T4.2 | 10h | 1.3 | Hito 1 |
| 5. Context Builder | T5.1 - T5.2 | 12h | 1.5 | Hitos 3, 4 |
| 6. Bedrock Service | T6.1 - T6.2 | 6h | 0.8 | Ninguna |
| 7. Chatbot Service | T7.1 - T7.2 | 10h | 1.3 | Hitos 4, 5, 6 |
| 8. Controller y DTOs | T8.1 - T8.3 | 6h | 0.8 | Hito 7 |
| **TOTAL** | **25 tareas** | **76h** | **10.0 días** | - |

### Archivos a Crear/Modificar

**Archivos NUEVOS (14)**:

1. `src/main/java/com/cattle/enums/QueryIntent.java`
2. `src/main/java/com/cattle/dtos/IntentContext.java`
3. `src/main/java/com/cattle/dtos/BovineContextDTO.java`
4. `src/main/java/com/cattle/dtos/MilkingContextDTO.java`
5. `src/main/java/com/cattle/dtos/PastureContextDTO.java`
6. `src/main/java/com/cattle/dtos/ChatRequestDTO.java`
7. `src/main/java/com/cattle/dtos/ChatResponseDTO.java`
8. `src/main/java/com/cattle/repository/MilkingRepository.java`
9. `src/main/java/com/cattle/repository/PastureRepository.java`
10. `src/main/java/com/cattle/services/BovineQueryService.java`
11. `src/main/java/com/cattle/services/MilkingQueryService.java`
12. `src/main/java/com/cattle/services/PastureQueryService.java`
13. `src/main/java/com/cattle/services/IntentDetectionService.java`
14. `src/main/java/com/cattle/services/ContextBuilderService.java`
15. `src/main/java/com/cattle/services/BedrockService.java`
16. `src/main/java/com/cattle/services/ChatbotService.java`
17. `src/main/java/com/cattle/controller/ChatbotController.java`

**Archivos EXISTENTES a EXTENDER (1)**:

1. `src/main/java/com/cattle/repository/BovineRepository.java` (agregar métodos de consulta)

### Consideraciones de Implementación

**Orden de desarrollo recomendado**:
1. Hito 1 (Día 1) - Base del sistema
2. Hito 2 (Días 2-3) - Acceso a datos
3. Hito 3 (Días 4-5) - Lógica de negocio
4. Hito 4 (Día 6) - Intent detection (paralelizable con Hito 6)
5. Hito 6 (Día 8 AM) - Bedrock service (paralelizable con Hito 4)
6. Hito 5 (Día 7) - Context builder (requiere 3 y 4)
7. Hito 7 (Días 8-9) - Orquestación (requiere 4, 5, 6)
8. Hito 8 (Día 10 AM) - Controller (requiere 7)

**Paralelización posible**:
- Hito 4 (Intent Detection) + Hito 6 (Bedrock Service) son independientes entre sí
- Tests unitarios de cada hito pueden hacerse en paralelo con siguiente hito

**Riesgos técnicos identificados**:
1. **MEDIO**: Copiar entities puede requerir ajustes de anotaciones - Mitigación: Validar compilación inmediatamente
2. **BAJO**: Queries DynamoDB con GSI pueden ser complejas - Mitigación: Referencias a cattle-lambda-function
3. **MEDIO**: Precisión de intent detection puede ser < 95% inicialmente - Mitigación: Iteraciones rápidas con test cases
4. **BAJO**: Timeout de Bedrock puede exceder 5s - Mitigación: Timeout configurado explícitamente

---

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-16 | **Enfoque:** Exploratorio

- [ ] Código compilable y sin errores
- [ ] 8 clases principales implementadas
- [ ] 47 tests implementados (cobertura ≥ 85%)
- [ ] Todos los criterios de aceptación pasan
- [ ] Documentación técnica completa
- [ ] Análisis de seguridad validado
- [ ] Deploy a AWS Dev exitoso
- [ ] Logs estructurados en CloudWatch
- [ ] Performance < 200ms (queries), < 5s (Bedrock)
- [ ] Code review aprobado
- [ ] Ready for UAT

---

## 📚 Documentación de Apoyo

- **ESPECIFICACION.md**: Detalles técnicos completos
- **IMPLEMENTACION.md**: Código compilable (8 clases)
- **EJECUCION.md**: Comandos Maven, AWS CLI
- **REFERENCIA-RAPIDA.md**: Cheat sheet

---

## 🎯 Métricas de Éxito

- ✅ Intención detectada con 95%+ precisión
- ✅ Queries ejecutadas en < 200ms
- ✅ Respuestas de Bedrock en < 5 segundos
- ✅ 0 vulnerabilidades críticas
- ✅ Cobertura de tests ≥ 85%
- ✅ Uptime 99.9%
- ✅ All CAse pass

---

**Documento**: HU-BEDROCK-001  
**Versión**: 1.3  
**Fecha**: 19 de Enero de 2026  
**Status**: ✅ Refinado (Developer) - Ready for implementación

---

## 📝 Registro de Cambios

| Fecha | Versión | Cambio | Autor |
|-------|---------|--------|-------|
| 2026-01-16 | 1.0 | Creación inicial de historia por PO | Product Owner |
| 2026-01-16 | 1.1 | Análisis arquitectónico completado | jhon.fernandez (Arquitecto) |
| 2026-01-16 | 1.2 | Refinamiento técnico completado | jhon.fernandez (Developer) |
| 2026-01-19 | 1.3 | Corrección de referencias: implementación en cattle-lambda-function en lugar de cattle-bedrock. Actualización de estimaciones: 12 puntos (76h, 10 días). Entities ya existen, no se copian. | jhon.fernandez (Developer) |
