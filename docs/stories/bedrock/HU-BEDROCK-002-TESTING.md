# HU-BEDROCK-002: Testing & QA del Módulo Chatbot

**ID**: HU-BEDROCK-002  
**Prioridad**: Alta  
**Sprint original**: S-2 (Enero 2026)  
**Dependencia**: HU-BEDROCK-001  
**Estado actual**: Revisado documentalmente - Abril 2026

## Objetivo

Definir y mantener una estrategia de pruebas verificable para el módulo Bedrock integrado en `lambda-aws-cattle-java`, cubriendo chat operativo, Knowledge Base y controles transversales de seguridad.

## Alineación vigente

- El chatbot vive dentro de `lambda-aws-cattle-java`, no en un repositorio activo separado.
- El flujo soportado de build y test usa `Gradle Wrapper`, no Maven como herramienta principal.
- La cobertura se genera en `build/reports/jacoco/test/html/index.html`.

## Evidencia revisada

- `src/test/java/com/cattle/services/chatbot/ChatbotServiceTest.java`
- `src/test/java/com/cattle/controller/ChatbotControllerKnowledgeTest.java`
- `src/test/java/com/cattle/services/knowledge/KnowledgeBaseServiceTest.java`
- `build.gradle`

## Criterios de aceptación vigentes

### CA-001: Ejecución estándar de tests

La suite del módulo debe poder ejecutarse con:

```bash
./gradlew test jacocoTestReport
```

Resultado esperado:

- ejecución exitosa de pruebas automatizadas
- reporte de cobertura JaCoCo generado
- sin dependencia de un flujo Maven como estándar del proyecto

### CA-002: Cobertura mínima del módulo chatbot

La cobertura del módulo Bedrock debe mantenerse en un nivel suficiente para cambios seguros, con foco en:

- orquestación de `ChatbotService`
- endpoint `/api/chat/knowledge`
- integración de `KnowledgeBaseService`
- errores, rate limiting y respuestas controladas

### CA-003: Tests unitarios sobre caminos críticos

Deben existir o completarse pruebas unitarias para:

- intención y construcción de contexto
- generación de respuestas del chatbot
- consultas a Knowledge Base
- límites por rate limiting
- errores de sanitización y de servicios externos

### CA-004: Tests de contrato HTTP

Los endpoints del módulo deben estar cubiertos por pruebas de controlador o integración para:

- `POST /api/chat/message`
- `POST /api/chat/knowledge`
- `GET /api/chat/health`

### CA-005: Trazabilidad de gaps

Si una prueba planeada aún no existe, el artefacto debe dejar claro qué falta y cuál es el siguiente paso técnico.

## Estado observado en el repositorio

### Implementado y visible

- existe `ChatbotServiceTest`
- existe `ChatbotControllerKnowledgeTest`
- existe `KnowledgeBaseServiceTest`
- el proyecto genera cobertura con JaCoCo vía Gradle

### Aún no confirmado con esta revisión

- suite completa de integración end-to-end del chatbot
- cobertura exhaustiva del endpoint `/api/chat/message`
- pruebas sistemáticas de performance o carga como artefacto ejecutable dentro del repo

## Riesgos abiertos

- documentación histórica de pruebas todavía refería un proyecto `cattle-bedrock` separado
- parte de la planificación original asumía clases y rutas de test que ya no representan la estructura actual

## Siguiente paso recomendado

1. Completar la matriz de pruebas vigente alrededor de `/api/chat/message`.
2. Consolidar en un solo índice qué pruebas Bedrock existen hoy y cuáles son deuda técnica.
3. Mantener esta HU sincronizada con `build.gradle` y con los archivos reales bajo `src/test/java`.