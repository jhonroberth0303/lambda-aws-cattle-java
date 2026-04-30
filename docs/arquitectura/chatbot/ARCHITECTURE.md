# Arquitectura del Chatbot Bedrock Integrado

## Contexto

El módulo de chatbot ya no vive en un repositorio separado activo. La evidencia actual del proyecto muestra que sus capacidades están integradas dentro de `lambda-aws-cattle-java`, compartiendo la misma Lambda, el mismo contexto Spring Boot, el mismo modelo de seguridad y la misma infraestructura base del backend.

## Evidencia revisada

- `src/main/java/com/cattle/controller/ChatbotController.java`
- `src/main/java/com/cattle/services/chatbot/ChatbotService.java`
- `src/main/java/com/cattle/services/chatbot/BedrockService.java`
- `src/main/java/com/cattle/services/ContextBuilderService.java`
- `src/main/java/com/cattle/services/IntentDetectionService.java`
- `src/main/java/com/cattle/services/knowledge/KnowledgeBaseService.java`
- `src/main/java/com/cattle/config/BedrockAgentConfig.java`
- `build.gradle`
- `template.yml`

## Responsabilidad del módulo

El chatbot resuelve dos capacidades relacionadas pero distintas:

- consultas conversacionales sobre datos operativos de la finca mediante `POST /api/chat/message`
- consultas RAG sobre conocimiento técnico ganadero mediante `POST /api/chat/knowledge`

Ambas se exponen desde `ChatbotController`, pero usan rutas de procesamiento diferentes.

## Arquitectura lógica

### 1. Capa HTTP

`ChatbotController` expone:

- `POST /api/chat/message`
- `POST /api/chat/knowledge`
- `GET /api/chat/health`

Además aplica controles transversales visibles en código:

- extracción de `farmId` y `userId` desde el `SecurityContext`
- rate limiting por finca
- sanitización de input
- auditoría

### 2. Flujo conversacional sobre datos de finca

Ruta principal:

`ChatbotController` → `ChatbotService` → `IntentDetectionService` → `ContextBuilderService` → `BedrockService`

Qué hace cada componente:

- `IntentDetectionService`: identifica intención y parámetros útiles de la consulta.
- `ContextBuilderService`: consulta servicios especializados para construir contexto textual acotado.
- `BedrockService`: invoca el modelo configurado en Bedrock con el prompt enriquecido.
- `ChatbotService`: orquesta el flujo y devuelve `ChatResponseDTO`.

### 3. Flujo de Knowledge Base

Ruta principal:

`ChatbotController` → `KnowledgeBaseService` → `BedrockAgentRuntimeClient`

`KnowledgeBaseService` usa la API `RetrieveAndGenerate` y devuelve:

- respuesta generada
- citaciones
- fuentes únicas
- metadatos de duración y timestamp

### 4. Servicios de consulta para contexto

`ContextBuilderService` delega en:

- `BovineQueryService`
- `MilkingQueryService`
- `PastureQueryService`

Estos servicios consumen datos de la finca para convertir consultas abiertas en contexto estructurado para el modelo.

## Integraciones externas

### Amazon Bedrock Runtime

Se usa para generar respuestas del chatbot sobre contexto operativo de la finca.

Dependencias confirmadas:

- `software.amazon.awssdk:bedrockruntime`
- variable `BEDROCK_MODEL_ID`

### Amazon Bedrock Agent Runtime / Knowledge Base

Se usa para consultas RAG a la base de conocimiento técnica.

Dependencias y configuración confirmadas:

- `software.amazon.awssdk:bedrockagentruntime`
- `BedrockAgentRuntimeClient`
- variables `BEDROCK_KB_ID` y `BEDROCK_KB_MODEL_ARN`

### DynamoDB

El chatbot no accede a DynamoDB de forma aislada mediante otro servicio externo. Lo hace reutilizando servicios y repositorios del mismo backend para construir contexto de bovinos, leche y potreros.

## Seguridad y controles

Controles confirmados en código:

- autenticación requerida para `/api/chat/message`
- autenticación requerida en la práctica para `POST /api/chat/knowledge`, ya que el controlador lee `farmId` y `userId` desde el `SecurityContext`
- rate limiting por finca con headers de respuesta
- sanitización de input antes de invocar Bedrock o Knowledge Base
- audit logging de eventos, errores y rate limiting

Gap relevante: la protección declarativa de `SecurityConfig` menciona explícitamente `/api/chat/message`, pero no refleja con la misma precisión el endpoint `/api/chat/knowledge`.

## Despliegue

El módulo de chatbot se despliega junto con el backend completo, no como stack separado.

Evidencia actual:

- una sola función SAM: `cattle-lambda-function`
- `MemorySize: 1024`
- `Timeout: 30`
- permisos IAM para Bedrock y Bedrock Knowledge Base en `template.yml`

## Riesgos y gaps

- Parte de la documentación histórica todavía habla de `cattle-bedrock` como proyecto vivo; eso ya no es fuente vigente.
- `BEDROCK_KB_ID` se usa en código pero no aparece declarado en `template.yml` revisado.
- La seguridad del endpoint de Knowledge Base necesita quedar alineada de forma explícita en `SecurityConfig`.
- El diagrama PlantUML heredado del directorio puede contener nomenclatura legacy y debe revisarse antes de usarse como fuente de verdad.

## Lectura recomendada

1. Leer este documento para entender la arquitectura del módulo.
2. Leer `../flujos/flujo-transversal-chatbot-frontend-backend.md` para el journey completo con la SPA.
2. Revisar `../architecture-cattle-lambda-function.md` para el contexto backend completo.
3. Usar `GUIA-INTEGRACION-CHATBOT-DYNAMODB.md` para estado de integración y pendientes reales.