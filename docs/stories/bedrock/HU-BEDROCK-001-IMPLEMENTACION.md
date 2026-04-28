# HU-BEDROCK-001: Chatbot Inteligente - Implementación

**ID**: HU-BEDROCK-001  
**Prioridad**: Crítica  
**Sprint original**: S-1 (Enero 2026)  
**Estado actual**: Implementación existente revisada documentalmente - Abril 2026

## Objetivo de negocio

Como usuario de la plataforma Cattle, quiero hacer consultas en lenguaje natural sobre mi finca y recibir respuestas contextualizadas usando datos operativos y capacidades de IA, para tomar decisiones sin recorrer múltiples pantallas manualmente.

## Alcance vigente

La implementación actual cubre el módulo chatbot integrado dentro de `lambda-aws-cattle-java`, no un proyecto activo separado.

Capacidades visibles en el código:

- chat operativo sobre datos de finca
- consulta a Knowledge Base técnica
- sanitización de input
- rate limiting por finca
- auditoría
- seguridad JWT configurable

## Evidencia revisada

- `src/main/java/com/cattle/controller/ChatbotController.java`
- `src/main/java/com/cattle/services/chatbot/ChatbotService.java`
- `src/main/java/com/cattle/services/chatbot/BedrockService.java`
- `src/main/java/com/cattle/services/IntentDetectionService.java`
- `src/main/java/com/cattle/services/ContextBuilderService.java`
- `src/main/java/com/cattle/services/BovineQueryService.java`
- `src/main/java/com/cattle/services/MilkingQueryService.java`
- `src/main/java/com/cattle/services/PastureQueryService.java`
- `src/main/java/com/cattle/services/knowledge/KnowledgeBaseService.java`
- `src/main/java/com/cattle/config/SecurityConfig.java`
- `template.yml`

## Criterios de aceptación vigentes

### CA-001: Detección de intención

El flujo de chat debe identificar intención suficiente para enrutar la consulta a construcción de contexto adecuada.

Se considera cubierto mientras existan y operen:

- `IntentDetectionService`
- `IntentContext`
- integración desde `ChatbotService`

### CA-002: Construcción de contexto con datos reales

El chatbot debe poder enriquecer prompts con datos operativos reales de bovinos, milking o potreros.

Se considera cubierto mientras `ContextBuilderService` delegue en:

- `BovineQueryService`
- `MilkingQueryService`
- `PastureQueryService`

### CA-003: Integración con Bedrock

El backend debe invocar Bedrock para generar respuestas conversacionales sobre el contexto construido.

Se considera cubierto mientras existan:

- `BedrockService`
- variable `BEDROCK_MODEL_ID`
- flujo `ChatbotService -> BedrockService`

### CA-004: Exposición HTTP del módulo

El backend debe exponer estos endpoints del módulo:

- `POST /api/chat/message`
- `POST /api/chat/knowledge`
- `GET /api/chat/health`

### CA-005: Controles transversales

El módulo debe incorporar:

- autenticación configurable por JWT
- rate limiting por finca
- sanitización de input
- auditoría y logging de errores

## Estado observado en el código

### Implementado

- `ChatbotController`
- `ChatbotService`
- `BedrockService`
- `IntentDetectionService`
- `ContextBuilderService`
- servicios de consulta de dominio
- `KnowledgeBaseService`

### Integración de despliegue visible

- una sola Lambda SAM: `cattle-lambda-function`
- permisos IAM para Bedrock y Knowledge Base
- memoria configurada en `1024`
- timeout en `30` segundos

## Gaps abiertos

- `SecurityConfig` no refleja con la misma precisión el endpoint `/api/chat/knowledge` que el endpoint `/api/chat/message`.
- `BEDROCK_KB_ID` se usa en código y debe quedar alineado también en la configuración de despliegue real del ambiente.
- la documentación histórica de Bedrock todavía podía mezclar nombres y topologías legacy; esta HU ya no debe hacerlo.

## Decisiones técnicas consolidadas

- arquitectura integrada en el backend principal
- uso de servicios de consulta por dominio para construir contexto
- separación entre chat operativo y consultas RAG
- despliegue unificado en una sola función Lambda

## Siguiente paso recomendado

1. Mantener esta HU como referencia funcional de implementación, no como plan futuro de creación desde cero.
2. Bajar los gaps abiertos a refinamiento técnico cuando se vaya a corregir seguridad o despliegue.
3. Mantener sincronizadas esta HU y la arquitectura en `docs/arquitectura/chatbot/`.