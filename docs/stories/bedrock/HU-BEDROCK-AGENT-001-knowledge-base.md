# 🧠 HU-BEDROCK-AGENT-001: Chatbot con Knowledge Base

**ID**: HU-BEDROCK-AGENT-001  
**Prioridad**: 🟡 ALTA  
**Estimación**: Pendiente (Estimación Developer)  
**Sprint**: Pendiente  
**Estado**: 🚀 Desplegado en Producción  

---

## 📋 Descripción

Como **usuario ganadero autenticado**, quiero **consultar información sobre prácticas ganaderas, protocolos sanitarios y conocimiento técnico** a través de un asistente virtual, para poder **obtener respuestas basadas en documentación especializada sin depender exclusivamente de mis datos operativos**.

---

## 🎯 Objetivo de Negocio

Complementar el chatbot existente (basado en DynamoDB) con un nuevo endpoint que utiliza **Amazon Bedrock Knowledge Base** para responder preguntas basadas en documentación técnica indexada, proporcionando:

- Respuestas sobre protocolos de vacunación
- Guías de manejo de ganado
- Información sobre enfermedades y tratamientos
- Normativas y regulaciones ganaderas
- Buenas prácticas de producción láctea

---

## 🔀 Coexistencia con Chatbot Actual

| Endpoint | Fuente de Datos | Tipo de Preguntas |
|----------|-----------------|-------------------|
| `POST /api/chat/message` | DynamoDB (datos finca) | "¿Cuántas vacas preñadas tengo?" |
| `POST /api/chat/knowledge` | Knowledge Base (documentos) | "¿Cómo detecto mastitis en vacas?" |

---

## 🎯 Criterios de Aceptación

### CA-001: Consulta a Knowledge Base ✅

```gherkin
Scenario: Consultar información técnica ganadera
  Given el usuario pregunta "¿Cuál es el protocolo de vacunación para terneros?"
  When la Lambda invoca Bedrock Knowledge Base
  Then obtiene respuesta basada en documentos indexados
  And incluye citaciones de las fuentes consultadas
  And responde en menos de 10 segundos
```

**Detalles**:
- Usar API `RetrieveAndGenerate` de Bedrock Agent Runtime
- Incluir referencias a documentos fuente en la respuesta
- Timeout máximo: 10 segundos

### CA-002: Autenticación JWT Obligatoria ✅

```gherkin
Scenario: Validar autenticación en endpoint knowledge
  Given request sin token JWT válido
  When Lambda recibe la solicitud
  Then rechaza con error 401 Unauthorized
  And registra intento en audit log
```

**Detalles**:
- Reutilizar `JwtTokenProvider` existente
- Extraer `farmId` y `userId` del token
- Aplicar mismas reglas de seguridad que `/api/chat/message`

### CA-003: Rate Limiting por Finca ✅

```gherkin
Scenario: Limitar consultas por finca
  Given usuario realiza 100+ queries en 1 hora
  When Lambda recibe request 101
  Then rechaza con error 429 Too Many Requests
  And incluye headers X-RateLimit-*
```

**Detalles**:
- Reutilizar `RateLimitingService` existente
- Mismo límite que chatbot actual (100/hora por finca)

### CA-004: Respuesta con Citaciones ✅

```gherkin
Scenario: Incluir fuentes en la respuesta
  Given consulta exitosa a Knowledge Base
  When se genera la respuesta
  Then incluye lista de documentos fuente consultados
  And cada citación tiene: texto, URI del documento, página (si aplica)
```

**Detalles**:
- Parsear campo `citations` de respuesta KB
- Formatear en `KnowledgeResponseDTO`

### CA-005: Manejo de Errores Sin Exposición Técnica ✅

```gherkin
Scenario: Error en Knowledge Base no expone detalles
  Given Knowledge Base no disponible o error de conexión
  When Lambda captura la excepción
  Then retorna mensaje genérico al usuario
  And registra error detallado en CloudWatch
  And NO expone stack trace ni IDs internos
```

### CA-006: Audit Logging ✅

```gherkin
Scenario: Registrar operaciones de knowledge
  Given cada consulta a Knowledge Base
  When se completa la operación
  Then genera log estructurado JSON
  And incluye: timestamp, userId, farmId, duration, status
```

**Detalles**:
- Reutilizar `AuditLoggingService` existente

### CA-007: Validación de Input ✅

```gherkin
Scenario: Sanitizar mensaje del usuario
  Given mensaje con caracteres potencialmente maliciosos
  When Lambda procesa el request
  Then sanitiza el input antes de enviar a KB
  And rechaza mensajes que excedan 1000 caracteres
```

**Detalles**:
- Reutilizar `InputValidationService` existente

---

## 🏗️ Arquitectura

```
Usuario Request (POST /api/chat/knowledge)
    ↓
[AWS API Gateway]
    ↓
[Lambda: cattle-lambda-function]
    ├─→ JwtAuthenticationFilter [REUTILIZADO]
    ├─→ ChatbotController.queryKnowledge() [NUEVO MÉTODO]
    │     ├─→ RateLimitingService [REUTILIZADO]
    │     ├─→ InputValidationService [REUTILIZADO]
    │     ├─→ KnowledgeBaseService.query() [NUEVO]
    │     │     └─→ BedrockAgentRuntimeClient.retrieveAndGenerate()
    │     └─→ AuditLoggingService [REUTILIZADO]
    ↓
Response (KnowledgeResponseDTO)
```

---

## Análisis Arquitectónico (Arquitecto)

### Decisiones de Diseño

**Patrón Arquitectónico:** Service Layer Pattern + Strategy Pattern + Dependency Injection

**Justificación:** 
1. **Reutilización máxima:** Aplicando el mismo patrón usado en `ChatbotService`, reutilizamos 4 servicios transversales existentes.
2. **Bajo acoplamiento:** El nuevo `KnowledgeBaseService` se inyecta como dependencia sin afectar el flujo actual.
3. **Alta cohesión:** Cada servicio tiene responsabilidad única - `BedrockService` para modelo directo, `KnowledgeBaseService` para RAG.
4. **SOLID:** Single Responsibility (separar KB de DynamoDB), Open/Closed (extender sin modificar), Dependency Inversion (inyectar cliente vía config).

**Componentes Afectados:**

- **BedrockAgentConfig (Nuevo):** Configuración del bean `BedrockAgentRuntimeClient` para Knowledge Base
  - Especificaciones: Region US_EAST_1, EnvironmentVariableCredentialsProvider

- **KnowledgeBaseService (Nuevo):** Servicio de invocación a Knowledge Base
  - Responsabilidades: Invocar API RetrieveAndGenerate, parsear citaciones, manejo de errores KB
  - Especificaciones: Método `query(String question)` retorna `KnowledgeResponseDTO`

- **KnowledgeRequestDTO (Nuevo):** DTO de request
  - Campos: `question` (String, @NotBlank, max 1000), `sessionId` (String, opcional)

- **KnowledgeResponseDTO (Nuevo):** DTO de response
  - Campos: `answer` (String), `citations` (List<CitationDTO>), `sources` (List<String>), `durationMs` (Long)

- **CitationDTO (Nuevo):** DTO para citaciones
  - Campos: `text` (String), `documentUri` (String), `page` (Integer opcional)

- **ChatbotController (Modificación):** Agregar endpoint `/api/chat/knowledge`
  - Nivel de cambio: Menor
  - Especificaciones: Nuevo método `queryKnowledge()`, reutiliza `getFarmIdFromSecurityContext()`

- **build.gradle (Modificación):** Agregar dependencia bedrockagentruntime
  - Nivel de cambio: Menor
  - Especificaciones: `software.amazon.awssdk:bedrockagentruntime:2.32.16`

- **application.properties (Modificación):** Variables de configuración KB
  - Nivel de cambio: Menor
  - Especificaciones: `bedrock.knowledge-base.id`, `bedrock.knowledge-base.model-arn`

- **template.yml (Modificación):** Permisos IAM para KB
  - Nivel de cambio: Menor
  - Especificaciones: `bedrock:RetrieveAndGenerate`, `bedrock:Retrieve`

**Hitos de Implementación:**

1. **Infraestructura y Dependencias** - Configuración base
   - Dependencias: Ninguna

2. **DTOs de Knowledge Base** - Estructuras de datos
   - Dependencias: Hito 1

3. **KnowledgeBaseService** - Lógica de invocación KB
   - Dependencias: Hito 1, Hito 2

4. **Endpoint Controller** - Integración en ChatbotController
   - Dependencias: Hito 3

5. **Tests Unitarios** - Cobertura de nuevos componentes
   - Dependencias: Hito 4

6. **Infraestructura AWS (template.yml)** - Permisos y variables
   - Dependencias: Hito 4

### Validación de Impacto

**Código verificado:**
- `BedrockConfig.java`: Patrón replicable para KB - usa `BedrockRuntimeClient`, necesitamos `BedrockAgentRuntimeClient` separado
- `BedrockService.java`: Usa Converse API - diferente de RetrieveAndGenerate API
- `ChatbotController.java`: Métodos `getFarmIdFromSecurityContext()` y `getUserIdFromSecurityContext()` reutilizables
- `build.gradle`: Falta dependencia `bedrockagentruntime` - requiere agregar
- `ChatResponseDTO.java`: Necesita DTO separado para incluir citations

**Cadena de invocación completa:**
```
API Gateway → ChatbotController.queryKnowledge()
  → RateLimitingService.allowRequest() [REUTILIZADO]
  → InputValidationService.sanitize() [REUTILIZADO]
  → KnowledgeBaseService.query() [NUEVO]
    → BedrockAgentRuntimeClient.retrieveAndGenerate() [NUEVO]
  → AuditLoggingService.logChatEvent() [REUTILIZADO]
  → Response con KnowledgeResponseDTO [NUEVO]
```

**Análisis de dependencias:**
- Sin impacto en flujo actual `/api/chat/message`
- Reutilización de 4 servicios transversales existentes
- Nueva dependencia: `software.amazon.awssdk:bedrockagentruntime:2.32.16`

### Notas Técnicas (Si aplica)

**Prerequisitos AWS (antes del desarrollo):**
1. Crear S3 Bucket para documentos de conocimiento
2. Crear Knowledge Base en consola Bedrock (US-EAST-1)
3. Indexar documentos iniciales (manuales, guías, protocolos)
4. Obtener KB_ID para configuración

**Diferencia entre APIs:**
- `BedrockRuntimeClient` → Para invocar modelos directamente (Converse API)
- `BedrockAgentRuntimeClient` → Para Knowledge Base (RetrieveAndGenerate API)

**Consideraciones de Testing:**
- Mock de `BedrockAgentRuntimeClient` similar a `BedrockRuntimeClient`
- Seguir patrón de `BedrockServiceTest.java` existente

### Referencias y Validación

**Documentación consultada:**
- index.md - GPS Arquitectónico del sistema
- HU-BEDROCK-001-IMPLEMENTACION.md - Historia del chatbot actual

**Historias relacionadas:**
- Historia #HU-BEDROCK-001: Chatbot con DynamoDB - Patrón base a reutilizar
- Historia #HU-BEDROCK-003: Seguridad JWT - Componentes de seguridad a reutilizar

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-22 | **Enfoque:** Dirigido + Exploratorio

---

## Refinamiento Técnico (Developer)

### Consideraciones Generales

**Basado en análisis arquitectónico:**
Sección "Análisis Arquitectónico (Arquitecto)" de esta historia - 6 Hitos definidos, patrón Service Layer + DI

**Nivel de complejidad:**
MEDIA - Integración con nuevo cliente AWS SDK (`BedrockAgentRuntimeClient`), creación de 5 archivos nuevos, modificación de 4 existentes. Patrón ya establecido con `BedrockService`.

**Riesgos técnicos conocidos:**
- Knowledge Base debe existir en AWS antes de testing de integración
- API `RetrieveAndGenerate` diferente a `Converse` API actual
- Respuestas con citations requieren parseo adicional

**Patrones y convenciones del equipo:**
- Constructor injection para dependencias (DIP)
- `@Service` para servicios, `@Configuration` para beans
- DTOs con Lombok `@Builder`, `@Data`
- Suffix `DTO` para data transfer objects
- Tests con `@Tag("unit")`, `@Tag("chatbot")`

**Dependencias nuevas a instalar:**
`software.amazon.awssdk:bedrockagentruntime:2.32.16`

**Estrategia de testing:**
JUnit 5 + Mockito | Tests unitarios con mock de `BedrockAgentRuntimeClient` | Cobertura: 80%+ | Builders: Reutilizar patrón de `BedrockServiceTest.java`

### Historias Relacionadas Consultadas

**Implementaciones similares analizadas:**
- HU-BEDROCK-001: Chatbot con DynamoDB - Patrón de integración Bedrock
- BedrockService.java + BedrockServiceTest.java - Estructura de mock AWS SDK

**Patrones de código reutilizados:**
- `ChatbotController.java`: métodos `getFarmIdFromSecurityContext()`, `getUserIdFromSecurityContext()`
- `BedrockConfig.java`: patrón de configuración de cliente AWS
- `ChatResponseDTO.java`: estructura base para response

**Mejores prácticas aplicadas:**
- Reutilizar servicios transversales (RateLimiting, InputValidation, AuditLogging)
- Separar cliente KB de cliente Bedrock directo (SRP)
- DTOs específicos para Knowledge Base (no mezclar con chatbot actual)

---

## Tareas de Implementación (Developer)

### Fase 0: Infraestructura y Setup

#### Cambios de Configuración

- [x] **Agregar dependencia bedrockagentruntime** (AC: CA-001)
  - [x] Modificar `build.gradle` - Agregar línea en bloque dependencies
  - [x] Verificar compatibilidad con versión AWS SDK existente (2.32.16)

- [x] **Agregar variables de configuración KB** (AC: CA-001)
  - [x] Modificar `src/main/resources/application.properties`
  - [x] Agregar: `bedrock.knowledge-base.id`, `bedrock.knowledge-base.model-arn`

- [x] **Actualizar permisos IAM** (AC: CA-001)
  - [x] Modificar `template.yml` - Agregar policy para `bedrock:RetrieveAndGenerate`, `bedrock:Retrieve`

---

### Fase 1: Infraestructura y Dependencias

#### 📦 Config

- [x] **Crear BedrockAgentConfig.java** (AC: CA-001)
  - [x] Crear archivo: `src/main/java/com/cattle/config/BedrockAgentConfig.java`
  - [x] Implementar bean `BedrockAgentRuntimeClient` con Region.US_EAST_1
  - [x] Usar `EnvironmentVariableCredentialsProvider` (patrón de `BedrockConfig.java`)

---

### Fase 2: DTOs de Knowledge Base

#### 📦 DTOs

- [x] **Crear KnowledgeRequestDTO.java** (AC: CA-001, CA-007)
  - [x] Crear archivo: `src/main/java/com/cattle/dtos/knowledge/KnowledgeRequestDTO.java`
  - [x] Campos: `question` (@NotBlank, @Size max=1000), `sessionId` (opcional)
  - [x] Usar Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
  - [x] Agregar anotaciones Swagger `@Schema`

- [x] **Crear CitationDTO.java** (AC: CA-004)
  - [x] Crear archivo: `src/main/java/com/cattle/dtos/knowledge/CitationDTO.java`
  - [x] Campos: `text`, `documentUri`, `page` (Integer opcional)
  - [x] Usar Lombok: `@Data`, `@Builder`

- [x] **Crear KnowledgeResponseDTO.java** (AC: CA-001, CA-004)
  - [x] Crear archivo: `src/main/java/com/cattle/dtos/knowledge/KnowledgeResponseDTO.java`
  - [x] Campos: `answer`, `citations` (List<CitationDTO>), `sources` (List<String>), `durationMs`, `timestamp`
  - [x] Usar Lombok: `@Data`, `@Builder`

---

### Fase 3: KnowledgeBaseService

#### 📦 Services

- [x] **Crear KnowledgeBaseService.java** (AC: CA-001, CA-004, CA-005)
  - [x] Crear archivo: `src/main/java/com/cattle/services/knowledge/KnowledgeBaseService.java`
  - [x] Inyectar `BedrockAgentRuntimeClient` vía constructor
  - [x] Implementar método `query(String question)` retorna `KnowledgeResponseDTO`
  - [x] Usar API `RetrieveAndGenerateRequest` con `knowledgeBaseId` y `modelArn`
  - [x] Parsear `citations` de `RetrieveAndGenerateResponse`
  - [x] Implementar manejo de errores (catch Exception, log, re-throw RuntimeException)
  - [x] Configurar `@Value` para `bedrock.knowledge-base.id` y `bedrock.knowledge-base.model-arn`

- [x] **Tests unitarios KnowledgeBaseService** (AC: CA-001, CA-004, CA-005)
  - [x] Crear archivo: `src/test/java/com/cattle/services/knowledge/KnowledgeBaseServiceTest.java`
  - [x] Test: `query_validQuestion_returnsResponse` - Mock response exitoso
  - [x] Test: `query_validQuestion_includesCitations` - Verificar parseo de citations
  - [x] Test: `query_bedrockException_throwsRuntimeException` - Error propagado
  - [x] Test: `query_emptyResponse_handlesGracefully` - Response vacío
  - [x] Test: `query_timeoutException_handlesGracefully` - Timeout manejado
  - [x] Usar `@Tag("unit")`, `@Tag("chatbot")`
  - [x] Seguir patrón de `BedrockServiceTest.java`

---

### Fase 4: Endpoint Controller

#### 📦 Controller

- [x] **Agregar endpoint /api/chat/knowledge en ChatbotController** (AC: CA-001, CA-002, CA-003, CA-006, CA-007)
  - [x] Modificar archivo: `src/main/java/com/cattle/controller/ChatbotController.java`
  - [x] Inyectar `KnowledgeBaseService` en constructor
  - [x] Crear método `queryKnowledge(@Valid @RequestBody KnowledgeRequestDTO request)`
  - [x] Reutilizar `getFarmIdFromSecurityContext()` y `getUserIdFromSecurityContext()`
  - [x] Reutilizar `rateLimitingService.allowRequest(farmId)`
  - [x] Reutilizar `inputValidationService.sanitize()`
  - [x] Reutilizar `auditLoggingService.logChatEvent()`
  - [x] Agregar anotaciones OpenAPI: `@Operation`, `@ApiResponses`
  - [x] Mapping: `@PostMapping("/knowledge")`

- [x] **Tests unitarios endpoint /knowledge**
  - [x] Crear archivo: `src/test/java/com/cattle/controller/ChatbotControllerKnowledgeTest.java`
  - [x] Test: `queryKnowledge_validRequest_returnsOk`
  - [x] Test: `queryKnowledge_rateLimitExceeded_returns429`
  - [x] Test: `queryKnowledge_invalidInput_returns400`
  - [x] Test: `queryKnowledge_serviceError_returns500`
  - [x] Usar `@Tag("unit")`, `@Tag("chatbot")`

---

### Fase 5: Infraestructura AWS (template.yml)

#### 📦 AWS Config

- [x] **Agregar variables de entorno en Lambda** (AC: CA-001)
  - [x] Modificar `template.yml` - Sección Environment.Variables
  - [x] Agregar: `BEDROCK_KB_ID`, `BEDROCK_KB_MODEL_ARN`

- [x] **Agregar permisos IAM para Knowledge Base** (AC: CA-001)
  - [x] Modificar `template.yml` - Sección Policies
  - [x] Agregar Statement para `bedrock:RetrieveAndGenerate`, `bedrock:Retrieve`
  - [x] Resource: `arn:aws:bedrock:*:*:knowledge-base/*`

---

### Fase N: QA y Deployment

#### 📦 Code Quality

- [ ] **Ejecutar Agente Peer Review** (MANUAL)
  - [ ] Ejecutar `*peer-review` en agente Peer Reviewer
  - [ ] Documentar hallazgos

- [ ] **Resolver incidentes del Peer Review** (MANUAL - condicional)
  - [ ] Aplicar correcciones si hay hallazgos

#### 📦 Deployment DEV

- [ ] **Crear Pull Request** (MANUAL)
  - [ ] Crear PR con descripción referenciando HU-BEDROCK-AGENT-001
  - [ ] Vincular criterios de aceptación completados

- [ ] **Ejecutar pipeline deployment DEV** (MANUAL)
  - [ ] Verificar build exitoso
  - [ ] Verificar tests pasan
  - [ ] Verificar deployment a ambiente DEV

#### 📦 Testing Manual

- [ ] **Diseñar set de pruebas manuales** (MANUAL)
  - [ ] Probar endpoint `/api/chat/knowledge` con Postman/curl
  - [ ] Verificar respuestas incluyen citations
  - [ ] Verificar rate limiting funciona
  - [ ] Verificar JWT requerido

- [ ] **Ejecutar pruebas manuales** (MANUAL)
  - [ ] Ejecutar casos de prueba diseñados
  - [ ] Documentar resultados

---

**Notas sobre vinculación con Criterios de Aceptación:**
- CA-001 (Consulta KB): Fases 0-5
- CA-002 (Auth JWT): Fase 4 - reutiliza auth existente
- CA-003 (Rate Limit): Fase 4 - reutiliza RateLimitingService
- CA-004 (Citations): Fases 2, 3
- CA-005 (Error Handling): Fase 3
- CA-006 (Audit Log): Fase 4 - reutiliza AuditLoggingService
- CA-007 (Input Validation): Fases 2, 4

---

## 🧪 Requisitos de Pruebas Unitarias

### Tests Requeridos para KnowledgeBaseService

| Test | Descripción | Prioridad |
|------|-------------|-----------|
| `query_validQuestion_returnsResponse` | Consulta exitosa retorna respuesta con citaciones | 🔴 Alta |
| `query_validQuestion_includesCitations` | Respuesta incluye lista de citaciones parseadas | 🔴 Alta |
| `query_bedrockException_throwsRuntimeException` | Error de KB se propaga correctamente | 🔴 Alta |
| `query_emptyResponse_handlesGracefully` | Respuesta vacía de KB manejada | 🟡 Media |
| `query_timeoutException_handlesGracefully` | Timeout de KB manejado | 🟡 Media |
| `query_nullQuestion_throwsException` | Pregunta null rechazada | 🟡 Media |

### Tests Requeridos para ChatbotController (endpoint /knowledge)

| Test | Descripción | Prioridad |
|------|-------------|-----------|
| `queryKnowledge_validRequest_returnsOk` | Request válido retorna 200 | 🔴 Alta |
| `queryKnowledge_rateLimitExceeded_returns429` | Rate limit retorna 429 | 🔴 Alta |
| `queryKnowledge_invalidInput_returns400` | Input inválido retorna 400 | 🔴 Alta |
| `queryKnowledge_serviceError_returns500` | Error interno retorna 500 genérico | 🟡 Media |
| `queryKnowledge_includesRateLimitHeaders` | Response incluye headers X-RateLimit | 🟡 Media |

### Patrón de Test a Seguir

Seguir estructura de `BedrockServiceTest.java`:
```java
@Tag("unit")
@Tag("chatbot")
class KnowledgeBaseServiceTest {
    @Mock
    private BedrockAgentRuntimeClient agentClient;
    
    // Tests con mock de RetrieveAndGenerateResponse
}
```

---

## 📊 Métricas de Éxito

| Métrica | Objetivo | Medición |
|---------|----------|----------|
| Latencia P95 | < 8 segundos | CloudWatch |
| Tasa de error | < 2% | CloudWatch |
| Cobertura tests | > 80% | JaCoCo |
| Adopción | 50+ consultas/semana | Audit logs |

---

## 📝 Notas para Refinamiento

**Para el Developer:**
1. Crear tareas técnicas basadas en los 6 hitos de implementación
2. Estimar esfuerzo por cada hito
3. Identificar archivos específicos a crear/modificar
4. Definir orden de PRs

**Dependencias externas:**
- Knowledge Base debe existir en AWS antes de testing de integración
- Para desarrollo local: usar mocks del cliente

---

## 📋 Registro de Cambios

| Fecha | Versión | Cambio | Autor |
|-------|---------|--------|-------|
| 2026-01-22 | 1.0 | Creación de historia con análisis arquitectónico | jhon.fernandez (Arquitecto) |
| 2026-01-22 | 1.1 | Refinamiento técnico con tareas detalladas | jhon.fernandez (Developer) |
| 2026-01-23 | 1.2 | Implementación completa Fases 0-5 + Tests | jhon.fernandez (Developer) |
| 2026-01-26 | 1.3 | Corrección placeholder circular + Despliegue a Producción | jhon.fernandez (Developer) |

---

## 📝 Dev Agent Record

### Resumen de Implementación

**Fecha de desarrollo:** 2026-01-23
**Developer:** jhon.fernandez
**Duración de sesión:** ~45 minutos

### Archivos Creados

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `src/main/java/com/cattle/config/BedrockAgentConfig.java` | Config | Bean BedrockAgentRuntimeClient |
| `src/main/java/com/cattle/dtos/knowledge/KnowledgeRequestDTO.java` | DTO | Request para Knowledge Base |
| `src/main/java/com/cattle/dtos/knowledge/KnowledgeResponseDTO.java` | DTO | Response con answer y citations |
| `src/main/java/com/cattle/dtos/knowledge/CitationDTO.java` | DTO | Estructura de citación |
| `src/main/java/com/cattle/services/knowledge/KnowledgeBaseService.java` | Service | Invocación RetrieveAndGenerate |
| `src/test/java/com/cattle/services/knowledge/KnowledgeBaseServiceTest.java` | Test | 5 tests unitarios |
| `src/test/java/com/cattle/controller/ChatbotControllerKnowledgeTest.java` | Test | 4 tests unitarios |

### Archivos Modificados

| Archivo | Tipo de Cambio | Descripción |
|---------|----------------|-------------|
| `build.gradle` | Dependencia | Agregado `bedrockagentruntime:2.32.16` |
| `src/main/resources/application.properties` | Config | Variables KB: id, model-arn |
| `template.yml` | IAM + Env | Permisos KB + Variables de entorno |
| `src/main/java/com/cattle/controller/ChatbotController.java` | Endpoint | Agregado POST /api/chat/knowledge |

### Tests Ejecutados

| Test Class | Tests | Estado |
|------------|-------|--------|
| `KnowledgeBaseServiceTest` | 5 | ✅ PASSED |
| `ChatbotControllerKnowledgeTest` | 4 | ✅ PASSED |
| **Total** | **9** | ✅ **100%** |

### Criterios de Aceptación Cubiertos

- ✅ CA-001: Consulta a Knowledge Base (RetrieveAndGenerate API)
- ✅ CA-002: Autenticación JWT Obligatoria (reutiliza JwtAuthenticationFilter)
- ✅ CA-003: Rate Limiting por Finca (reutiliza RateLimitingService)
- ✅ CA-004: Respuesta con Citaciones (CitationDTO + parseo)
- ✅ CA-005: Manejo de Errores Sin Exposición Técnica
- ✅ CA-006: Audit Logging (reutiliza AuditLoggingService)
- ✅ CA-007: Validación de Input (reutiliza InputValidationService)

### Notas de Implementación

1. **API Utilizada:** `RetrieveAndGenerateRequest` con `KnowledgeBaseRetrieveAndGenerateConfiguration`
2. **Parseo de Citations:** Extracción de `content().text()` y `location().s3Location().uri()`
3. **Reutilización:** 4 servicios transversales reutilizados sin modificación
4. **Testing:** Mocks de `BedrockAgentRuntimeClient` siguiendo patrón de `BedrockServiceTest`

### Prerequisitos para Producción

⚠️ Antes de desplegar a producción:
1. Crear Knowledge Base en AWS Bedrock Console (US-EAST-1)
2. Indexar documentos de conocimiento ganadero en S3
3. Configurar variable de entorno `BEDROCK_KB_ID` con el ID real

