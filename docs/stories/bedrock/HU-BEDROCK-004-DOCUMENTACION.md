# HU-BEDROCK-004: Documentación del Módulo Chatbot

**ID**: HU-BEDROCK-004  
**Prioridad**: Media  
**Sprint original**: S-2 (Enero 2026)  
**Dependencias**: HU-BEDROCK-001, HU-BEDROCK-002, HU-BEDROCK-003  
**Estado actual**: Revisado documentalmente - Abril 2026

## Objetivo

Mantener documentación navegable, útil y alineada al estado real del módulo Bedrock dentro de `lambda-aws-cattle-java`, evitando referencias a un proyecto independiente ya archivado o a endpoints que no existen en la implementación vigente.

## Alineación vigente

- La documentación de entrada del backend vive en `README.md` y `docs/README.md`.
- La arquitectura vigente del módulo está en `docs/arquitectura/chatbot/`.
- El chatbot está integrado en `lambda-aws-cattle-java`, no en un repositorio activo separado.

## Evidencia revisada

- `README.md`
- `docs/README.md`
- `docs/arquitectura/index.md`
- `docs/arquitectura/chatbot/ARCHITECTURE.md`
- `docs/arquitectura/chatbot/ARQUITECTURA-ECOSISTEMA-CATTLE.md`
- `docs/arquitectura/chatbot/GUIA-INTEGRACION-CHATBOT-DYNAMODB.md`

## Criterios de aceptación vigentes

### CA-001: README raíz consistente

El README principal del backend debe reflejar:

- Java 21
- Gradle Wrapper como build soportado
- despliegue con AWS SAM
- endpoints reales del backend y del chatbot
- variables de entorno relevantes de Bedrock, seguridad y tablas

### CA-002: Arquitectura base consistente

La carpeta `docs/arquitectura/` debe distinguir claramente:

- arquitectura base del backend
- subarquitectura del chatbot integrado
- sistema de eventos del dominio de potreros

### CA-003: Contratos HTTP documentados

La documentación vigente debe cubrir al menos estos endpoints del módulo chatbot:

- `POST /api/chat/message`
- `POST /api/chat/knowledge`
- `GET /api/chat/health`
- `GET /actuator/ping`
- `GET /swagger-ui.html`

### CA-004: Sin referencias activas a despliegue legacy

La documentación no debe presentar como vigente:

- un repositorio activo `cattle-bedrock`
- una Lambda independiente para el chatbot
- endpoints `/api/v1/*` no presentes en el código actual
- comandos principales basados en Maven cuando el flujo soportado es Gradle Wrapper

### CA-005: Trazabilidad de gaps

Cuando exista una discrepancia entre documentación y código, el artefacto debe dejarla explícita como gap o deuda abierta.

## Estado observado

### Ya corregido en esta pasada

- README raíz del backend alineado al código actual
- README de `docs/` alineado a la arquitectura vigente
- documentación base de `docs/arquitectura/chatbot/` corregida

### Gaps todavía relevantes fuera de esta HU

- artefactos históricos adicionales en `docs/stories/bedrock/` pueden seguir describiendo decisiones o planes de un momento anterior del proyecto
- la documentación operativa debe seguir revisándose cuando cambien seguridad, despliegue o contratos HTTP

## Siguiente paso recomendado

1. Mantener esta HU como referencia de calidad documental del módulo Bedrock.
2. Actualizar historias relacionadas cuando cambie el contrato del chatbot o la seguridad.
3. Evitar duplicar arquitectura detallada dentro de las HUs si ya existe en `docs/arquitectura/`.