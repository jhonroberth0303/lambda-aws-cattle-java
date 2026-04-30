# HU-ASEGURAMIENTO-CALIDAD-001: Aseguramiento de Calidad del Backend y Chatbot

**ID**: HU-ASEGURAMIENTO-CALIDAD-001  
**Tipo**: Quality Assurance / Technical Debt  
**Prioridad**: Crítica  
**Sprint original**: Sprint 3 - Q1 2026  
**Estado actual**: Reenfocada documentalmente - Abril 2026

## Objetivo

Mantener un marco verificable de calidad para el backend `lambda-aws-cattle-java`, con foco especial en el módulo chatbot y en la cobertura automatizada suficientemente alta para cambios seguros.

## Alcance vigente

Este artefacto ya no se usa como plan exhaustivo de cientos de tests estimados por clase, sino como referencia viva para:

- objetivos de cobertura
- superficies críticas que requieren pruebas
- criterios mínimos de ejecución y mantenimiento
- trazabilidad de deuda de calidad

## Evidencia revisada

- `build.gradle`
- `src/test/java/com/cattle/services/chatbot/ChatbotServiceTest.java`
- `src/test/java/com/cattle/controller/ChatbotControllerKnowledgeTest.java`
- `src/test/java/com/cattle/services/knowledge/KnowledgeBaseServiceTest.java`
- reportes y estructura de tests visibles en el repositorio

## Criterios de aceptación vigentes

### CA-001: Ejecución estándar de calidad

La ejecución base de calidad del proyecto debe seguir el flujo soportado:

```bash
./gradlew test jacocoTestReport
```

### CA-002: Cobertura utilizable para cambios seguros

La cobertura debe mantenerse en un nivel que permita refactorizar y corregir con seguridad, priorizando:

- controladores
- servicios
- query services
- seguridad
- chatbot y Knowledge Base

### CA-003: Prioridad por riesgo

Las superficies más críticas a cubrir son:

- `ChatbotController`
- `ChatbotService`
- `KnowledgeBaseService`
- `ContextBuilderService`
- `IntentDetectionService`
- `SecurityConfig` y filtros asociados
- repositorios y servicios con lógica de negocio relevante

### CA-004: Trazabilidad de deuda

Si una clase crítica no tiene pruebas suficientes, debe registrarse como gap explícito en lugar de asumirse cubierta por planificación histórica.

### CA-005: Reporte de cobertura vigente

El reporte de JaCoCo generado por Gradle debe seguir siendo la referencia principal para validar progreso real de cobertura.

## Estado observado

### Confirmado

- el proyecto usa JaCoCo desde Gradle
- existen pruebas visibles para chatbot y Knowledge Base
- la suite actual ya no debe describirse como dependiente de un proyecto externo `cattle-bedrock`

### No revalidado en esta pasada

- el porcentaje exacto actual de cobertura global
- el cumplimiento numérico de metas históricas como `85-90%` en todo el backend
- cobertura real por paquete al día de hoy

Por eso este artefacto no afirma una cifra actualizada que no se haya recalculado en esta revisión.

## Riesgos abiertos

- la planificación histórica de calidad puede sobrestimar cobertura o clases pendientes respecto del árbol real actual
- la deuda de pruebas sobre `/api/chat/message`, seguridad y algunos query services sigue siendo prioritaria hasta que se compruebe con reporte actualizado

## Criterio de mantenimiento

- usar este documento para orientar prioridades de testing
- usar el reporte JaCoCo real para medir avance
- evitar listas extensas de tests hipotéticos si no están respaldadas por código o backlog vigente

## Siguiente paso recomendado

1. Ejecutar y revisar cobertura actual antes de volver a fijar metas numéricas finas.
2. Priorizar cobertura de endpoints y servicios Bedrock con mayor impacto operativo.
3. Mantener alineadas esta HU, `build.gradle` y los tests reales bajo `src/test/java`.