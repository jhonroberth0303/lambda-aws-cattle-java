# HU-BEDROCK-AGENT-001: Knowledge Base para el Chatbot

**ID**: HU-BEDROCK-AGENT-001  
**Prioridad**: Alta  
**Estimación**: No revalidada en esta revisión  
**Sprint original**: Pendiente en artefacto histórico  
**Estado actual**: Implementación existente revisada documentalmente - Abril 2026

## Objetivo de negocio

Como usuario autenticado de la plataforma, quiero consultar conocimiento técnico ganadero desde el asistente virtual para obtener respuestas basadas en documentación especializada, sin limitarme a mis datos operativos de finca.

## Alcance vigente

Esta historia cubre el endpoint de Knowledge Base integrado dentro del backend `lambda-aws-cattle-java`.

Capacidades visibles en el código:

- endpoint `POST /api/chat/knowledge`
- invocación `RetrieveAndGenerate` mediante Bedrock Agent Runtime
- respuesta con citaciones y fuentes
- reutilización de autenticación, rate limiting, sanitización y auditoría

## Coexistencia con el chat operativo

| Endpoint | Fuente principal | Tipo de consulta |
|---|---|---|
| `POST /api/chat/message` | Datos operativos de finca | "¿Cuántas vacas preñadas tengo?" |
| `POST /api/chat/knowledge` | Knowledge Base documental | "¿Cómo detectar mastitis en vacas?" |

## Evidencia revisada

- `src/main/java/com/cattle/controller/ChatbotController.java`
- `src/main/java/com/cattle/services/knowledge/KnowledgeBaseService.java`
- `src/main/java/com/cattle/config/BedrockAgentConfig.java`
- `src/main/java/com/cattle/dtos/knowledge/KnowledgeRequestDTO.java`
- `src/main/java/com/cattle/dtos/knowledge/KnowledgeResponseDTO.java`
- `src/main/java/com/cattle/dtos/knowledge/CitationDTO.java`
- `src/test/java/com/cattle/controller/ChatbotControllerKnowledgeTest.java`
- `src/test/java/com/cattle/services/knowledge/KnowledgeBaseServiceTest.java`
- `build.gradle`
- `template.yml`

## Criterios de aceptación vigentes

### CA-001: Endpoint funcional de Knowledge Base

El backend debe exponer `POST /api/chat/knowledge` y devolver una respuesta estructurada basada en documentos indexados.

### CA-002: Seguridad reutilizada

El endpoint debe reutilizar el modelo de seguridad del chatbot:

- contexto autenticado
- rate limiting por finca
- sanitización de input
- auditoría

### CA-003: Respuesta con citaciones

La respuesta debe poder incluir:

- `answer`
- `citations`
- `sources`
- metadatos de duración y timestamp

### CA-004: Configuración por ambiente

La integración debe depender de configuración externa, especialmente:

- `BEDROCK_KB_ID`
- `BEDROCK_KB_MODEL_ARN`

### CA-005: Sin impacto regresivo sobre el chat operativo

La existencia de `/api/chat/knowledge` no debe romper el flujo de `/api/chat/message`.

## Estado observado en el código

### Implementado

- `KnowledgeBaseService`
- DTOs específicos de knowledge
- endpoint en `ChatbotController`
- pruebas unitarias visibles para controlador y servicio
- cliente `BedrockAgentRuntimeClient` en configuración

### Integración AWS visible

- dependencia `bedrockagentruntime` en `build.gradle`
- permisos Bedrock Knowledge Base en `template.yml`
- uso de `RetrieveAndGenerate` en el servicio

## Gaps abiertos

- `BEDROCK_KB_ID` se utiliza en código, pero su declaración y suministro por ambiente debe mantenerse alineada con la infraestructura real.
- la política declarativa de seguridad debería reflejar con la misma precisión todos los endpoints Bedrock requeridos.
- el estado exacto de despliegue en producción no se revalidó en esta revisión documental; por eso no se afirma aquí como hecho operativo nuevo.

## Decisiones técnicas consolidadas

- separar consultas documentales de las consultas operativas de finca
- reutilizar servicios transversales existentes en vez de duplicarlos
- usar DTOs específicos para citaciones y fuentes
- mantener la integración KB dentro de la misma Lambda del backend

## Siguiente paso recomendado

1. Mantener esta HU alineada con `ARCHITECTURE.md` y `GUIA-INTEGRACION-CHATBOT-DYNAMODB.md`.
2. Confirmar en refinamiento técnico la alineación completa entre seguridad, variables de entorno y despliegue.
3. Si cambia el contrato de `KnowledgeResponseDTO`, actualizar esta HU y los tests asociados.