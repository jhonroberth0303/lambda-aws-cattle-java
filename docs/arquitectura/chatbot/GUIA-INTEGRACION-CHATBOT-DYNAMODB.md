# Guía de Integración: Chatbot, Servicios de Consulta y DynamoDB

## Estado actual

La integración base ya existe dentro de `lambda-aws-cattle-java`. Este documento ya no debe leerse como roadmap para conectar un proyecto separado `cattle-bedrock`, sino como guía de cómo el chatbot integrado consume datos reales y qué piezas siguen pendientes de endurecer.

## Evidencia revisada

- `ChatbotController.java`
- `ChatbotService.java`
- `ContextBuilderService.java`
- `BovineQueryService.java`
- `MilkingQueryService.java`
- `PastureQueryService.java`
- `KnowledgeBaseService.java`
- `BedrockAgentConfig.java`

## Integración actual

### Consulta conversacional sobre datos operativos

El flujo actual ya reutiliza datos reales del backend:

1. `ChatbotController` recibe la solicitud.
2. `ChatbotService` detecta la intención.
3. `ContextBuilderService` consulta servicios especializados.
4. Esos servicios leen información persistida en DynamoDB a través de la capa existente del backend.
5. El contexto enriquecido se envía a Bedrock mediante `BedrockService`.

### Consulta a Knowledge Base

El flujo de Knowledge Base usa `KnowledgeBaseService` y `BedrockAgentRuntimeClient` para ejecutar `RetrieveAndGenerate` con una Knowledge Base configurada por ambiente.

## Mapa de servicios implicados

| Componente | Responsabilidad actual |
|---|---|
| `ChatbotController` | Entrypoint HTTP, rate limiting, sanitización, auditoría |
| `ChatbotService` | Orquestación intención → contexto → Bedrock |
| `IntentDetectionService` | Clasificación de intención |
| `ContextBuilderService` | Construcción de contexto textual a partir de datos de finca |
| `BovineQueryService` | Agregados y detalle de bovinos para contexto |
| `MilkingQueryService` | Agregados de producción láctea |
| `PastureQueryService` | Estado y agregados de potreros |
| `KnowledgeBaseService` | Consultas RAG con citaciones |

## Dependencias de configuración

La integración depende de variables de entorno y configuración visibles en código:

- `BEDROCK_MODEL_ID`
- `BEDROCK_KB_ID`
- `BEDROCK_KB_MODEL_ARN`
- `SECURITY_ENABLED`
- `RATE_LIMIT_PER_HOUR`

## Qué ya está resuelto

- El chatbot está integrado en el backend principal.
- Existe capa de intención, contexto y ejecución de modelo.
- Existen servicios de consulta para contexto de bovinos, milking y potreros.
- Existe integración con Knowledge Base y retorno de citaciones.

## Qué sigue pendiente o débil

- `BEDROCK_KB_ID` no aparece declarado en el `template.yml` revisado.
- La protección explícita de `SecurityConfig` necesita alinearse con todos los endpoints de chatbot realmente usados.
- La documentación histórica todavía conserva referencias a nombres de proyecto, rutas o despliegues legacy.
- El frontend todavía necesita una capa de integración más explícita para el chatbot si se quiere trazabilidad completa extremo a extremo.

## Recomendaciones de mantenimiento

1. Tratar esta integración como parte del backend principal, no como subsistema desplegado aparte.
2. Mantener alineados `template.yml`, `application.properties` y `SecurityConfig`.
3. Cuando cambie el contrato de contexto o intención, actualizar también `ARCHITECTURE.md`.

## No usar como fuente vigente

No debe asumirse como vigente ninguna instrucción que dependa de:

- un repositorio activo `cattle-bedrock`
- un pipeline de despliegue separado para el chatbot
- una Lambda independiente solo para Bedrock

Esos enfoques pertenecen al historial del proyecto, no a la arquitectura activa observada en este workspace.